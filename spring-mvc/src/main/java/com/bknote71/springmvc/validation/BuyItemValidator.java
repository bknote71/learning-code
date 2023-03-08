package com.bknote71.springmvc.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

//@Component
public class BuyItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return BuyItem.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BuyItem buyItem = (BuyItem) target;
        if (buyItem.getQuantity() == 0) {
            errors.rejectValue("quantity", "required", new Object[]{"100"}, "default_quantity");
        }

        if (buyItem.getPrice() == 0) {
            errors.rejectValue("price", "required", new Object[]{"100"}, "default_price");
        }

        // 특정 필드 예외가 아닌 전체 예외
        if (buyItem.getQuantity() != 0 && buyItem.getPrice() != 0) {
            if (buyItem.getQuantity() * buyItem.getPrice() < 3000) {
                errors.reject("orderPrice", new Object[]{3000}, "default_orderPrice");
            }
        }
    }
}
