package com.bknote71.springbootbatchexample.config;

import com.bknote71.BootBatch.model.product.DiscountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DiscountProductJobConfiguration {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    int chunkSize = 2;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("discountProductJob")
                .start(dicountStep())
                .build();
    }

    @Bean
    public Step dicountStep() {
        return stepBuilderFactory.get("discountStep")
                .<DiscountDto, DiscountDto>chunk(chunkSize)
                .reader(fileItemReader(null))
//                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<DiscountDto> fileItemReader(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new FlatFileItemReaderBuilder<DiscountDto>()
                .name("fileItemReader")
                .resource(new ClassPathResource("discountlist/discount-" + requestDate))
                .delimited().delimiter(",")
                .names("productId", "rate", "startDate", "endDate")
                .fieldSetMapper(fs -> new DiscountDto(
                        fs.readLong("productId"),
                        fs.readInt("rate"),
                        LocalDate.parse(fs.readString("startDate"), FORMATTER),
                        LocalDate.parse(fs.readString("endDate"), FORMATTER)))
                .linesToSkip(1)
                .build();
    }

    @Bean
    public ItemWriter<? super DiscountDto> writer() {
        String sql = """
                UPDATE product
                SET rate = :rate, start_date = :startDate, end_date = :endDate, status = 'DISCOUNT'
                where id = :productId
                """;

        return new JdbcBatchItemWriterBuilder<DiscountDto>()
                .beanMapped()
                .dataSource(dataSource)
                .sql(sql)
                .assertUpdates(false)
                .build();
    }
}
