package com.bknote71.springmvc.validation;

import lombok.Data;
import org.hibernate.validator.constraints.Range; // 하이버네이트 <<

import javax.validation.constraints.NotBlank; // java 표준
import javax.validation.constraints.NotNull;

@Data
public class Post {
    @NotBlank
    private String title;

    @NotNull
    @Range(min = 1, max = 1000) // 1000자 이내 + null 을 막아주지는 않는다.
    private String content;

    public Post() {
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
