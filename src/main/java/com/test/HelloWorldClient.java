package com.test;


import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.logging.Logger;

public class HelloWorldClient {
    private final static Logger logger = Logger.getLogger(HelloWorldClient.class.getName());
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

        HelloRequest request = HelloRequest.newBuilder()
                .setFirstname("Ekant")
                .setLastname("Yadav")

                .build();

        HelloReply response = stub.sayHello(request);
        logger.info("Response: " + response.getMessage());

        channel.shutdown();
    }
}
