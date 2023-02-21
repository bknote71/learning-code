package com.bknote71.springbootbatch.config.faultTolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SkipConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job skipJob() {
        return jobBuilderFactory.get("skipJob")
                .start(step())
                .build();
    }

    public Step step() {
        return stepBuilderFactory.get("step")
                .<Integer, Integer>chunk(5)
                .reader(new ItemReader<Integer>() {
                    int i = 0;

                    @Override public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        if (++i == 3) throw new IllegalArgumentException("argument skip");
                        System.out.println("item reader: " + i);
                        return i > 20 ? null : i;
                    }
                })
                .processor(new ItemProcessor<Integer, Integer>() {
                    @Override
                    public Integer process(Integer item) throws Exception {
                        System.out.println("item process: " + item);
                        if (item == 6 || item == 9)
                            throw new IllegalStateException("state skip");
                        return item;
                    }
                })
                .writer(items -> {
                    for (Integer item : items) {
                        if (item == 12) {
                            throw new IllegalAccessException("access skip");
                        } else {
                            System.out.println("item writer: " + item);
                        }
                    }
                })
                .faultTolerant()
//                .skip(IllegalArgumentException.class)
//                .skip(IllegalStateException.class)
//                .skipLimit(5)
                .skipPolicy(skipPolicy())
                .build();
    }

    private SkipPolicy skipPolicy() {
        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<>();
        skippableExceptions.put(IllegalArgumentException.class, Boolean.TRUE);
        skippableExceptions.put(IllegalStateException.class, Boolean.TRUE);
        skippableExceptions.put(IllegalAccessException.class, Boolean.TRUE);
        SkipPolicy skipPolicy = new LimitCheckingItemSkipPolicy(5, skippableExceptions);
        return skipPolicy;
    }
}
