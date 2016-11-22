package de.otto.rx.composer.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @RequestMapping(path = "/hello", produces = "text/html")
    @ResponseBody
    public String sayHello(final @RequestParam(defaultValue = "World") String name) {
        return "<h1>Hello " + name + "!</h1>";
    }
}
