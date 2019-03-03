package com.github.delegacy.youngbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.reactivex.Single;

@RestController
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public Single<String> ok() {
        return Single.just("OK, this is Young Bot.");
    }
}
