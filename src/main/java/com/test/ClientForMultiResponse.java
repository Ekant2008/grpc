package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ClientForMultiResponse {
    private static final Logger logger = Logger.getLogger(ClientForMultiResponse.class.getName());
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterStub asyncStub = GreeterGrpc.newStub(channel);

        HelloRequest request = HelloRequest.newBuilder()
                .setFirstname("Ekant")
                .setLastname("Yadav")
                .build();

        CountDownLatch latch = new CountDownLatch(1);

        asyncStub.sayHelloManyTimes(request, new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
                logger.info("Received: " + value.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Error from server: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Stream completed");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        channel.shutdown();
    }
}
