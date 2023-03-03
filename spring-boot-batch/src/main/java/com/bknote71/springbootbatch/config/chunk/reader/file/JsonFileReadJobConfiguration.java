package com.bknote71.springbootbatch.config.chunk.reader.file;

import com.bknote71.springbootbatch.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@RequiredArgsConstructor
@Configuration
public class JsonFileReadJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Value("${chunkSize:10}")
    private int chunkSize;

    @Bean
    public Job jsonReadJob() {
        return jobBuilderFactory.get("jsonReadJob")
                .incrementer(new RunIdIncrementer())
                .start(jsonReadStep())
                .build();
    }

    @Bean
    public Step jsonReadStep() {
        return stepBuilderFactory.get("jsonReadStep")
                .<Product, Product>chunk(chunkSize)
                .reader(jsonFileReader(null))
                .writer(System.out::println)
                .build();
    }

    @Bean
    @StepScope
    public JsonItemReader<? extends Product> jsonFileReader(
            @Value("#{jobParameters[requestDate]}") String requestDate
    ) {
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul")));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonJsonObjectReader<Product> jsonObjectReader = new JacksonJsonObjectReader<>(Product.class);
        jsonObjectReader.setMapper(objectMapper);
        return new JsonItemReaderBuilder<Product>()
                .name("jsonFileItemReader")
                .jsonObjectReader(jsonObjectReader)
                .resource(new ClassPathResource("products/products-" + requestDate + ".json"))
                .build();
    }
}
