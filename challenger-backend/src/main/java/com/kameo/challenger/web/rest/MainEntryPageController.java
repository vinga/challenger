package com.kameo.challenger.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainEntryPageController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

}
