package com.github.delegacy.youngbot.server.message.handler.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TodoTaskTest {
    @Test
    @DisplayName("Incomplete Tasks General")
    void testParse_incompleteTasks_general() {
        TodoTask todoLine = TodoTask.of("(A) Thank Mom for the meatballs @phone");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("Thank Mom for the meatballs @phone");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).containsExactly("@phone");
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(B) Schedule Goodwill pickup +GarageSale @phone");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('B'));
        assertThat(todoLine.content()).isEqualTo("Schedule Goodwill pickup +GarageSale @phone");
        assertThat(todoLine.projects()).containsExactly("+GarageSale");
        assertThat(todoLine.contexts()).containsExactly("@phone");
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("Post signs around the neighborhood +GarageSale");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Post signs around the neighborhood +GarageSale");
        assertThat(todoLine.projects()).containsExactly("+GarageSale");
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("@GroceryStore Eskimo pies");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("@GroceryStore Eskimo pies");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).containsExactly("@GroceryStore");
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Incomplete Tasks Rule 1: If priority exists, it ALWAYS appears first.")
    void testParse_incompleteTasks_rule1() {
        TodoTask todoLine = TodoTask.of("(A) Call Mom");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("Call Mom");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("Really gotta call Mom (A) @phone @someday");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Really gotta call Mom (A) @phone @someday");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).containsExactly("@phone", "@someday");
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(b) Get back to the boss");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("(b) Get back to the boss");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(B)->Submit TPS report");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("(B)->Submit TPS report");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Incomplete Tasks Rule 2: A task's creation date may optionally appear directly after priority and a space.")
    void testParse_incompleteTasks_rule2() {
        TodoTask todoLine = TodoTask.of("2011-03-02 Document +TodoTxt task format");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).contains(LocalDate.of(2011, 3, 2));
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Document +TodoTxt task format");
        assertThat(todoLine.projects()).containsExactly("+TodoTxt");
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(A) 2011-03-02 Call Mom");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).contains(LocalDate.of(2011, 3, 2));
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("Call Mom");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(A) Call Mom 2011-03-02");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("Call Mom 2011-03-02");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Incomplete Tasks Rule 3: Contexts and Projects may appear anywhere in the line after priority/prepended date.")
    void testParse_incompleteTasks_rule3() {
        TodoTask todoLine = TodoTask.of("(A) Call Mom +Family +PeaceLoveAndHappiness @iphone @phone");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("Call Mom +Family +PeaceLoveAndHappiness @iphone @phone");
        assertThat(todoLine.projects()).containsExactly("+Family", "+PeaceLoveAndHappiness");
        assertThat(todoLine.contexts()).containsExactly("@iphone", "@phone");
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("Email SoAndSo at soandso@example.com");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Email SoAndSo at soandso@example.com");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("Learn how to add 2+2");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Learn how to add 2+2");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Complete Tasks Rule 1: A completed task starts with an lowercase x character (x).")
    void testParse_completeTasks_rule1() {
        TodoTask todoLine = TodoTask.of("x 2011-03-03 Call Mom");
        assertThat(todoLine.isCompleted()).isTrue();
        assertThat(todoLine.completionDate()).contains(LocalDate.of(2011, 3, 3));
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Call Mom");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("xylophone lesson");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("xylophone lesson");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("X 2012-01-01 Make resolutions");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("X 2012-01-01 Make resolutions");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();

        todoLine = TodoTask.of("(A) x Find ticket prices");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).contains(Character.valueOf('A'));
        assertThat(todoLine.content()).isEqualTo("x Find ticket prices");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Complete Tasks Rule 2: The date of completion appears directly after the x, separated by a space.")
    void testParse_completeTasks_rule2() {
        final TodoTask todoLine =
                TodoTask.of("x 2011-03-02 2011-03-01 Review Tim's pull request +TodoTxtTouch @github");
        assertThat(todoLine.isCompleted()).isTrue();
        assertThat(todoLine.completionDate()).contains(LocalDate.of(2011, 3, 2));
        assertThat(todoLine.creationDate()).contains(LocalDate.of(2011, 3, 1));
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Review Tim's pull request +TodoTxtTouch @github");
        assertThat(todoLine.projects()).containsExactly("+TodoTxtTouch");
        assertThat(todoLine.contexts()).containsExactly("@github");
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    @DisplayName("Additional File Format Definitions")
    void testParse_additional() {
        TodoTask todoLine = TodoTask.of("Call Mom due:2010-01-02");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Call Mom due:2010-01-02");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).containsExactly(entry("due", "2010-01-02"));

        todoLine = TodoTask.of("Call Mom du:e:2010-01-02");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("Call Mom du:e:2010-01-02");
        assertThat(todoLine.projects()).isEmpty();
        assertThat(todoLine.contexts()).isEmpty();
        assertThat(todoLine.tags()).isEmpty();
    }

    @Test
    void testParse_international() {
        final TodoTask todoLine = TodoTask.of("어머니에게 전화하기 +가족 @전화 주기:매일");
        assertThat(todoLine.isCompleted()).isFalse();
        assertThat(todoLine.completionDate()).isEmpty();
        assertThat(todoLine.creationDate()).isEmpty();
        assertThat(todoLine.priority()).isEmpty();
        assertThat(todoLine.content()).isEqualTo("어머니에게 전화하기 +가족 @전화 주기:매일");
        assertThat(todoLine.projects()).containsExactly("+가족");
        assertThat(todoLine.contexts()).containsExactly("@전화");
        assertThat(todoLine.tags()).containsExactly(entry("주기", "매일"));
    }
}
