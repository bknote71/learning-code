package com.bknote71.springmvc;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

import java.util.logging.Logger;

@Slf4j
@Controller
public class HomeController {

    @GetMapping("/")
    public @ResponseBody String index() {
        return "index";
    }

    @GetMapping("/re1")
    public @ResponseBody String returnResponseBody() {
        return "responseBody";
    }

    @GetMapping("/re2")
    public ResponseEntity<String> returnResponseEntity() {
        return ResponseEntity.ok("responseBody");
    }

    @Data
    static class Item {
        String name;
        int price;

        @JsonView({Item.class})
        String option;

        public Item() {
        }

        public Item(String item, int price) {
            this.name = item;
            this.price = price;
        }
    }

    /**
     * @JsonView{class...} - 원하는 필드만 필터링해서 요청을 받거나 응답할 수 있게 한다.
     * - 필드에 지정 / {파라미터 or 리턴타입}에 지정
     * - @JsonView에서 지정한 클래스가 적용이 안된 필드는 무시된다.(null or 기본값)
     * - 파라미터 or 리턴타입에 지정할 때 단 하나의 Class 만 지정이 가능하다. << JsonViewRequestBodyAdvice, JsonViewResponseBodyAdvice를 참조
     */
    @GetMapping("/item")
    public @ResponseBody Item item(@JsonView({Item.class}) @RequestBody Item item) {
        log.info("item={}", item);
        return new Item(item.name, item.price);
    }
}
