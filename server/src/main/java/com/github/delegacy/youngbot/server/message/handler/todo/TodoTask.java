package com.github.delegacy.youngbot.server.message.handler.todo;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

final class TodoTask {
    static TodoTask of(String text) {
        return TodoTxtTaskParser.parse(text);
    }

    private final String content;

    private final boolean completed;

    @Nullable
    private final LocalDate completionDate;

    @Nullable
    private final Character priority;

    @Nullable
    private final LocalDate creationDate;

    private final List<String> projects;

    private final List<String> contexts;

    private final Map<String, String> tags;

    private TodoTask(String content) {
        this(content,
             false, null,
             null, null,
             Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
    }

    @SuppressWarnings("squid:S00107")
    private TodoTask(String content,
                     boolean completed, @Nullable LocalDate completionDate,
                     @Nullable Character priority, @Nullable LocalDate creationDate,
                     List<String> projects, List<String> contexts, Map<String, String> tags) {

        this.content = requireNonNull(content, "content");
        this.completed = completed;
        this.completionDate = completionDate;
        this.priority = priority;
        this.creationDate = creationDate;
        this.projects = requireNonNull(projects, "projects");
        this.contexts = requireNonNull(contexts, "contexts");
        this.tags = requireNonNull(tags, "tags");
    }

    String content() {
        return content;
    }

    boolean isCompleted() {
        return completed;
    }

    Optional<LocalDate> completionDate() {
        return Optional.ofNullable(completionDate);
    }

    Optional<Character> priority() {
        return Optional.ofNullable(priority);
    }

    Optional<LocalDate> creationDate() {
        return Optional.ofNullable(creationDate);
    }

    List<String> projects() {
        return projects;
    }

    List<String> contexts() {
        return contexts;
    }

    Map<String, String> tags() {
        return tags;
    }

    TodoTask markAsDone(LocalDate completionDate) {
        final Map<String, String> newTags;
        if (priority == null) {
            newTags = tags;
        } else {
            newTags = new HashMap<>(tags);
            newTags.put("pri", priority.toString());
        }

        return new TodoTask(content,
                            true, completionDate,
                            null, creationDate,
                            projects, contexts, newTags);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (completed) {
            sb.append("x ");
        }
        if (completionDate != null) {
            sb.append(completionDate)
              .append(' ');
        }
        if (priority != null) {
            sb.append('(').append(priority)
              .append(") ");
        }
        if (creationDate != null) {
            sb.append(creationDate)
              .append(' ');
        }
        sb.append(content);
        return sb.toString();
    }

    private static final class TodoTxtTaskParser {
        private static final Pattern PATTERN =
                Pattern.compile("(^(?<completed>x)\\s+" +
                                "(?:(?<completionDate>\\d{4}-\\d{2}-\\d{2})\\s+)?)?" +
                                "(?:^\\((?<priority>[A-Z])\\)\\s+)?" +
                                "(?:(?<creationDate>\\d{4}-\\d{2}-\\d{2})\\s+)?" +
                                "(?<content>.+)$");

        private static final Pattern PATTERN_FOR_PROJECT =
                Pattern.compile("(?:^|\\s)+(?<project>\\+\\S+)", Pattern.UNICODE_CHARACTER_CLASS);

        private static final Pattern PATTERN_FOR_CONTEXT =
                Pattern.compile("(?:^|\\s)+(?<context>@\\S+)", Pattern.UNICODE_CHARACTER_CLASS);

        private static final Pattern PATTERN_FOR_TAG =
                Pattern.compile("(?:^|\\s)+(?<key>[\\S&&[^:]]+):(?<value>[\\S&&[^:]]+)(?:$|\\s)+",
                                Pattern.UNICODE_CHARACTER_CLASS);

        private static TodoTask parse(String text) {
            final Matcher matcher = PATTERN.matcher(text);
            if (!matcher.matches()) {
                return new TodoTask(text);
            }

            final boolean completed = matcher.group("completed") != null;

            final String strPriority = matcher.group("priority");
            final Character priority = strPriority == null ? null : Character.valueOf(strPriority.charAt(0));

            final String strCompletionDate = matcher.group("completionDate");
            LocalDate completionDate = null;
            if (strCompletionDate != null) {
                completionDate = LocalDate.parse(strCompletionDate);
            }

            final String strCreationDate = matcher.group("creationDate");
            LocalDate creationDate = null;
            if (strCreationDate != null) {
                creationDate = LocalDate.parse(strCreationDate);
            }

            final String content = matcher.group("content");

            final List<String> projects = new ArrayList<>();
            final Matcher projectMatcher = PATTERN_FOR_PROJECT.matcher(content);
            while (projectMatcher.find()) {
                projects.add(projectMatcher.group("project"));
            }

            final List<String> contexts = new ArrayList<>();
            final Matcher contextMatcher = PATTERN_FOR_CONTEXT.matcher(content);
            while (contextMatcher.find()) {
                contexts.add(contextMatcher.group("context"));
            }

            final Map<String, String> tags = new HashMap<>();
            final Matcher tagMatcher = PATTERN_FOR_TAG.matcher(content);
            while (tagMatcher.find()) {
                tags.put(tagMatcher.group("key"), tagMatcher.group("value"));
            }

            return new TodoTask(content,
                                completed, completionDate,
                                priority, creationDate,
                                projects, contexts, tags);
        }
    }
}
