package com.bknote71.BootBatch.model.sales;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sales {
    @Id @GeneratedValue
    private Long id;
    private Long productId;
    private Long amount;
    private LocalDate orderDate;

    public Sales(Long productId, Long amount, LocalDate orderDate) {
        this.productId = productId;
        this.amount = amount;
        this.orderDate = orderDate;
    }
}
