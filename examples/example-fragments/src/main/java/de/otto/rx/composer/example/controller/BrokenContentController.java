package de.otto.rx.composer.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class BrokenContentController {

    @RequestMapping(path = "/somethingBroken", produces = "text/html")
    @ResponseBody
    public void serveSomeError(final HttpServletResponse response) throws IOException {
        response.sendError(500, "This service is simply returning HTTP 500");
    }
}
