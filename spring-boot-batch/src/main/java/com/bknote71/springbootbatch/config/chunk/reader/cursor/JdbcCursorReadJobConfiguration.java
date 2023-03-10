package com.bknote71.springbootbatch.config.chunk.reader.cursor;

import com.bknote71.springbootbatch.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Configuration
public class JdbcCursorReadJobConfiguration {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    int chunkSize = 10;

    @Bean
    public Job jdbcCursorReadJob() {
        return jobBuilderFactory.get("jdbcCursorReadJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcCursorReadStep())
                .build();
    }

    @Bean
    public Step jdbcCursorReadStep() {
        return stepBuilderFactory.get("jdbcCursorReadStep")
                .<Post, Post>chunk(chunkSize)
                .reader(jdbcCursorItemReader(null, null))
                .writer(items -> {
                    System.out.println(items);
                })
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<? extends Post> jdbcCursorItemReader(
            @Value("#{jobParameters[startDate]}") String startDate,
            @Value("#{jobParameters[endDate]}") String endDate
    ) {
        // ??????: mysql????????? datetime(LocalDateTime) ??? '2023-03-02' ?????? date??? ?????? ??????
        // 2023-03-02 ??? date?
        String originalSql = """
                select * 
                from Post
                where created_at between ? and ?
                """;

        return new JdbcCursorItemReaderBuilder<Post>()
                .name("jdbcCursorItemReader")
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .sql(originalSql)
                .queryArguments(startDate, endDate)
                .beanRowMapper(Post.class)
                .build();
    }

}
