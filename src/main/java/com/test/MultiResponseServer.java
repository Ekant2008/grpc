package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class MultiResponseServer {
    private static final Logger logger = Logger.getLogger(MultiResponseServer.class.getName());
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreeterImpl())
                .build()
                .start();

        logger.info("Server started on port 50051");
        server.awaitTermination();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHelloManyTimes(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            if (request.getLastname().isEmpty()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Lastname cannot be empty")
                                .asRuntimeException()
                );
                return;
            }
            String name = request.getFirstname() + " " + request.getLastname();
            try {
                for (int i = 1; i <= 10; i++) {
                    String message = "Hello " + name + ", message " + i;
                    HelloReply reply = HelloReply.newBuilder()
                            .setMessage(message)
                            .build();

                    responseObserver.onNext(reply);  // send each message one by one
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {//
                responseObserver.onError(e);
            } finally {
                responseObserver.onCompleted();  // complete the stream
            }
        }
    }
}
