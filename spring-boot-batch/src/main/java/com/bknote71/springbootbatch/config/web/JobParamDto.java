package com.bknote71.springbootbatch.config.web;

import lombok.Data;

import java.util.Properties;

@Data
public class JobParamDto {
    String requestDate;

    public String toParameterString() {
        return String.format("""
                requestDate=%s
                """, requestDate);
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        properties.put("requestDate", requestDate);
        return properties;
    }
}
