package com.bknote71.springmvc.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Locale;

@Slf4j
@Controller
public class ValidationController {

    @Autowired MessageSource messageSource;

    @PostMapping("/buy1")
    public ResponseEntity<?> buy1(@ModelAttribute BuyItem buyItem, BindingResult bindingResult) {
        // validate logic v1

        // 필드 예외 << 특정 필드에 한정해서 발생하는 예외
        if (buyItem.getQuantity() == 0) {
            bindingResult.addError(new FieldError("buyItem", "quantity", "수량은 0개 이상이어야 합니다."));
        }

        if (buyItem.getPrice() == 0) {
            bindingResult.addError(new FieldError("buyItem", "price", "가격은 0원 이상이어야 합니다."));
        }

        // 특정 필드 예외가 아닌 전체 예외
        if (buyItem.getQuantity() != 0 && buyItem.getPrice() != 0) {
            if (buyItem.getQuantity() * buyItem.getPrice() < 3000) {
                bindingResult.addError(new ObjectError("buyItem", "최소 주문 금액은 3000원 이상이어야 합니다."));
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("error count={}", bindingResult.getErrorCount());
            log.info("error target={}", bindingResult.getTarget());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        return ResponseEntity.ok("success");
    }

    @PostMapping("/buy2")
    public ResponseEntity<?> buy2(@ModelAttribute BuyItem buyItem, BindingResult bindingResult) {
        // v2
        // 필드 예외 << 특정 필드에 한정해서 발생하는 예외
        // errorArgs: '0'을 넣을때는 문자열로 <<
        if (buyItem.getQuantity() == 0) {
            bindingResult.addError(new FieldError("buyItem", "quantity",
                    buyItem.getQuantity(), false, new String[]{"required.item.quantity"}, new Object[]{"0"}, "default_quantity"));
        }

        if (buyItem.getPrice() == 0) {
            bindingResult.addError(new FieldError("buyItem", "price",
                    buyItem.getPrice(), false, new String[]{"required.item.price"}, new Object[]{"0"}, "default_price"));
        }

        // 특정 필드 예외가 아닌 전체 예외
        if (buyItem.getQuantity() != 0 && buyItem.getPrice() != 0) {
            if (buyItem.getQuantity() * buyItem.getPrice() < 3000) {
                bindingResult.addError(new ObjectError("buyItem",
                        new String[]{"required.item.orderPrice"}, new Object[]{3000}, "default_orderPrice"));
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors?={}", bindingResult);
            log.info("object name?={}", bindingResult.getObjectName());
            log.info("target?={}", bindingResult.getTarget());
            // template engine이 아닌 json으로 bindingResult 응답하기
            // messageSource 직접 이용
            // + FieldError, ObjectError 는 MessageSourceResolvable을 구현하고 있다.
            ErrorResults errorResults = new ErrorResults();
            bindingResult.getAllErrors().stream()
                    .forEach(error -> errorResults.add(error, messageSource.getMessage(error, Locale.getDefault())));
            // 참고: 메시지 국제화?
            // Local.KOREA: messages_ko_KR.properties 파일 선택
            // ++ 반드시 messages.properties 라는 basefile이 있어야 _ko_KR 파일도 선택될 수 있다.
            return ResponseEntity.badRequest().body(errorResults);
        }
        return ResponseEntity.ok("success");
    }

    @PostMapping("/buy3")
    public ResponseEntity<?> buy3(@ModelAttribute BuyItem buyItem, BindingResult bindingResult) {
        // v3
        // rejectValue: 필드 예외 << 특정 필드에 한정해서 발생하는 예외
        if (buyItem.getQuantity() == 0) {
            bindingResult.rejectValue("quantity", "required", new Object[]{"0"}, "default_quantity");
        }

        if (buyItem.getPrice() == 0) {
            bindingResult.rejectValue("price", "required", new Object[]{ "zero" }, "default_price");
        }

        // 특정 필드 예외가 아닌 전체 예외
        if (buyItem.getQuantity() != 0 && buyItem.getPrice() != 0) {
            if (buyItem.getQuantity() * buyItem.getPrice() < 3000) {
                bindingResult.reject("orderPrice", new Object[]{3000}, "default_quantity");
            }
        }
        // 참고: 스프링이 자동으로 추가해주는 errorCode: typeMismatch with (objectName, field)


        if (bindingResult.hasErrors()) {
            // object name == class name (앞글자 소문자)
            log.info("object name?={}", bindingResult.getObjectName());
            // 참고: 해당 errorCode가 없으면 NoSuchMessageException <<
            // bindingResult.reject("reject");
            ErrorResults errorResults = new ErrorResults(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.badRequest().body(errorResults);
        }
        return ResponseEntity.ok("success");
    }

    @GetMapping("/buy4")
    public String buy4Page(@ModelAttribute BuyItem buyItem) {
        System.out.println("get buy4 page");
        return "buy";
    }

    @PostMapping("/buy4")
    public String buy4(@ModelAttribute BuyItem buyItem, BindingResult bindingResult) {
        // v4: with thymeleaf
        if (buyItem.getQuantity() == 0) {
            bindingResult.rejectValue("quantity", "required", new Object[]{"100"}, "default_quantity");
        }

        if (buyItem.getPrice() == 0) {
            bindingResult.rejectValue("price", "required", new Object[]{"100"}, "default_price");
        }

        // 특정 필드 예외가 아닌 전체 예외
        if (buyItem.getQuantity() != 0 && buyItem.getPrice() != 0) {
            if (buyItem.getQuantity() * buyItem.getPrice() < 3000) {
                bindingResult.reject("orderPrice", new Object[]{3000}, "default_orderPrice");
            }
        }
        if (bindingResult.hasErrors()) {
            log.info("errors?={}", bindingResult);
            return "buy";
        }
        log.info("success");
        return "redirect:/";
    }

    @InitBinder
    // WebDataBinder 는 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함한다.
    // 해당 컨트롤러에서만 설정
    // 글로벌: WebMvcConfigurer overrides getValidator --> return new MyValidator()
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new BuyItemValidator());
    }

    @PostMapping("/buy5")
    public ResponseEntity<?> buy5(@Validated @ModelAttribute BuyItem buyItem, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("errors?={}", bindingResult);
            ErrorResults errorResults = new ErrorResults(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.badRequest().body(errorResults);
        }
        return ResponseEntity.ok("success");
    }
}
