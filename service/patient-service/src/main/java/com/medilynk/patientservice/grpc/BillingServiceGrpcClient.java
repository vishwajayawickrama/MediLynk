package com.medilynk.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillingServiceGrpcClient {
    // Blocking stub used to make synchronous gRPC calls to the Billing Service
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    /**
     * Constructor initializes the gRPC channel and stub for Billing Service communication.
     * @param serverAddress Address of the Billing Service (from properties or default)
     * @param serverPort Port of the Billing Service (from properties or default)
     */
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress, // Default to localhost if not set
            @Value("${billing.service.port:9001}") int serverPort) { // Default to 9001 if not set
        // Log the connection details for debugging
        log.info("Connecting to  Billing Service GRPC service at {}:{}", serverAddress, serverPort);

        // Create a gRPC channel to the Billing Service using plaintext (no SSL)
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        // Initialize the blocking stub for making synchronous calls
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Creates a billing account for a patient by making a gRPC call to the Billing Service.
     * @param patientId Unique identifier for the patient
     * @param name Name of the patient
     * @param email Email address of the patient
     * @return BillingResponse containing the result of the account creation
     */
    public BillingResponse createBillingAccount(
            String patientId,
            String name,
            String email) {

        // Build the BillingRequest with patient details
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        // Make the gRPC call to create the billing account and receive the response
        BillingResponse response = blockingStub.createBillingAccount(request);
        // Log the response for debugging and traceability
        log.info("Received response form billing service via grpc {}", response);
        return response;
    }
}
