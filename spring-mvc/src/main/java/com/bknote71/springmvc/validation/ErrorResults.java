package com.bknote71.springmvc.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@NoArgsConstructor
public class ErrorResults {

    List<ErrorResult> errorResults = new ArrayList<>();

    public ErrorResults(Errors errors, MessageSource messageSource, Locale locale) {
        errors.getAllErrors().stream()
                .forEach(error -> add(error, messageSource.getMessage(error, locale)));
    }

    public void add(ObjectError error, String message) {
        errorResults.add(new ErrorResult(error, message));
    }

    @Data
    static class ErrorResult {
        private ObjectError error;
        private String message;

        public ErrorResult(ObjectError error, String message) {
            this.error = error;
            this.message = message;
        }
    }

}
