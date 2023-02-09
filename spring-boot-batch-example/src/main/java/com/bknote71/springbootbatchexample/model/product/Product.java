package com.bknote71.BootBatch.model.product;

import lombok.*;

import javax.persistence.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    public enum ProductStatus {
        // 판매중, 할인중, 품절
        SALE, DISCOUNT, SOLD_OUT
    }

    @Id @GeneratedValue
    private Long id;
    private String name;
    private String categoryname;
    private String brandname;
    private int price;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    // on_sale : 세일중일 때의 정보
    @Embedded
    private DiscountInfo discountInfo;

    public Product(String name, String categoryname, String brandname, int price) {
        this.name = name;
        this.categoryname = categoryname;
        this.brandname = brandname;
        this.price = price;
        status = ProductStatus.SALE;
        discountInfo = new DiscountInfo();
    }

    public Product(String name, String categoryname, String brandname, int price, ProductStatus status) {
        this.name = name;
        this.categoryname = categoryname;
        this.brandname = brandname;
        this.price = price;
        this.status = status;
    }

    @Override public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", categoryname='" + categoryname + '\'' +
                ", brandname='" + brandname + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", discountInfo=" + discountInfo +
                '}';
    }
}
