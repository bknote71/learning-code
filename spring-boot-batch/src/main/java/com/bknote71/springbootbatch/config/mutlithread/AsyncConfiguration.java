package com.bknote71.springbootbatch.config.mutlithread;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.Future;

// AsyncItemProcessor 를 활용하기 위해서는 spring-batch-integration 의존성을 추가해야한다.
@RequiredArgsConstructor
@Configuration
public class AsyncConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job asyncJob() {
        return jobBuilderFactory.get("asyncJob")
                .start(asyncStep())
                .build();
    }

    private Step asyncStep() {
        return stepBuilderFactory.get("asyncStep")
                .<Integer, Future<Integer>>chunk(10)
                .reader(new ItemReader<Integer>() {
                    int i = 0;
                    @Override public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return i++ < 30 ? i : null;
                    }
                })
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    private ItemProcessor<Integer, Future<Integer>> asyncItemProcessor() {
        AsyncItemProcessor<Integer, Integer> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(new SleepItemProcessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return asyncItemProcessor;
    }


    private ItemWriter<Future<Integer>> asyncItemWriter() {
        AsyncItemWriter<Integer> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(System.out::println);
        return asyncItemWriter;
    }

    static class SleepItemProcessor implements ItemProcessor<Integer, Integer> {

        @Override public Integer process(Integer item) throws Exception {
            Thread.sleep(1000);
            return item;
        }
    }


}
