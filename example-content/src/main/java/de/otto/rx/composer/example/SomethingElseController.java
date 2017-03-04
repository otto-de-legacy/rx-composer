package de.otto.rx.composer.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SomethingElseController {

    @RequestMapping(path = "/somethingElse", produces = "text/html")
    @ResponseBody
    public String serveSomethingElse() {
        return "<h1>Some other content</h1>";
    }
}
