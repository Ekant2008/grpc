package com.test;

import com.example.grpc.JsonFileRequest;
import com.example.grpc.JsonFileResponse;
import com.example.grpc.JsonServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class FileServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new JsonServiceImpl())
                .build();

        System.out.println("Starting JSON gRPC Server on port 50051...");
        server.start();
        server.awaitTermination();
    }

    static class JsonServiceImpl extends JsonServiceGrpc.JsonServiceImplBase {
        @Override
        public void sendJsonFile(JsonFileRequest request, StreamObserver<JsonFileResponse> responseObserver) {
            byte[] jsonBytes = request.getJsonFileContent().toByteArray();

            // Convert bytes to string (for demo, you can save or process as needed)
            String jsonString = new String(jsonBytes);

            System.out.println("Received JSON content:\n" + jsonString);

            JsonFileResponse response = JsonFileResponse.newBuilder()
                    .setStatus("JSON received successfully, size: " + jsonBytes.length + " bytes")
                    .build();


            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
