package example.spring.boot.slack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class SlackApplication {
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        SpringApplication.run(SlackApplication.class, args);
    }
}
