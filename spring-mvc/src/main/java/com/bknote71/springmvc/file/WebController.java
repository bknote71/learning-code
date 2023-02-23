package com.bknote71.springmvc.file;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @GetMapping("/")
    public @ResponseBody String fileHome() {
        return "file-home";
    }
}
