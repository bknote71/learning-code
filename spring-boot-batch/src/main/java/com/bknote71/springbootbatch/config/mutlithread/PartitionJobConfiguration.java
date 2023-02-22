package com.bknote71.springbootbatch.config.mutlithread;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

@RequiredArgsConstructor
@Configuration
public class PartitionJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public Job partitionJob() {
        return jobBuilderFactory.get("partitionJob")
                .start(masterStep())
                .build();
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep")
                .<String, String>chunk(10)
                .reader(reader(null))
                .writer(System.out::println)
                .build();
    }

    @Bean
    @StepScope // 프록시 reader 를 사용한다.
    public ItemReader<? extends String> reader(
            @Value("#{stepExecutionContext['key']}") String key) {
        CustomItemReader customItemReader = new CustomItemReader(integerHolder());
        return customItemReader;
    }

    @Bean
    public IntegerHolder integerHolder() {
        return new IntegerHolder();
    }

    @Bean
    public Partitioner partitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> result = new HashMap<>();
                for (int i = 0; i < gridSize; ++i) {
                    ExecutionContext context = new ExecutionContext();
                    result.put("partition" + i, context);
                    context.putString("key", i + "thGrid");
                }
                return result;
            }
        };
    }

    static class CustomItemReader implements ItemReader<String> {
        int i = 0;
        Object lock = new Object();
        IntegerHolder integerHolder;

        public CustomItemReader(IntegerHolder integerHolder) {
            this.integerHolder = integerHolder;
        }

        @Override
        public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
            synchronized (lock) {
                int ret = integerHolder.getAndIncrement();
                return ret < 30 ? "ret-" + ret : null;
            }
        }
    }

    @Data
    static class IntegerHolder {
        int i = 0;
        synchronized int getAndIncrement() {
            return i++;
        }
    }
}
