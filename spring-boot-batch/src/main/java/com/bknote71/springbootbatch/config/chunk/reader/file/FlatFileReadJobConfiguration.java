package com.bknote71.springbootbatch.config.chunk.reader.file;

import com.bknote71.springbootbatch.domain.product.Product;
import com.bknote71.springbootbatch.domain.product.ProductOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class FlatFileReadJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Value("${chunkSize:10}")
    private int chunkSize;

    @Bean
    public Job fileReadJob() {
        return jobBuilderFactory.get("fileReadJob")
                .incrementer(new RunIdIncrementer()) // 임시로 << test 용
                .start(fileReadStep())
                .next(optionsReadStep())
                .build();
    }

    @Bean
    public Step fileReadStep() {
        return stepBuilderFactory.get("fileReadStep")
                .<Product, Product>chunk(chunkSize)
                .reader(fileReader(null))
                .writer(System.out::println)
                .build();
    }

    // list 읽기
    @Bean
    public Step optionsReadStep() {
        return stepBuilderFactory.get("optionsReadStep")
                .<ProductOptions, ProductOptions>chunk(chunkSize)
                .reader(optionsReader(null))
                .writer(System.out::println)
                .build();
    }

    /**
     * beanWrapperFieldSetMapper 조건:
     * 1. No Args Constructor
     * 2. Setter
     *
     * + FlatFileItemReader, ... 은 name 설정해야한다. (saveState가 true일 때, 기본값)
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Product> fileReader(
            @Value("#{jobParameters[requestDate]}") String requestDate
    ) {
        return new FlatFileItemReaderBuilder<Product>()
                .name("flatFileItemReader")
                .resource(new ClassPathResource("products/products-" + requestDate + ".csv"))
                .delimited().delimiter(",")
                .names("id", "name", "price")
                .targetType(Product.class)
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .build();
    }

    /**
     * .delimited().delimiter()
     * - names() 를 지정해야 하는데 필드 개수가 정확히 딱 떨어져야 할 때 사용할 수 있다.
     * - 필드 개수가 가변적인 리스트를 받을 수는 없다.
     *
     * 즉 리스트를 읽고 싶을 때는 delimited() api를 사용할 수 없으므로 lineTokenizer도 사용할 수 없고 당연히 fieldSetMapper 도 사용할 수 없게된다.
     * --> lineMapper를 직접 설정해야 한다.
     *
     */
    @Bean
    @StepScope
    public FlatFileItemReader<? extends ProductOptions> optionsReader(
            @Value("#{jobParameters[requestDate]}") String requestDate
    ) {
        return new FlatFileItemReaderBuilder<ProductOptions>()
                .name("productOptionsReader")
                .resource(new ClassPathResource("products/options/options-" + requestDate + ".csv"))
                .linesToSkip(1)
                .lineMapper((line, lineNumber) -> {
                    String[] productOptions = line.split(",");
                    Long productId = Long.valueOf(productOptions[0]);
                    List<String> options = new ArrayList<>();
                    for (int i = 1; i < productOptions.length; ++i) {
                        options.add(productOptions[i]);
                    }
                    return new ProductOptions(productId, options);
                })
                .build();
    }
}
