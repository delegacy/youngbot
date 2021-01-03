package example.youngbot.boot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;

import com.github.delegacy.youngbot.event.EventService;
import com.github.delegacy.youngbot.line.LineClient;
import com.github.delegacy.youngbot.line.LineService;
import com.github.delegacy.youngbot.slack.SlackAppService;
import com.github.delegacy.youngbot.slack.SlackClient;
import com.github.delegacy.youngbot.slack.SlackRtmService;
import com.github.delegacy.youngbot.slack.SlackService;
import com.slack.api.bolt.App;
import com.slack.api.rtm.RTMClient;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.parser.LineSignatureValidator;

import example.youngbot.boot.YoungBotAutoConfigurationTest.TestApplication;

@SpringBootTest(
        webEnvironment = WebEnvironment.MOCK,
        classes = TestApplication.class
)
class YoungBotAutoConfigurationTest {
    @SpringBootApplication
    static class TestApplication {}

    @Resource
    private ApplicationContext appCtx;

    @ParameterizedTest
    @MethodSource("coreTypes")
    void testYoungBotAutoConfiguration(Class<?> type) throws Exception {
        appCtx.getBean(type);
    }

    private static Stream<Arguments> coreTypes() {
        return Stream.of(Arguments.of(EventService.class));
    }

    @ParameterizedTest
    @MethodSource("lineTypes")
    void testLineConfiguration(Class<?> type) throws Exception {
        appCtx.getBean(type);
    }

    private static Stream<Arguments> lineTypes() {
        return Stream.of(Arguments.of(LineSignatureValidator.class),
                         Arguments.of(LineMessagingClient.class),
                         Arguments.of(LineClient.class),
                         Arguments.of(LineService.class));
    }

    @ParameterizedTest
    @MethodSource("slackTypesEnabledByDefault")
    void testSlackConfiguration_enabledByDefault(Class<?> type) throws Exception {
        appCtx.getBean(type);
    }

    private static Stream<Arguments> slackTypesEnabledByDefault() {
        return Stream.of(Arguments.of(App.class),
                         Arguments.of(SlackClient.class),
                         Arguments.of(SlackService.class),
                         Arguments.of(SlackAppService.class));
    }

    @ParameterizedTest
    @MethodSource("slackTypesDisabledByDefault")
    void testSlackConfiguration_disabledByDefault(Class<?> type) throws Exception {
        assertThatThrownBy(() -> appCtx.getBean(type)).isInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private static Stream<Arguments> slackTypesDisabledByDefault() {
        return Stream.of(Arguments.of(RTMClient.class),
                         Arguments.of(SlackRtmService.class));
    }
}
