package com.github.delegacy.youngbot.server.message.handler.todo;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class TodoCommand {
    enum CommandType {
        UNKNOWN,
        ADD,
        ADDTO,
        APPEND,
        ARCHIVE,
        DEL,
        DEPRI,
        DO,
        LIST,
        LISTALL,
        LISTCON,
        LISTFILE,
        LISTPRI,
        LISTPROJ,
        MOVE,
        PREPEND,
        PRI,
        REPLACE,
        REPORT,
        HELP,
        SHORTHELP;

        static CommandType of(String cmd) {
            switch (cmd.toLowerCase(Locale.ENGLISH)) {
                case "a":
                case "add":
                    return ADD;
                case "addto":
                    return ADDTO;
                case "append":
                case "app":
                    return APPEND;
                case "archive":
                    return ARCHIVE;
                case "del":
                case "rm":
                    return DEL;
                case "depri":
                case "dp":
                    return DEPRI;
                case "do":
                    return DO;
                case "list":
                case "ls":
                    return LIST;
                case "listall":
                case "lsa":
                    return LISTALL;
                case "listcon":
                case "lsc":
                    return LISTCON;
                case "listfile":
                case "lf":
                    return LISTFILE;
                case "listpri":
                case "lsp":
                    return LISTPRI;
                case "listproj":
                case "lsprj":
                    return LISTPROJ;
                case "move":
                case "mv":
                    return MOVE;
                case "prepend":
                case "prep":
                    return PREPEND;
                case "pri":
                case "p":
                    return PRI;
                case "replace":
                    return REPLACE;
                case "report":
                    return REPORT;
                case "help":
                    return HELP;
                case "shorthelp":
                    return SHORTHELP;
                default:
                    return UNKNOWN;
            }
        }
    }

    static TodoCommand of(String text) {
        return TodoTxtCommandParser.parse(text);
    }

    static final TodoCommand UNKNOWN = new TodoCommand(CommandType.UNKNOWN, Collections.emptyList());

    static final TodoCommand SHORTHELP = new TodoCommand(CommandType.SHORTHELP, Collections.emptyList());

    private final CommandType commandType;

    private final List<String> args;

    private TodoCommand(CommandType commandType, List<String> args) {
        this.commandType = commandType;
        this.args = args;
    }

    CommandType commandType() {
        return commandType;
    }

    List<String> args() {
        return args;
    }

    private static final class TodoTxtCommandParser {
        private static final Pattern PATTERN_FOR_SPLIT = Pattern.compile("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        private static TodoCommand parse(String text) {
            final List<String> split = split(text);
            if (split.isEmpty()) {
                return SHORTHELP;
            }

            final CommandType commandType = CommandType.of(split.get(0));
            final List<String> args = split.size() == 1 ? Collections.emptyList()
                                                        : split.subList(1, split.size());
            switch (commandType) {
                case ADD:
                    return new TodoCommand(CommandType.ADD, merge(args));
                case ADDTO:
                    return new TodoCommand(CommandType.ADDTO, takeOneAndMerge(args));
                case APPEND:
                    return new TodoCommand(CommandType.APPEND, takeOneAndMerge(args));
                case ARCHIVE:
                    return new TodoCommand(CommandType.ARCHIVE, Collections.emptyList());
                case DEL:
                    return new TodoCommand(CommandType.DEL, args);
                case DEPRI:
                    return new TodoCommand(CommandType.DEPRI, args);
                case DO:
                    return new TodoCommand(CommandType.DO, args);
                case LIST:
                    return new TodoCommand(CommandType.LIST, args);
                case LISTALL:
                    return new TodoCommand(CommandType.LISTALL, args);
                case LISTCON:
                    return new TodoCommand(CommandType.LISTCON, Collections.emptyList());
                case LISTFILE:
                    return new TodoCommand(CommandType.LISTFILE, args);
                case LISTPRI:
                    return new TodoCommand(CommandType.LISTPRI, args);
                case LISTPROJ:
                    return new TodoCommand(CommandType.LISTPROJ, Collections.emptyList());
                case MOVE:
                    return new TodoCommand(CommandType.MOVE, args);
                case PREPEND:
                    return new TodoCommand(CommandType.PREPEND, takeOneAndMerge(args));
                case PRI:
                    return new TodoCommand(CommandType.PRI, args);
                case REPLACE:
                    return new TodoCommand(CommandType.REPLACE, takeOneAndMerge(args));
                case REPORT:
                    return new TodoCommand(CommandType.REPORT, Collections.emptyList());
                case HELP:
                    return new TodoCommand(CommandType.HELP, args);
                case SHORTHELP:
                    return SHORTHELP;
                default:
                    return UNKNOWN;
            }
        }

        private static List<String> split(@Nullable String str) {
            if (str == null) {
                return Collections.emptyList();
            }

            return Arrays.stream(PATTERN_FOR_SPLIT.split(str))
                         .map(String::trim)
                         .collect(Collectors.toUnmodifiableList());
        }

        private static List<String> merge(List<String> args) {
            return Collections.singletonList(join(args));
        }

        private static String join(List<String> args) {
            return String.join(" ", args);
        }

        private static List<String> takeOneAndMerge(List<String> args) {
            checkState(args.size() > 1, "args.size(): %s (expected: > 1)", args.size());

            return List.of(args.get(0), join(args.subList(1, args.size())));
        }
    }
}
