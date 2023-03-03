package com.bknote71.springbootbatch.config.chunk.reader.paging;


import com.bknote71.springbootbatch.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JdbcPagingReadJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    int chunkSize = 10;
    @Bean
    public Job jdbcPagingReadJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingReadJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcPagingReadStep())
                .build();
    }

    @Bean
    public Step jdbcPagingReadStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingReadStep")
                .<Post, Post>chunk(chunkSize)
                .reader(jdbcPagingItemReader(null))
                .writer(System.out::println)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<? extends Post> jdbcPagingItemReader(
            @Value("#{jobParameters[startDate]}") String requestDate
    ) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("requestDate", requestDate);
        return new JdbcPagingItemReaderBuilder<Post>()
                .name("jdbcPagingItemReader")
                .pageSize(chunkSize)
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .queryProvider(pagingQueryProvider())
                .parameterValues(params)
                .rowMapper(new BeanPropertyRowMapper<>(Post.class))
                .build();
    }

    @Bean
    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, title, content, created_at");
        queryProvider.setFromClause("from Post");
        queryProvider.setWhereClause("where created_at >= :requestDate");
        // sortKeys 는 필수
        // ++ LocalDateTime는 sortKey로 사용할 수 없다
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.DESCENDING);
        queryProvider.setSortKeys(sortKeys);
        return queryProvider.getObject();
    }
}
