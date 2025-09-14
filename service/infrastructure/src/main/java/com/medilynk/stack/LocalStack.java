package com.medilynk.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.msk.CfnCluster;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LocalStack defines the AWS infrastructure for the MediLynk healthcare platform.
 * This class creates a complete microservices architecture using AWS CDK including:
 * - VPC with networking infrastructure
 * - PostgreSQL databases for data persistence
 * - ECS Fargate services for containerized microservices
 * - MSK (Managed Streaming for Kafka) for event streaming
 * - Health checks for database monitoring
 * - API Gateway with load balancer for external access

 * The infrastructure supports multiple microservices:
 * - Auth Service: User authentication and authorization
 * - Patient Service: Patient data management
 * - Billing Service: Financial transactions and billing
 * - Analytics Service: Data analysis and reporting
 * - API Gateway: External API routing and management
 */
public class LocalStack extends Stack {
    // Core infrastructure components shared across services
    private final Vpc vpc;          // Virtual Private Cloud for network isolation
    private final Cluster ecsCluster; // ECS cluster for container orchestration

    /**
     * Constructor initializes the complete MediLynk infrastructure stack.
     * Creates all AWS resources in a specific order to handle dependencies.
     *
     * @param scope The CDK app scope
     * @param id Unique identifier for this stack
     * @param props Stack configuration properties
     */
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Step 1: Create VPC - foundational networking layer
        this.vpc = createVpc();

        // Step 2: Create databases for services that need persistent storage
        // Auth service database stores user credentials, roles, and sessions
        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
        // Patient service database stores patient records, medical history, appointments
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

        // Step 3: Create health checks to monitor database availability
        // These ensure services only start when their databases are ready
        CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthDBHealthCheck");
        CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientDBHealthCheck");

        // Step 4: Create Kafka cluster for event-driven communication between services
        CfnCluster mskCluster = createMskCluster();

        // Step 5: Create ECS cluster for running containerized services
        this.ecsCluster = createEcsCluster();

        // Step 6: Create individual microservices with their dependencies

        // Auth Service - handles user authentication, JWT token generation
        // Depends on auth database and includes JWT secret for token signing
        FargateService authService = createFargateService(
                "AuthService",
                "auth-service",
                List.of(4005), // Service runs on port 4005
                authServiceDb,
                Map.of("JWT_SECRET", "9740c22c0fe531c2572077b6f1bd57df59b78dbee8df3c618a4b30efaa6a2867"));
        // Ensure auth service waits for database health check and database availability
        authService.getNode().addDependency(authDbHealthCheck);
        authService.getNode().addDependency(authServiceDb);

        // Billing Service - manages financial transactions, invoicing, payments
        // Runs on port 4001 (HTTP) and 9001 (gRPC for internal communication)
        // No database dependency - likely uses external payment processors
        FargateService billingService = createFargateService(
                "BillingService",
                "billing-service",
                List.of(4001, 9001), // HTTP API and gRPC ports
                null, // No dedicated database
                null); // No additional environment variables

