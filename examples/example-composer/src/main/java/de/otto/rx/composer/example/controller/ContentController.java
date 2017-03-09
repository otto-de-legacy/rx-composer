package de.otto.rx.composer.example.controller;

import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.tracer.TracerBuilder.loggingStatisticsTracer;

/**
 * Fetches contents from some different "microservices" and renders the contents using a thymeleaf template.
 */
@Controller
public class ContentController {

    private final Page page;

    @Autowired
    public ContentController(Page page) {
        this.page = page;
    }

    @RequestMapping("/")
    public ModelAndView getContent(final @RequestParam(defaultValue = "false") boolean debugMode) {
        final Contents contents = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
        ModelAndView modelAndView = new ModelAndView("content");
        modelAndView.addObject("contents", contents);
        modelAndView.addObject("debugMode", debugMode);
        return modelAndView;
    }

}
