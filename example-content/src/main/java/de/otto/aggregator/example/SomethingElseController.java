package de.otto.aggregator.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SomethingElseController {

    @RequestMapping(path = "/somethingElse", produces = "text/html")
    @ResponseBody
    public String serveSomethingElse() {
        return "<h1>Service B</h1>";
    }
}
