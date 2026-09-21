package com.plytagalvisx.roombooking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Value("${spring.application.name}")
    private String appName;

    // When a web request goes to the root "/" endpoint of our website then the method index() gets called
    @RequestMapping("/")
    public String index() {
        System.out.println("appName: "+ appName);
        return "index.html"; // We return the view to the browser
    }

    // We can create several methods each with a different endpoint here e.g., "/about", "/contact", etc.
}
