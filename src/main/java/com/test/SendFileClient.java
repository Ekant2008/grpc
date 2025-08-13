package com.test;

import com.example.grpc.JsonFileRequest;
import com.example.grpc.JsonFileResponse;
import com.example.grpc.JsonServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

public class SendFileClient {

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        JsonServiceGrpc.JsonServiceBlockingStub jsonStub = JsonServiceGrpc.newBlockingStub(channel);

        // Load JSON file bytes (replace path with your actual JSON file path)
        byte[] jsonBytes = Files.readAllBytes(Path.of("src/main/resources/sample.json"));

        JsonFileRequest jsonRequest = JsonFileRequest.newBuilder()
                .setJsonFileContent(com.google.protobuf.ByteString.copyFrom(jsonBytes))
                .build();

        JsonFileResponse jsonResponse = jsonStub.sendJsonFile(jsonRequest);
        System.out.println("JSON Service response: " + jsonResponse.getStatus());

        channel.shutdown();
    }

}
