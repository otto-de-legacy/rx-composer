package de.otto.rx.composer.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @RequestMapping(path = "/hello", produces = "text/html")
    @ResponseBody
    public String sayHello() {
        return "<h1>Hello World!</h1>";
    }
}
