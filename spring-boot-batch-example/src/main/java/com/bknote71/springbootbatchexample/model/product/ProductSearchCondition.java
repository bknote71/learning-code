package com.bknote71.BootBatch.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCondition {
    private Integer goePrice;
    private Integer loePrice;
    private String name;
    private String categoryname;
    private String brandname;
    private Product.ProductStatus status;
}
