package com.github.delegacy.youngbot.server.message.handler.todo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.delegacy.youngbot.server.message.handler.todo.TodoCommand.CommandType;

class TodoCommandTest {
    @Test
    void testAdd() {
        TodoCommand cmd = TodoCommand.of("add \"THING I NEED TO DO +project @context\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ADD);
        assertThat(cmd.args()).containsExactly("\"THING I NEED TO DO +project @context\"");

        cmd = TodoCommand.of("a \"THING I NEED TO DO +project @context\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ADD);
        assertThat(cmd.args()).containsExactly("\"THING I NEED TO DO +project @context\"");

        // quotes optional
        cmd = TodoCommand.of("a THING I NEED TO DO +project @context");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ADD);
        assertThat(cmd.args()).containsExactly("THING I NEED TO DO +project @context");
    }

    @Test
    void testAddTo() {
        TodoCommand cmd = TodoCommand.of("addto DEST \"TEXT TO ADD\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ADDTO);
        assertThat(cmd.args()).containsExactly("DEST", "\"TEXT TO ADD\"");

        // quotes optional
        cmd = TodoCommand.of("addto DEST TEXT TO ADD");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ADDTO);
        assertThat(cmd.args()).containsExactly("DEST", "TEXT TO ADD");
    }

    @Test
    void testAppend() {
        TodoCommand cmd = TodoCommand.of("append NUMBER \"TEXT TO APPEND\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.APPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "\"TEXT TO APPEND\"");

        cmd = TodoCommand.of("app NUMBER \"TEXT TO APPEND\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.APPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "\"TEXT TO APPEND\"");

        // quotes optional
        cmd = TodoCommand.of("app NUMBER TEXT TO APPEND");
        assertThat(cmd.commandType()).isEqualTo(CommandType.APPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "TEXT TO APPEND");
    }

    @Test
    void testArchive() {
        final TodoCommand cmd = TodoCommand.of("archive");
        assertThat(cmd.commandType()).isEqualTo(CommandType.ARCHIVE);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testDel() {
        TodoCommand cmd = TodoCommand.of("del NUMBER TERM");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DEL);
        assertThat(cmd.args()).containsExactly("NUMBER", "TERM");

        cmd = TodoCommand.of("rm NUMBER TERM");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DEL);
        assertThat(cmd.args()).containsExactly("NUMBER", "TERM");

        cmd = TodoCommand.of("rm NUMBER");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DEL);
        assertThat(cmd.args()).containsExactly("NUMBER");
    }

    @Test
    void testDepri() {
        TodoCommand cmd = TodoCommand.of("depri NUMBER");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DEPRI);
        assertThat(cmd.args()).containsExactly("NUMBER");

        cmd = TodoCommand.of("dp NUMBER");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DEPRI);
        assertThat(cmd.args()).containsExactly("NUMBER");
    }

    @Test
    void testDo() {
        final TodoCommand cmd = TodoCommand.of("do NUMBER");
        assertThat(cmd.commandType()).isEqualTo(CommandType.DO);
        assertThat(cmd.args()).containsExactly("NUMBER");
    }

    @Test
    void testList() {
        TodoCommand cmd = TodoCommand.of("list TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LIST);
        assertThat(cmd.args()).containsExactly("TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("ls TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LIST);
        assertThat(cmd.args()).containsExactly("TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("ls");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LIST);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testListAll() {
        TodoCommand cmd = TodoCommand.of("listall TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTALL);
        assertThat(cmd.args()).containsExactly("TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("lsa TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTALL);
        assertThat(cmd.args()).containsExactly("TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("lsa");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTALL);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testListCon() {
        TodoCommand cmd = TodoCommand.of("listcon");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTCON);
        assertThat(cmd.args()).isEmpty();

        cmd = TodoCommand.of("lsc");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTCON);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testListFile() {
        TodoCommand cmd = TodoCommand.of("listfile SRC TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTFILE);
        assertThat(cmd.args()).containsExactly("SRC", "TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("lf SRC TERM1 TERM2 TERM3");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTFILE);
        assertThat(cmd.args()).containsExactly("SRC", "TERM1", "TERM2", "TERM3");

        cmd = TodoCommand.of("lf SRC");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTFILE);
        assertThat(cmd.args()).containsExactly("SRC");
    }

    @Test
    void testListPri() {
        TodoCommand cmd = TodoCommand.of("listpri PRIORITY");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTPRI);
        assertThat(cmd.args()).containsExactly("PRIORITY");

        cmd = TodoCommand.of("lsp PRIORITY");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTPRI);
        assertThat(cmd.args()).containsExactly("PRIORITY");

        cmd = TodoCommand.of("lsp");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTPRI);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testListProj() {
        TodoCommand cmd = TodoCommand.of("listproj");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTPROJ);
        assertThat(cmd.args()).isEmpty();

        cmd = TodoCommand.of("lsprj");
        assertThat(cmd.commandType()).isEqualTo(CommandType.LISTPROJ);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testMove() {
        TodoCommand cmd = TodoCommand.of("move NUMBER DEST SRC");
        assertThat(cmd.commandType()).isEqualTo(CommandType.MOVE);
        assertThat(cmd.args()).containsExactly("NUMBER", "DEST", "SRC");

        cmd = TodoCommand.of("mv NUMBER DEST SRC");
        assertThat(cmd.commandType()).isEqualTo(CommandType.MOVE);
        assertThat(cmd.args()).containsExactly("NUMBER", "DEST", "SRC");

        cmd = TodoCommand.of("mv NUMBER DEST");
        assertThat(cmd.commandType()).isEqualTo(CommandType.MOVE);
        assertThat(cmd.args()).containsExactly("NUMBER", "DEST");
    }

    @Test
    void testPrepend() {
        TodoCommand cmd = TodoCommand.of("prepend NUMBER \"TEXT TO PREPEND\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.PREPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "\"TEXT TO PREPEND\"");

        cmd = TodoCommand.of("prep NUMBER \"TEXT TO PREPEND\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.PREPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "\"TEXT TO PREPEND\"");

        // quotes optional
        cmd = TodoCommand.of("prep NUMBER TEXT TO PREPEND");
        assertThat(cmd.commandType()).isEqualTo(CommandType.PREPEND);
        assertThat(cmd.args()).containsExactly("NUMBER", "TEXT TO PREPEND");
    }

    @Test
    void testPri() {
        TodoCommand cmd = TodoCommand.of("pri NUMBER PRIORITY");
        assertThat(cmd.commandType()).isEqualTo(CommandType.PRI);
        assertThat(cmd.args()).containsExactly("NUMBER", "PRIORITY");

        cmd = TodoCommand.of("p NUMBER PRIORITY");
        assertThat(cmd.commandType()).isEqualTo(CommandType.PRI);
        assertThat(cmd.args()).containsExactly("NUMBER", "PRIORITY");
    }

    @Test
    void testReplace() {
        TodoCommand cmd = TodoCommand.of("replace NUMBER \"UPDATED TODO\"");
        assertThat(cmd.commandType()).isEqualTo(CommandType.REPLACE);
        assertThat(cmd.args()).containsExactly("NUMBER", "\"UPDATED TODO\"");

        // quotes optional
        cmd = TodoCommand.of("replace NUMBER UPDATED TODO");
        assertThat(cmd.commandType()).isEqualTo(CommandType.REPLACE);
        assertThat(cmd.args()).containsExactly("NUMBER", "UPDATED TODO");
    }

    @Test
    void testReport() {
        final TodoCommand cmd = TodoCommand.of("report");
        assertThat(cmd.commandType()).isEqualTo(CommandType.REPORT);
        assertThat(cmd.args()).isEmpty();
    }

    @Test
    void testHelp() {
        final TodoCommand cmd = TodoCommand.of("help");
        assertThat(cmd.commandType()).isEqualTo(CommandType.HELP);
        assertThat(cmd.args()).isEmpty();
    }
}
