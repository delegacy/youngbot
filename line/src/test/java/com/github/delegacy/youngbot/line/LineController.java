package com.github.delegacy.youngbot.line;

import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.parser.LineSignatureValidator;

@RestController
public class LineController extends AbstractLineController {
    public LineController(LineService lineService, LineSignatureValidator lineSignatureValidator) {
        super(lineService, lineSignatureValidator);
    }
}
