package com.bknote71.springbootlogback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class LombokLogBackController {

    @GetMapping("/log2")
    public String printLog() {
        log.error("logger error");
        log.warn("logger warn");
        log.info("logger info");
        log.debug("logger debug");
        log.trace("logger trace");
        return "success";
    }
}
