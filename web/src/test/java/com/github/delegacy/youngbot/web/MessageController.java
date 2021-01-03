package com.github.delegacy.youngbot.web;

import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.event.EventService;

@RestController
public class MessageController extends AbstractMessageController {
    public MessageController(EventService eventService) {
        super(eventService);
    }
}
