package com.bknote71.BootBatch.example;

import com.bknote71.BootBatch.TestBatchExampleConfig;
import com.bknote71.BootBatch.model.product.Product;
import com.bknote71.BootBatch.model.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {DiscountProductJobConfiguration.class, TestBatchExampleConfig.class})
class DiscountProductJobConfigurationTest {

    @Autowired JobLauncherTestUtils jobLauncherTestUtils;
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

    @Test
    void 과연_될_것인가() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addString("requestDate", "2023-02-08")
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);

        List<Product> all = productRepository.findAll();
        all.forEach(System.out::println);
    }


}