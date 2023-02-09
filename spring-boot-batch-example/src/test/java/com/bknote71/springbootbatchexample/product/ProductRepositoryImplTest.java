package com.bknote71.BootBatch.model.product;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductRepositoryImplTest {

    @Autowired ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        Product p1 = new Product("p1", "c1", "b1", 1000);
        Product p2 = new Product("p2", "c2", "b2", 100);
        Product p3 = new Product("p3", "c3", "b3", 10000);
        Product p4 = new Product("p4", "c4", "b4", 500);

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
    }

    @After
    void down() {
        productRepository.deleteAllInBatch();
    }

    @Test
    void save() {
        List<Product> products = productRepository.findAll();
        assertThat(products.size()).isEqualTo(4);
        assertThat(products.get(0).getName()).isEqualTo("p1");
    }

    @Test
    void test() {
        ProductSearchCondition cond = new ProductSearchCondition(10, 600, null, null, null, Product.ProductStatus.SALE);
        List<Product> products = productRepository.findBySearchCondition(cond);
        assertThat(products.size()).isEqualTo(2);
    }

}