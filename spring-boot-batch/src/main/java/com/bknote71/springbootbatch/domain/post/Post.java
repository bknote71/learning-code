package com.bknote71.springbootbatch.domain.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post {

    @Id @GeneratedValue
    private Long id;

    private String title;
    private String content;
    private LocalDateTime created_at;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
