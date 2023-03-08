package com.bknote71.springmvc.validation;

import lombok.Data;

@Data
public class BuyItem {
    private String name;
    private int quantity;
    private int price;
}
