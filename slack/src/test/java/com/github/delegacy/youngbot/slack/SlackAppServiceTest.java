package com.github.delegacy.youngbot.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.response.Response;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SlackAppServiceTest {
    @Mock
    private App app;

    @Mock
    private SlackService slackService;

    @InjectMocks
    private SlackAppService slackAppService;

    @BeforeEach
    void beforeEach() throws Exception {
        slackAppService.init();
    }

    @Test
    void testRun(@Mock Request<?> request, @Mock Response response) throws Exception {
        when(app.run(eq(request))).thenReturn(response);

        StepVerifier.create(slackAppService.run(request))
                    .assertNext(it -> assertThat(it).isSameAs(response))
                    .expectComplete()
                    .verify();
    }
}
