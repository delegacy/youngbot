package example.spring.boot.slack;

import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.slack.AbstractSlackController;
import com.github.delegacy.youngbot.slack.SlackAppService;
import com.slack.api.bolt.App;

@RestController
public class SlackController extends AbstractSlackController {
    public SlackController(App app, SlackAppService slackAppService) {
        super(app, slackAppService);
    }
}
