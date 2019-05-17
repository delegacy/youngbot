package com.github.delegacy.youngbot.server.conf;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Configuration
public class MdcContextLifterConfiguration {
    private static final String MDC_CONTEXT_REACTOR_KEY = MdcContextLifterConfiguration.class.getName();

    @PostConstruct
    private void init() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                             Operators.lift((scannable, coreSubscriber) ->
                                                    new MdcContextLifter<>(coreSubscriber)));
    }

    @PreDestroy
    private void close() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }

    @RequiredArgsConstructor
    private static class MdcContextLifter<T> implements CoreSubscriber<T> {
        private final CoreSubscriber<T> coreSubscriber;

        @Override
        public void onSubscribe(Subscription s) {
            coreSubscriber.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            coreSubscriber.onError(t);
        }

        @Override
        public void onComplete() {
            coreSubscriber.onComplete();
        }

        @Override
        public Context currentContext() {
            return coreSubscriber.currentContext();
        }

        private static void copyToMdc(Context context) {
            if (context.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(context.stream()
                                         .collect(Collectors.toMap(e -> e.getKey().toString(),
                                                                   e -> e.getValue().toString())));
            }
        }
    }
}
