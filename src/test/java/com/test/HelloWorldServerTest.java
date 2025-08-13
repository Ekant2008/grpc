package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldServerTest {

    private Server server;
    private ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws IOException {
        server = ServerBuilder.forPort(50051)
                .addService(new HelloWorldServer.GreeterImpl() {
                    @Override
                    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
                        if (request.getFirstname().isEmpty()) {
                            responseObserver.onError(
                                    Status.INVALID_ARGUMENT
                                            .withDescription("Firstname cannot be empty")
                                            .asRuntimeException()
                            );
                            return;
                        }
                        super.sayHello(request, responseObserver);
                    }
                })
                .build()
                .start();

        // Create channel and stub
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
        }
        if (server != null) {
            server.shutdownNow().awaitTermination();
        }
    }

    @Test
    void sayHello_ValidRequest_ReturnsGreeting() {
        HelloRequest request = HelloRequest.newBuilder()
                .setFirstname("Ekant")
                .setLastname("Yadav")
                .build();

        HelloReply reply = blockingStub.sayHello(request);

        assertEquals("Hello, Ekant Yadav!", reply.getMessage());
    }

    @Test
    void sayHello_EmptyFirstname_ThrowsError() {
        HelloRequest request = HelloRequest.newBuilder()
                .setFirstname("")
                .setLastname("Yadav")
                .build();

        Exception ex = assertThrows(Exception.class, () -> blockingStub.sayHello(request));

        assertTrue(ex.getMessage().contains("Firstname cannot be empty"));
    }
}
