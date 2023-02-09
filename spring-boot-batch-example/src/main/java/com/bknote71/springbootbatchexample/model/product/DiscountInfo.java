package com.bknote71.BootBatch.model.product;

import lombok.Data;

import javax.persistence.Embeddable;
import java.time.LocalDate;

@Data
@Embeddable
public class DiscountInfo {
    private int rate;
    private LocalDate startDate;
    private LocalDate endDate;
}
