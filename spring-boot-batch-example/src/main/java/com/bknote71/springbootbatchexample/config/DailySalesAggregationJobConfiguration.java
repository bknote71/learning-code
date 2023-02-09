package com.bknote71.springbootbatchexample.config;

import com.bknote71.BootBatch.model.sales.Sales;
import com.bknote71.BootBatch.model.sales.SalesSum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DailySalesAggregationJobConfiguration {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    int chunkSize = 2;

    @Bean
    public Job dailySalesAggregationJob() {
        return jobBuilderFactory.get("dailySalesAggregationJob")
                .start(batchStep())
                .build();
    }

    @Bean
    public Step batchStep() {
        return stepBuilderFactory.get("batchStep")
                .<Sales, SalesSum>chunk(10)
                .reader(reader(null))
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<? extends Sales> reader(@Value("#{jobParameters[orderDate]}") String orderDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderDate", LocalDate.parse(orderDate, FORMATTER));

        String query = String.format("""
                select new %s(productId, s.orderDate, SUM(s.amount))
                from Sales s
                where s.orderDate = :orderDate
                group by s.productId
                """, SalesSum.class.getName());

        return new JpaPagingItemReaderBuilder<Sales>()
                .name("jpaPagingReader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString(query)
                .parameterValues(params)
                .build();
    }

    private ItemWriter<? super SalesSum> writer() {
        return new JpaItemWriterBuilder<SalesSum>()
                .entityManagerFactory(emf)
                .build();
    }

}
