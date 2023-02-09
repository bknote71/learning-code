package com.bknote71.rdbms.page;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class CursorPage<T> {
    private List<T> contents;
    private Long nextId;

    public CursorPage(List<T> results, Long nextId) {
        this.contents = results;
        this.nextId = nextId;
    }
}
