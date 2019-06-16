package com.github.delegacy.youngbot.server.message.handler.todo;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.github.delegacy.youngbot.server.message.MessageContext;
import com.github.delegacy.youngbot.server.message.handler.MessageHandler;

import reactor.core.publisher.Flux;

@Component
class TodoMessageHandler implements MessageHandler {
    private static final Pattern PATTERN =
            Pattern.compile("^/?todo(?:\\.sh)?(?:\\s+(?<args>.+))?$", CASE_INSENSITIVE);

    private final TodoService todoService;

    @Inject
    TodoMessageHandler(TodoService todoService) {
        this.todoService = requireNonNull(todoService, "todoService");
    }

    @Override
    public Pattern pattern() {
        return PATTERN;
    }

    @Override
    public Flux<String> handle(MessageContext msgCtx, Matcher matcher) {
        final String args = matcher.group("args");
        final TodoCommand todoCommand = TodoCommand.of(args);
        return todoService.process(msgCtx, todoCommand)
                          .flux();
    }
}
