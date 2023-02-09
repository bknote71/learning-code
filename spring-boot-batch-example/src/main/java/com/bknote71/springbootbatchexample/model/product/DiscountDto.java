package com.bknote71.BootBatch.model.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDto {
    private Long productId;
    private int rate;
    private LocalDate startDate;
    private LocalDate endDate;
}
