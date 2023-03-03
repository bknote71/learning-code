package com.bknote71.springbootbatch.config.chunk.reader.cursor;

import com.bknote71.springbootbatch.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Configuration
public class JpaCursorReadJobConfiguration {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    int chunkSize = 10;

    @Bean
    public Job jpaCursorReadJob() {
        return jobBuilderFactory.get("jpaCursorReadJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaCursorReadStep())
                .build();
    }

    @Bean
    public Step jpaCursorReadStep() {
        return stepBuilderFactory.get("jpaCursorReadStep")
                .<Post, Post>chunk(chunkSize)
                .reader(jpaCursorItemReader())
                .writer(System.out::println)
                .build();
    }

    @Bean
    public ItemReader<? extends Post> jpaCursorItemReader() {
        return new JpaCursorItemReaderBuilder<Post>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(emf)
                .queryString("select p from Post p")
                .maxItemCount(20)
                .build();
    }
}
