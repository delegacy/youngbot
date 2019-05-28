package com.github.delegacy.youngbot.server.line;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.delegacy.youngbot.server.TheVoid;

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.Service;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.SimpleDecoratingService;
import com.linecorp.armeria.testing.junit.server.ServerExtension;

import reactor.test.StepVerifier;

class LineServiceTest {
    private static final String DUMMY_TOKEN = "DUMMY_TOKEN";

    @RegisterExtension
    static final ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            final Function<Service<HttpRequest, HttpResponse>, Service<HttpRequest, HttpResponse>> decorator =
                    s -> new SimpleDecoratingService<>(s) {
                        @Override
                        public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                            final String authorization = req.headers().get(HttpHeaderNames.AUTHORIZATION);

                            if (!("Bearer " + DUMMY_TOKEN).equals(authorization)) {
                                return HttpResponse.of(HttpStatus.UNAUTHORIZED);
                            }

                            return delegate().serve(ctx, req);
                        }
                    };
            sb.decorator(decorator);

            sb.service("/v2/bot/message/reply", new AbstractHttpService() {
                @Override
                protected HttpResponse doPost(ServiceRequestContext ctx, HttpRequest req) {
                    return HttpResponse.of(HttpStatus.OK);
                }
            });
        }
    };

    @Test
    void testReplyMessage() {
        final LineService service = new LineService(server.uri("/"), DUMMY_TOKEN);

        StepVerifier.create(service.replyMessage("aReplyToken", "aText"))
                    .expectNext(TheVoid.INSTANCE)
                    .verifyComplete();
    }
}
