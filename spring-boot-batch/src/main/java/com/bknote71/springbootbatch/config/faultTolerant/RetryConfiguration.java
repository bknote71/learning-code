package com.bknote71.springbootbatch.config.faultTolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.RethrowOnThresholdExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class RetryConfiguration  {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job retryJob() {
        return jobBuilderFactory.get("retryJob")
                .start(step())
                .build();
    }

    public Step step() {
        return stepBuilderFactory.get("step")
                .<Integer, Integer>chunk(10)
                .reader(new ItemReader<>() {
                    int i = 0;
                    @Override
                    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return ++i > 30 ? null : i;
                    }
                })
                .processor(new ItemProcessor<Integer, Integer>() {
                    @Override
                    public Integer process(Integer item) throws Exception {
                        System.out.println(item);
                        if((item % 2) == 0)
                            throw new IllegalArgumentException();
                        return item;
                    }
                })
                .writer(System.out::println)
                .faultTolerant()
//                .retryLimit(2)
//                .retry(IllegalArgumentException.class)
                .retryPolicy(retryPolicy())
                .skip(IllegalArgumentException.class) // retry와 보완적인 관계인 skip <<
                .skipLimit(2)                         // retryLimit: 개별 아이템당 적용, skipLimit: 전체 아이템에 적용
                .build();
    }

    // 직접 RetryPolicy 를 등록해서 사용해보기
    @Bean
    public RetryPolicy retryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IllegalArgumentException.class, Boolean.TRUE);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(2, retryableExceptions);
        return retryPolicy;
    }
}

