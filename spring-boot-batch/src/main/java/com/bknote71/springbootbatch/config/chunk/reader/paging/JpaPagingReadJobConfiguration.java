package com.bknote71.springbootbatch.config.chunk.reader.paging;

import com.bknote71.springbootbatch.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Configuration
public class JpaPagingReadJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    int chunkSize = 10;
    @Bean
    public Job jpaPagingReadJob() {
        return jobBuilderFactory.get("jpaPagingReadJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaPagingReadStep())
                .build();
    }

    @Bean
    public Step jpaPagingReadStep() {
        return stepBuilderFactory.get("jpaPagingReadStep")
                .<Post, Post>chunk(chunkSize)
                .reader(jpaPagingItemReader())
                .writer(System.out::println)
                .build();
    }

    @Bean
    public ItemReader<? extends Post> jpaPagingItemReader() {
        String jpql = """
                select p 
                from Post p 
                """;
        return new JpaPagingItemReaderBuilder<Post>()
                .name("jpaPagingItemReader")
                .pageSize(chunkSize)
                .entityManagerFactory(emf)
                .queryString(jpql)
                .build();
    }

}
