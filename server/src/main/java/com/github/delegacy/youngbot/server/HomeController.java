package com.github.delegacy.youngbot.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public Mono<String> ok() {
        return Mono.just("OK, this is Young Bot.");
    }
}
