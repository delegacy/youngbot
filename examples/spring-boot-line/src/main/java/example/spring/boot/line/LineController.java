package example.spring.boot.line;

import org.springframework.web.bind.annotation.RestController;

import com.github.delegacy.youngbot.line.AbstractLineController;
import com.github.delegacy.youngbot.line.LineService;

import com.linecorp.bot.parser.LineSignatureValidator;

@RestController
public class LineController extends AbstractLineController {
    public LineController(LineService lineService, LineSignatureValidator lineSignatureValidator) {
        super(lineService, lineSignatureValidator);
    }
}
