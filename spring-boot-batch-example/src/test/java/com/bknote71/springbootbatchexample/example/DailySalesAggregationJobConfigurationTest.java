package com.bknote71.BootBatch.example;

import com.bknote71.BootBatch.TestBatchExampleConfig;
import com.bknote71.BootBatch.model.sales.Sales;
import com.bknote71.BootBatch.model.sales.SalesSum;
import com.bknote71.BootBatch.model.sales.SalesSumRepository;
import com.bknote71.BootBatch.model.sales.SalesRepository;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {DailySalesAggregationJobConfiguration.class, TestBatchExampleConfig.class})
class DailySalesAggregationJobConfigurationTest {

    @Autowired JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired SalesRepository salesRepository;
    @Autowired SalesSumRepository salesSumRepository;

    @After
    void down() {
        salesRepository.deleteAllInBatch();
        salesSumRepository.deleteAllInBatch();
    }


    @Test
    void testNotNull() {
        assertThat(jobLauncherTestUtils).isNotNull();
        assertThat(salesRepository).isNotNull();
        assertThat(salesSumRepository).isNotNull();
    }

    @Test
    void daily_sum_aggregation_test() throws Exception {
        LocalDate date = LocalDate.of(2023, 2, 8);
        salesRepository.save(new Sales(1L, 100L, date));
        salesRepository.save(new Sales(1L, 10L, date));
        salesRepository.save(new Sales(2L, 1000L, date));
        salesRepository.save(new Sales(2L, 20L, date));
        salesRepository.save(new Sales(3L, 30L, date));
        salesRepository.save(new Sales(3L, 400L, date));
        salesRepository.save(new Sales(4L, 500L, date));
        salesRepository.save(new Sales(4L, 600L, date));

        JobParameters params = new JobParametersBuilder()
                .addString("orderDate", "2023-02-08")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<SalesSum> salesSumList = salesSumRepository.findAll();
        assertThat(salesSumList.size()).isEqualTo(4);
        assertThat(salesSumList.get(0).getOrderDate()).isEqualTo(date);
        assertThat(salesSumList.get(0).getSum()).isEqualTo(110);
    }


}