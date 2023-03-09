package com.bknote71.springmvc.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Locale;

@Slf4j
@Controller
public class PostValidationController {

    @Autowired MessageSource messageSource;

    /**
     * 스프링부트가 스타터-validation 라이브러리 추가시
     * LocalValidatorFactoryBean (Bean Validator)을 글로벌 Validator로 등록
     * - @NotNull 같은 애노테이션으로 검증 수행
     * - @Valid, @Validated 만 적용하면 된다.
     * - 자동으로 error bindingResult에 바인딩해줌!!
     * 참고: WebMvcConfigurer 에서 getValidator() 를 오버라이드하여 글로벌 Validator 를 지정해논 상태이면
     * LocalValidatorFactoryBean 가 글로벌로 등록되지 않는다.
     *
     * 검증 순서
     * 1. 타입 변환
     * - @ModelAttribute 는 필드 단위로 바인딩 << (중요)
     * - 실패하면? FieldError
     * 2. Validator 적용
     *
     */
    @PostMapping("/post")
    public ResponseEntity<?> post(@Valid @ModelAttribute Post post, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("errors?={}", bindingResult);
            ErrorResults errorResults = new ErrorResults(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.badRequest().body(errorResults);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * @RequestBody 에 적용하기
     * - JSON 요청 처리
     *
     * @ModelAttribute 와는 다른 차이 (중요)
     * - @ModelAttribute 는 필드 단위로 바인딩한다.
     * - 즉 특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩 + Validator 로 검증 가능
     * - @RequestBody 는 객체 단위로 바인딩하기 때문에 JSON 데이터를 객체로 바인딩하지 못하면 Validator 를 적용할 수 없다.
     * - 중요: 즉 "타입 예외"같은 예외가 발생하면 @RequestBody 에서는 Validator 적용 없이 오류가 발생한다!!
     */
    @PostMapping("/post-json")
    public ResponseEntity<?> postJson(@Valid @RequestBody Post post, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("errors?={}", bindingResult);
            ErrorResults errorResults = new ErrorResults(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.badRequest().body(errorResults);
        }

        return ResponseEntity.ok("success");
    }
}
