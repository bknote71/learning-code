package com.bknote71.springmvc.validation;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostValidationTest {

    @Test
    void test() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Post post = new Post("", "");
        Set<ConstraintViolation<Post>> violationSet = validator.validate(post);
        for (ConstraintViolation<Post> violation : violationSet) {
            System.out.println(violation);
            System.out.println(violation.getMessage());
        }
    }


}