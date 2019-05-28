package com.github.delegacy.youngbot.server.conf;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import com.github.delegacy.youngbot.server.ReactorContextFilter;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Configuration
public class MdcContextLifterConfiguration {
    private static final String MDC_CONTEXT_REACTOR_KEY = MdcContextLifterConfiguration.class.getName();

    private static final Set<String> ALLOWED_MDC_KEYS = Set.of(ReactorContextFilter.REQUEST_ID_KEY);

    @SuppressWarnings("MethodMayBeStatic")
    @PostConstruct
    private void init() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
                             Operators.lift((scannable, coreSubscriber) ->
                                                    new MdcContextLifter<>(coreSubscriber)));
    }

    @SuppressWarnings("MethodMayBeStatic")
    @PreDestroy
    private void close() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }

    private static final class MdcContextLifter<T> implements CoreSubscriber<T> {
        private static void copyToMdc(Context context) {
            if (context.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(context.stream()
                                         .filter(e -> ALLOWED_MDC_KEYS.contains(e.getKey()))
                                         .collect(Collectors.toMap(e -> e.getKey().toString(),
                                                                   e -> e.getValue().toString())));
            }
        }

        private final CoreSubscriber<T> coreSubscriber;

        private MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
            this.coreSubscriber = coreSubscriber;
        }

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
    }
}
