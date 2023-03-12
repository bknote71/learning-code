package com.bknote71.springbootlogback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogbackController {

    private Logger logger = LoggerFactory.getLogger(LogbackController.class);

    @GetMapping("/log")
    public String printLog() {
        logger.error("logger error");
        logger.warn("logger warn");
        logger.info("logger info");
        logger.debug("logger debug");
        logger.trace("logger trace");
        return "success";
    }
}
