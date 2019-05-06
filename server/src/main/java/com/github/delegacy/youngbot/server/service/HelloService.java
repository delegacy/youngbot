package com.github.delegacy.youngbot.server.service;

import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.protocol.hello.HelloReply;
import com.github.delegacy.youngbot.protocol.hello.HelloRequest;
import com.github.delegacy.youngbot.protocol.hello.HelloServiceGrpc;

import io.grpc.stub.StreamObserver;

@Service
public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void hello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = HelloReply.newBuilder()
                                           .setMessage("Hello, " + req.getName() + '!')
                                           .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
