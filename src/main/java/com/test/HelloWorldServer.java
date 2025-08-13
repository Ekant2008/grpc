package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class HelloWorldServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreeterImpl())
                .build();

        System.out.println("Starting gRPC Server on port 50051...");
        server.start();
        server.awaitTermination();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            String message = "Hello, " + request.getFirstname() + " " + request.getLastname() + "!";
            HelloReply reply = HelloReply.newBuilder().setMessage(message).build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
