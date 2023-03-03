package com.bknote71.springbootbatch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptions {
    private Long productId;
    private List<String> options = new ArrayList<>();
}
