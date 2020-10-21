package example.spring.boot.minimal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class MinimalApplication {
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        SpringApplication.run(MinimalApplication.class, args);
    }
}
