package de.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WebController {
    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard/index.html";
    }
}