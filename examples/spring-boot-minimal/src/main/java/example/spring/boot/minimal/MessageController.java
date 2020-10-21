package example.spring.boot.minimal;

import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.message.MessageService;
import com.github.delegacy.youngbot.web.AbstractMessageController;

@RestController
public class MessageController extends AbstractMessageController {
    public MessageController(MessageService messageService) {
        super(messageService);
    }
}
