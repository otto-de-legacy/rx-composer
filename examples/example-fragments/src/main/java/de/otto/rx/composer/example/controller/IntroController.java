package de.otto.rx.composer.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IntroController {

    @RequestMapping(path = "/intro", produces = "text/html")
    public String getIntro() {
        return "intro";
    }
}
