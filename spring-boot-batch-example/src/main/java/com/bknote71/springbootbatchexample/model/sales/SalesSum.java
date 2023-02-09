package com.bknote71.BootBatch.model.sales;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SalesSum {
    @Id @GeneratedValue
    private Long id;
    private Long productId;
    private LocalDate orderDate;
    private Long sum;

    public SalesSum(Long productId, LocalDate orderDate, Long sum) {
        this.productId = productId;
        this.orderDate = orderDate;
        this.sum = sum;
    }

    @Override public String toString() {
        return "SalesAggregation{" +
                "id=" + id +
                ", productId=" + productId +
                ", orderDate=" + orderDate +
                ", sum=" + sum +
                '}';
    }
}
