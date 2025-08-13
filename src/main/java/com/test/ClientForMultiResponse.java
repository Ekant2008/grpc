package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientForMultiResponse {

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
                System.out.println("Received: " + value.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed");
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        channel.shutdown();
    }
}
