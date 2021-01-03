package com.github.delegacy.youngbot.slack;

import org.springframework.web.bind.annotation.RestController;

import com.slack.api.bolt.App;

@RestController
public class SlackController extends AbstractSlackController {
    public SlackController(App app, SlackAppService slackAppService) {
        super(app, slackAppService);
    }
}
