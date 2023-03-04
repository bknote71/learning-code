package com.bknote71.springbootbatch.config.chunk.writer;

import com.bknote71.springbootbatch.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class JdbcBatchItemWriterJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    int chunkSize = 10;

    @Bean
    public Job jdbcBatchWriteJob() {
        return jobBuilderFactory.get("jdbcBatchWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcBatchWriteStep())
                .build();
    }

    @Bean
    public Step jdbcBatchWriteStep() {
        return stepBuilderFactory.get("jdbcBatchWriteStep")
                .<String, Post>chunk(chunkSize)
                .reader(new ItemReader<String>() {
                    int cnt = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return cnt++ < 30 ? "item" + cnt : null;
                    }
                })
                .processor(createPostProcessor())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    private ItemProcessor<? super String, Post> createPostProcessor() {
        return item -> new Post(item, item);
    }

    /**
     * 내부적으로 jdbcTemplate.batchUpdate(...) 호출!!
     * - 대용량 쓰기(insert, update, delete) 작업 가능
     */
    @Bean
    public JdbcBatchItemWriter<? super Post> jdbcBatchItemWriter() {
        String sql = """
                insert into post(title, content)
                values(:title, :content)
                """;
        return new JdbcBatchItemWriterBuilder<Post>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(BeanPropertySqlParameterSource::new) // == beanMapped()
                .build();
    }
}
