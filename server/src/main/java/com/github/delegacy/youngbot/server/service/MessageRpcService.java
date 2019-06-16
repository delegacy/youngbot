package com.github.delegacy.youngbot.server.service;

import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.protocol.v1.MessageRequest;
import com.github.delegacy.youngbot.protocol.v1.MessageResponse;
import com.github.delegacy.youngbot.protocol.v1.MessageServiceGrpc;

import io.grpc.stub.StreamObserver;

@Service
public class MessageRpcService extends MessageServiceGrpc.MessageServiceImplBase {
    @Override
    public void process(MessageRequest req, StreamObserver<MessageResponse> responseObserver) {
        final MessageResponse res = MessageResponse.newBuilder()
                                                   .setText(req.getText())
                                                   .build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }
}
