package com.github.delegacy.youngbot.slack.reaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.delegacy.youngbot.slack.reaction.processor.SlackReactionProcessor;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class SlackReactionServiceTest {
    private static class MatchedProcessor implements SlackReactionProcessor {
        @Override
        public boolean matches(SlackReactionRequest request) {
            return true;
        }

        @Override
        public Flux<SlackReactionResponse> process(SlackReactionRequest request) {
            return Flux.just(SlackReactionResponse.of(request, "matched"),
                             SlackReactionResponse.of(request, ""));
        }
    }

    private static class NotMatchedProcessor implements SlackReactionProcessor {
        @Override
        public boolean matches(SlackReactionRequest request) {
            return false;
        }

        @Override
        public Flux<SlackReactionResponse> process(SlackReactionRequest request) {
            return Flux.just(SlackReactionResponse.of(request, "not-matched"));
        }
    }

    private final SlackReactionService service =
            new SlackReactionService(Set.of(new MatchedProcessor(), new NotMatchedProcessor()));

    @Test
    void testProcess() throws Exception {
        final SlackReactionRequest req = new SlackReactionRequest("channel", "reaction", "user", "messageTs");
        StepVerifier.create(service.process(req))
                    .assertNext(res -> assertThat(res.text()).isEqualTo("matched"))
                    .expectComplete()
                    .verify();
    }
}
