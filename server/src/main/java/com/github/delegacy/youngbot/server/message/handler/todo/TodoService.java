package com.github.delegacy.youngbot.server.message.handler.todo;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.delegacy.youngbot.server.RequestContext;
import com.github.delegacy.youngbot.server.message.handler.todo.TodoCommand.CommandType;
import com.github.delegacy.youngbot.server.platform.Platform;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);

    private final MTodoTaskRepository repository;

    @Inject
    TodoService(MTodoTaskRepository repository) {
        this.repository = requireNonNull(repository, "repository");
    }

    Mono<String> process(RequestContext ctx, TodoCommand todoCommand) {
        switch (todoCommand.commandType()) {
            case ADD:
                return processAdd(ctx, todoCommand.args());
            case ADDTO:
                return processAddTo(ctx, todoCommand.args());
            case DO:
                return processDo(ctx, todoCommand.args());
            case LIST:
                return processList(ctx, todoCommand.args());
            case LISTALL:
                return processListAll(ctx, todoCommand.args());
            case ARCHIVE:
                return processArchive(ctx);
            case HELP:
                return processHelp(todoCommand.args());
            default:
                return processShortHelp();
        }
    }

    private Mono<String> processAdd(RequestContext ctx, List<String> args) {
        if (args.size() != 1) {
            return processHelp(Collections.singleton("add"));
        }

        return internalProcessAddTo(ctx, MTodoTask.TODO_FILE, args.get(0));
    }

    private Mono<String> processAddTo(RequestContext ctx, List<String> args) {
        if (args.size() != 2) {
            return processHelp(Collections.singleton("addto"));
        }

        return internalProcessAddTo(ctx, args.get(0), args.get(1));
    }

    private Mono<String> internalProcessAddTo(RequestContext ctx, String dest, String text) {
        final String file = StringUtils.stripEnd(dest.toUpperCase(Locale.ENGLISH), ".TXT");
        final TodoTask task = TodoTask.of(text);
        final String normalizedText = task.toString();

        final MTodoTask model = new MTodoTask();
        model.setPlatform(ctx.platform());
        model.setChannelId(ctx.channelId());
        model.setText(normalizedText);
        model.setFile(file);

        return Mono.fromCallable(() -> repository.save(model))
                   .map(m -> String.format("%d %s\n%s: %d added",
                                           m.getId(), text, file, m.getId()))
                   .onErrorResume(t -> {
                       logger.warn("Failed to add {}", text, t);
                       return Mono.just(String.format("%s: Failed to add %s", file, text));
                   });
    }

    private Mono<String> processDo(RequestContext ctx, List<String> args) {
        final Set<Long> todoTaskIds = args.stream()
                                          .map(arg -> StringUtils.strip(arg, ","))
                                          .map(arg -> {
                                              try {
                                                  return Long.parseLong(arg);
                                              } catch (NumberFormatException e) {
                                                  return -1L;
                                              }
                                          })
                                          .filter(todoTaskId -> todoTaskId > 0)
                                          .collect(Collectors.toUnmodifiableSet());

        if (todoTaskIds.isEmpty()) {
            return processHelp(Collections.singleton("do"));
        }

        return findModels(todoTaskIds, ctx.platform(), ctx.channelId(), MTodoTask.TODO_FILE)
                .map(model -> model.setText(TodoTask.of(model.getText())
                                                    .markAsDone(LocalDate.now())
                                                    .toString()))
                .flatMap(model -> Mono.fromCallable(() -> repository.save(model)))
                .collectList()
                .map(models -> {
                    final String texts =
                            models.stream()
                                  .map(model -> String.format("%d %s", model.getId(), model.getText()))
                                  .collect(Collectors.joining("\n"));

                    final String summaries =
                            models.stream()
                                  .collect(Collectors.groupingBy(MTodoTask::getFile,
                                                                 Collectors.counting()))
                                  .entrySet().stream()
                                  .sorted(Comparator.comparing(Entry::getValue, Comparator.reverseOrder()))
                                  .map(entry -> String.format("%s: %d marked as done",
                                                              entry.getKey(), entry.getValue()))
                                  .collect(Collectors.joining("\n"));

                    return texts + '\n' + summaries;
                })
                .zipWith(processArchive(ctx), (s1, s2) -> s1 + '\n' + s2)
                .onErrorResume(t -> {
                    logger.warn("Failed to mark as done", t);
                    return Mono.just("Failed to mark as done");
                })
                .defaultIfEmpty("No tasks to mark as done");
    }

    private Flux<MTodoTask> findModels(Set<Long> taskIds, Platform platform, String channelId, String file) {
        return Mono.fromCallable(() -> repository.findAllById(taskIds))
                   .flatMapMany(models -> {
                       final List<MTodoTask> filtered =
                               models.stream()
                                     .filter(model -> model.getPlatform() == platform &&
                                                      model.getChannelId().equals(channelId) &&
                                                      model.getFile().equals(file))
                                     .collect(Collectors.toUnmodifiableList());

                       return filtered.isEmpty() ? Flux.empty() : Flux.fromIterable(filtered);
                   });
    }

    private Mono<String> processList(RequestContext ctx, List<String> terms) {
        return Mono.fromCallable(() -> repository.findByPlatformAndChannelIdAndFile(ctx.platform(),
                                                                                    ctx.channelId(),
                                                                                    MTodoTask.TODO_FILE))
                   .map(models -> respondTasks(models, terms))
                   .onErrorResume(t -> {
                       logger.warn("Failed to list tasks", t);
                       return Mono.just(String.format("%s: Failed to list tasks", MTodoTask.TODO_FILE));
                   });
    }

    private static String respondTasks(List<MTodoTask> models, List<String> terms) {
        final List<MTodoTask> filtered =
                models.stream()
                      .filter(model -> {
                          if (terms.isEmpty()) {
                              return true;
                          }
                          return terms.stream().allMatch(term -> model.getText().contains(term));
                      })
                      .sorted(Comparator.comparing(MTodoTask::getText))
                      .collect(Collectors.toList());

        final String texts = filtered.stream()
                                     .map(model -> String.format("%d %s", model.getId(), model.getText()))
                                     .collect(Collectors.joining("\n"));

        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(texts)) {
            sb.append(texts)
              .append('\n');
        }

        final Map<String, Long> counted =
                models.stream().collect(Collectors.groupingBy(MTodoTask::getFile, Collectors.counting()));

        final Map<String, Long> filteredCounted =
                filtered.stream().collect(Collectors.groupingBy(MTodoTask::getFile, Collectors.counting()));
        sb.append("--");
        if (counted.keySet().isEmpty()) {
            sb.append('\n')
              .append(MTodoTask.TODO_FILE)
              .append(": 0 of 0 tasks shown");
        } else {
            filteredCounted.entrySet().stream()
                           .sorted(Comparator.comparing(Entry::getValue, Comparator.reverseOrder()))
                           .forEach(entry -> sb.append('\n')
                                               .append(entry.getKey())
                                               .append(": ")
                                               .append(entry.getValue())
                                               .append(" of ")
                                               .append(counted.get(entry.getKey()))
                                               .append(" tasks shown"));

            if (counted.keySet().size() > 1) {
                sb.append("\nTOTAL: ")
                  .append(filtered.size())
                  .append(" of ")
                  .append(models.size())
                  .append(" tasks shown");
            }
        }

        return sb.toString();
    }

    private Mono<String> processListAll(RequestContext ctx, List<String> terms) {
        return Mono.fromCallable(() -> repository.findByPlatformAndChannelIdAndFileIn(
                ctx.platform(), ctx.channelId(), Set.of(MTodoTask.TODO_FILE, MTodoTask.DONE_FILE)))
                   .map(models -> respondTasks(models, terms))
                   .onErrorResume(t -> {
                       logger.warn("Failed to list all tasks", t);
                       return Mono.just("TOTAL: Failed to list all tasks");
                   });
    }

    private Mono<String> processArchive(RequestContext ctx) {
        return Mono.fromCallable(() -> repository.findByPlatformAndChannelIdAndFile(ctx.platform(),
                                                                                    ctx.channelId(),
                                                                                    MTodoTask.TODO_FILE))
                   .flatMapMany(models -> {
                       final List<MTodoTask> filtered =
                               models.stream()
                                     .filter(model -> TodoTask.of(model.getText()).isCompleted())
                                     .collect(Collectors.toUnmodifiableList());

                       return filtered.isEmpty() ? Flux.empty() : Flux.fromIterable(filtered);
                   })
                   .map(model -> model.setFile(MTodoTask.DONE_FILE))
                   .flatMap(model -> Mono.fromCallable(() -> repository.save(model)))
                   .collectList()
                   .map(models -> {
                       final String texts =
                               models.stream()
                                     .map(MTodoTask::getText)
                                     .collect(Collectors.joining("\n"));

                       final String summaries =
                               models.stream()
                                     .collect(Collectors.groupingBy(MTodoTask::getFile,
                                                                    Collectors.counting()))
                                     .entrySet().stream()
                                     .sorted(Comparator.comparing(Entry::getValue, Comparator.reverseOrder()))
                                     .map(entry -> String.format("%s: archived", entry.getKey()))
                                     .collect(Collectors.joining("\n"));

                       return texts + '\n' + summaries;
                   })
                   .onErrorResume(t -> {
                       logger.warn("Failed to archive", t);
                       return Mono.just("Failed to archive");
                   });
    }

    private static Mono<String> processHelp(Collection<String> actions) {
        return Flux.fromIterable(actions)
                   .map(CommandType::of)
                   .map(commandType -> {
                       switch (commandType) {
                           case ADD:
                               return new StringBuilder("add \"THING I NEED TO DO +project @context\"")
                                       .append("\na \"THING I NEED TO DO +project @context\"")
                                       .append("\n  Adds THING I NEED TO DO to your todo.txt file on its own line.")
                                       .append("\n  Project and context notation optional.")
                                       .append("\n  Quotes optional.")
                                       .toString();
                           case ADDTO:
                               return new StringBuilder("addto DEST \"TEXT TO ADD\"")
                                       .append("\n  Adds a line of text to any file located in the todo.txt directory.")
                                       .append("\n  For example, addto inbox.txt \"decide about vacation\"")
                                       .toString();
                           case ARCHIVE:
                               return new StringBuilder("archive")
                                       .append("\n  Moves all done tasks from todo.txt to done.txt and removes blank lines.")
                                       .toString();
                           case DO:
                               return new StringBuilder("do ITEM#[, ITEM#, ITEM#, ...]")
                                       .append("\n  Marks task(s) on line ITEM# as done in todo.txt.")
                                       .toString();
                           case LIST:
                               return new StringBuilder("list [TERM...]")
                                       .append("\nls [TERM...]")
                                       .append("\n  Displays all tasks that contain TERM(s) sorted by priority with line")
                                       .append("\n  numbers.  Each task must match all TERM(s) (logical AND); to display")
                                       .append("\n  tasks that contain any TERM (logical OR), use")
                                       .append("\n  \"TERM1\\|TERM2\\|...\" (with quotes), or TERM1\\\\|TERM2 (unquoted).")
                                       .append("\n  Hides all tasks that contain TERM(s) preceded by a")
                                       .append("\n  minus sign (i.e. -TERM). If no TERM specified, lists entire todo.txt.")
                                       .toString();
                           case LISTALL:
                               return new StringBuilder("listall [TERM...]")
                                       .append("\nlsa [TERM...]")
                                       .append("\n  Displays all the lines in todo.txt AND done.txt that contain TERM(s)")
                                       .append("\n  sorted by priority with line  numbers.  Hides all tasks that")
                                       .append("\n  contain TERM(s) preceded by a minus sign (i.e. -TERM).  If no")
                                       .append("\n  TERM specified, lists entire todo.txt AND done.txt")
                                       .append("\n  concatenated and sorted.")
                                       .toString();
                           case SHORTHELP:
                               return new StringBuilder("shorthelp")
                                       .append("\n  List the one-line usage of all actions.")
                                       .toString();
                           case HELP:
                               return new StringBuilder("help [ACTION...]")
                                       .append("\n  Display help about usage, options, built-in and add-on actions,")
                                       .append("\n  or just the usage help for the passed ACTION(s).")
                                       .toString();
                           default:
                               return StringUtils.EMPTY;
                       }
                   })
                   .filter(text -> !text.isEmpty())
                   .collectList()
                   .map(texts -> String.join("\n\n", texts));
    }

    private static Mono<String> processShortHelp() {
        final StringBuilder sb = new StringBuilder("Usage: todo action [task_number] [task_description]")
                .append("\n\nActions:")
                .append("\n  add|a \"THING I NEED TO DO +project @context\"")
                .append("\n  addto DEST \"TEXT TO ADD\"")
                .append("\n  archive")
                .append("\n  do ITEM#[, ITEM#, ITEM#, ...]")
                .append("\n  list|ls [TERM...]")
                .append("\n  listall|lsa [TERM...]")
                .append("\n  shorthelp")
                .append("\n\nSee \"help\" for more details.");
        return Mono.just(sb.toString());
    }
}
