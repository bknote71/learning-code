package com.bknote71.rdbms.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private Long memberId;
    private String contents;
    private LocalDate createdDate;
    private LocalDateTime createdAt;

    @Override public String toString() {
        return "Post{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", contents='" + contents + '\'' +
                ", createDate=" + createdDate +
                ", createAt=" + createdAt +
                '}';
    }
}