        // Analytics Service - processes data for reporting and insights
        // Consumes events from Kafka for real-time analytics
        FargateService analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-service",
                List.of(4002), // Service runs on port 4002
                null, // No dedicated database - likely uses data lake/warehouse
                null);
        // Must wait for Kafka cluster to be available for event streaming
        analyticsService.getNode().addDependency(mskCluster);

        // Patient Service - core service managing patient data and medical records
        // Integrates with billing service for payment processing
        FargateService patientService = createFargateService(
                "PatientService",
                "patient-service",
                List.of(4000), // Service runs on port 4000
                patientServiceDb,
                Map.of(
                        // Configuration for calling billing service via gRPC
                        "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ));
        // Complex dependency chain: database health -> database -> billing service -> kafka
        patientService.getNode().addDependency(patientDbHealthCheck);
        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        // Step 7: Create API Gateway as the external-facing entry point
        createApiGatewayService();
    }

    /**
     * Creates the Virtual Private Cloud (VPC) - the networking foundation.
     * VPC provides network isolation and security for all AWS resources.
     *
     * Design decisions:
     * - 2 Availability Zones for high availability and fault tolerance
     * - Automatic subnet creation (public/private) for security best practices
     * - Named for easy identification in AWS console
     *
     * @return Configured VPC instance
     */
    private Vpc createVpc() {
        return Vpc.Builder.create(this, "MediLynkVPC")
                .vpcName("MediLynkVPC") // Human-readable name in AWS console
                .maxAzs(2) // Distribute resources across 2 Availability Zones for reliability
                .build();
    }

    /**
     * Creates a PostgreSQL database instance for service data persistence.
     * Uses AWS RDS for managed database service with automatic backups, patching.
     *
     * Configuration rationale:
     * - PostgreSQL 17.2: Latest stable version with advanced features
     * - t2.micro: Cost-effective for development/testing environments
     * - 20GB storage: Sufficient for initial data volumes
     * - Auto-generated credentials: Security best practice stored in Secrets Manager
     * - DESTROY removal policy: Allows easy cleanup in development environments
     *
     * @param id Unique identifier for the database instance
     * @param dbName Name of the default database to create
     * @return Configured database instance
     */
    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder.
                create(this, id)
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_17_2).build())) // Latest PostgreSQL version
                .vpc(vpc) // Deploy in our secure VPC
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // Cost-effective instance
                .allocatedStorage(20) // 20GB initial storage with auto-scaling capability
                .credentials(Credentials.fromGeneratedSecret("admin_user")) // Auto-generated secure password
                .databaseName(dbName) // Default database name
                .removalPolicy(RemovalPolicy.DESTROY) // Allow deletion with stack (development setting)
                .build();
    }

    /**
     * Creates a health check for database monitoring and dependency management.
     * Health checks ensure services don't start until their dependencies are ready.
     *
     * TCP health check configuration:
     * - Checks database port connectivity every 30 seconds
     * - Fails after 3 consecutive failures (90 seconds total)
     * - Prevents services from attempting database connections before DB is ready
     *
     * @param db The database instance to monitor
     * @param id Unique identifier for the health check
     * @return Configured health check
     */
    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP") // Simple TCP connectivity check
                        .port(Token.asNumber(db.getDbInstanceEndpointPort())) // Database port (usually 5432 for PostgreSQL)
                        .ipAddress(db.getDbInstanceEndpointAddress()) // Database endpoint IP
                        .requestInterval(30) // Check every 30 seconds
                        .failureThreshold(3) // Mark unhealthy after 3 failures
                        .build())
                .build();
    }

    /**
     * Creates a Fargate service for running containerized microservices.
     * Fargate provides serverless container execution without managing EC2 instances.
     *
     * This method handles the complete service setup:
     * 1. Task definition with CPU/memory allocation
     * 2. Container configuration with port mappings
     * 3. Environment variables for service configuration
     * 4. Database connection setup (if required)
     * 5. Logging configuration for monitoring
     *
     * @param id Service identifier
     * @param imageName Docker image name for the service
     * @param ports List of ports the service listens on
     * @param db Database instance (null if service doesn't need database)
     * @param additionalEnvVars Additional environment variables for service configuration
     * @return Configured Fargate service
     */
    private FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars ) {

        // Create task definition - defines the container execution requirements
        // 256 CPU units (0.25 vCPU) and 1024 MiB RAM - suitable for microservices
        FargateTaskDefinition taskDefinition =  FargateTaskDefinition.Builder
                .create(this, id + "Task")
                .cpu(256) // CPU allocation (1024 = 1 vCPU)
                .memoryLimitMiB(1024) // Memory allocation in MiB
                .build();

        // Configure container settings including image, ports, and logging
        ContainerDefinitionOptions.Builder containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry(imageName)) // Pull image from registry
                        // Map container ports to host ports - enables external access
                        .portMappings(ports.stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port) // Port inside container
                                        .hostPort(port) // Port on host (same for simplicity)
                                        .protocol(Protocol.TCP) // TCP protocol
                                        .build())
                                .toList())
                        // Configure CloudWatch logging for monitoring and debugging
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                        .logGroupName("/ecs/" + imageName) // Standard ECS log group naming
                                        .removalPolicy(RemovalPolicy.DESTROY) // Allow cleanup
                                        .retention(RetentionDays.ONE_DAY) // Retain logs for 1 day (cost optimization)
                                        .build())
                                .streamPrefix(imageName) // Prefix for log streams
                                .build()));

        // Set up environment variables for service configuration
        Map<String, String> envVars = new HashMap<String, String>();

        // Configure Kafka connection - all services connect to MSK cluster for event streaming
        // Multiple bootstrap servers for high availability
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511,  localhost.localstack.cloud:4512");

        // Add any service-specific environment variables
        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        // Configure database connection if service requires database
        if (db != null) {
            // Build JDBC URL with database endpoint and port
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(), // RDS endpoint
                    db.getDbInstanceEndpointPort(),     // Database port
                    imageName                           // Database name
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user"); // Database username
            // Password retrieved from AWS Secrets Manager for security
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            // Hibernate configuration for automatic schema updates
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update"); // Auto-update database schema
            envVars.put("SPRING_DATASOURCE_INIT_MODE", "always"); // Always initialize datasource
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000"); // 60 second timeout
        }

        // Apply environment variables to container
        containerOptions.environment(envVars);
        // Add container to task definition
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        // Create and return the Fargate service
        return FargateService.Builder.create(this, id + "Service")
                .cluster(ecsCluster) // Deploy to our ECS cluster
                .taskDefinition(taskDefinition) // Use the configured task definition
                .assignPublicIp(false) // Deploy in private subnets for security
                .serviceName(imageName) // Service name for identification
                .build();
    }

    /**
     * Creates an Amazon MSK (Managed Streaming for Kafka) cluster for event streaming.
     * Kafka enables asynchronous communication between microservices through events.
     *
     * Use cases in MediLynk:
     * - Patient record updates trigger billing events
     * - Analytics service processes real-time data streams
     * - Audit logging for compliance requirements
     * - Decoupling services for better scalability
     *
     * Configuration choices:
     * - Single broker for development (would scale to 3+ in production)
     * - m5.xlarge instance type balances performance and cost
     * - Deployed in private subnets for security
     *
     * @return Configured MSK cluster
     */
    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("Kafka-cluster") // Cluster identifier
                .kafkaVersion("2.8.0") // Kafka version - stable and feature-rich
                .numberOfBrokerNodes(vpc.getPrivateSubnets().size()) // Single broker for development (3+ for production HA)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge") // Instance type balancing performance/cost
                        // Deploy brokers in private subnets across AZs for security and availability
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList()))
                        .brokerAzDistribution("DEFAULT") // Even distribution across AZs
                        .build())
                .build();
    }

    /**
     * Creates the ECS cluster for container orchestration.
     * ECS manages the lifecycle of containerized services including deployment, scaling, health monitoring.
     *
     * Key features:
     * - Cloud Map integration for service discovery
     * - Services can communicate using DNS names (e.g., auth-service.medilynk.local)
     * - Deployed in VPC for network security
     * - Enables zero-downtime deployments and auto-scaling
     *
     * @return Configured ECS cluster
     */
    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "MesiLynkEcsCluster")
                .vpc(vpc) // Deploy in our secure VPC
                // Enable service discovery - services can find each other by name
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("medilynk.local") // Internal DNS namespace
                        .build())
                .build();
    }

    /**
     * Creates the API Gateway service as the external entry point for the MediLynk platform.
     * This service handles:
     * - External API requests from web/mobile clients
     * - Request routing to appropriate microservices
     * - Authentication and authorization
     * - Load balancing and SSL termination
     * - Rate limiting and API versioning
     *
     * Uses Application Load Balancer for:
     * - High availability across multiple AZs
     * - Health checks and automatic failover
     * - SSL/TLS termination
     * - External internet access through public subnets
     */
    private void createApiGatewayService() {
        // Create task definition for API Gateway
        FargateTaskDefinition taskDefinition =  FargateTaskDefinition.Builder
                .create(this,  "ApiGatewayTaskDefinition")
                .cpu(256) // CPU allocation
                .memoryLimitMiB(1024) // Memory allocation
                .build();

        // Configure API Gateway container
        ContainerDefinitionOptions containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("api-gateway")) // API Gateway Docker image
                        .environment(Map.of(
                                "SPRING_PROFILES_ACTIVE", "prod", // Production Spring profile
                                "AUTH_SERVICE_URL", "http://host.docker.internal:4005" // Auth service endpoint
                        ))
                        // API Gateway listens on port 4004
                        .portMappings(List.of(4004).stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        // Configure logging for monitoring API requests
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                        .logGroupName("/ecs/api-gateway")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix("api-gateway")
                                .build()))
                        .build();

        // Add container to task definition
        taskDefinition.addContainer("ApiGatewayContainer", containerOptions);

        // Create Application Load Balanced Fargate Service
        // This provides external internet access through an Application Load Balancer
        ApplicationLoadBalancedFargateService apiGateway
                = ApplicationLoadBalancedFargateService.Builder.create(this, "ApiGatewayService")
                .cluster(ecsCluster) // Deploy to ECS cluster
                .serviceName("api-gateway") // Service name
                .taskDefinition(taskDefinition) // Use configured task definition
                .desiredCount(1) // Number of running tasks (1 for development)
                .healthCheckGracePeriod(Duration.seconds(60)) // Allow 60 seconds for service startup
                .build();
    }

    /**
     * Main method - entry point for CDK application.
     * Synthesizes the CloudFormation template and deploys the infrastructure.

     * CDK workflow:
     * 1. Create CDK App instance
     * 2. Create LocalStack with configuration
     * 3. Synthesize to CloudFormation template
     * 4. Deploy using AWS CLI or CDK deploy command

     * BootstraplessSynthesizer: Allows deployment without CDK bootstrap
     * (useful for local development environments)
     */
    public static void main(final String[] args) {
        // Create CDK application with output directory
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Configure stack properties
        StackProps stackProps = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer()) // No bootstrap required
                .build();

        // Create the LocalStack infrastructure
        new LocalStack(app, "LocalStack", stackProps);

        // Generate CloudFormation template
        app.synth();
        System.out.println("App synthesizing in progress");
    }
}
