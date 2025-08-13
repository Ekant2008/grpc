package com.test;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class MultiResponseServerTest {

    private ManagedChannel channel;
    private Server server;

    @BeforeEach
    void setUp() throws IOException {
        server = ServerBuilder.forPort(50050)
                .addService(new MultiResponseServer.GreeterImpl())
                .build()
                .start();

        channel = ManagedChannelBuilder.forAddress("localhost", 50050)
                .usePlaintext()
                .build();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (channel != null) channel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
        if (server != null) server.shutdownNow().awaitTermination();
    }

    private Throwable invokeSayHelloManyTimes(HelloRequest request, Consumer<HelloReply> onNext) throws InterruptedException {
        GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] errorHolder = new Throwable[1];

        stub.sayHelloManyTimes(request, new StreamObserver<>() {
            @Override public void onNext(HelloReply value) { onNext.accept(value); }
            @Override public void onError(Throwable t) { errorHolder[0] = t; latch.countDown(); }
            @Override public void onCompleted() { latch.countDown(); }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Stream did not complete in time");
        return errorHolder[0];
    }

    @Test
    void testSayHelloManyTimes() throws InterruptedException {
        List<String> receivedMessages = new ArrayList<>();
        Throwable error = invokeSayHelloManyTimes(
                HelloRequest.newBuilder().setFirstname("Aman").setLastname("Yadav").build(),
                r -> receivedMessages.add(r.getMessage())
        );

        assertNull(error, "Unexpected error: " + (error != null ? error.getMessage() : ""));
        assertEquals(10, receivedMessages.size());
        assertEquals("Hello Aman Yadav, message 1", receivedMessages.get(0));
        assertEquals("Hello Aman Yadav, message 10", receivedMessages.get(9));
    }

    @Test
    void testSayHelloManyTimes_ErrorCase() throws InterruptedException {
        Throwable error = invokeSayHelloManyTimes(
                HelloRequest.newBuilder().setFirstname("Yash").setLastname("").build(),
                r -> fail("onNext should not be called when error occurs")
        );

        assertNotNull(error, "Expected error but got null");
        assertTrue(error.getMessage().contains("Lastname cannot be empty"));
    }

}
