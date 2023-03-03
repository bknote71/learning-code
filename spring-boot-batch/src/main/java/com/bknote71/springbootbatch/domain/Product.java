package com.bknote71.springbootbatch.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Product {

    private Long id;
    private String name;
    private int price;
    private LocalDateTime createdAt;

    public Product() {}

}
