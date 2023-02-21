package com.bknote71.springbootbatch.config.faultTolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class FaultTolerantConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job faultJob() {
        return jobBuilderFactory.get("faultJob")
                .start(step())
                .build();
    }

    public Step step() {
        return stepBuilderFactory.get("step")
                .<String, String>chunk(5)
                .reader(new ItemReader<String>() {
                    int i = 0;

                    @Override public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        if (++i == 1) throw new IllegalArgumentException("skip");
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<String, String>() {
                    @Override public String process(String item) throws Exception {
                        throw new IllegalStateException("retry");
                    }
                })
                .writer(System.out::println)
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(2)
                .retry(IllegalStateException.class)
                .retryLimit(2)
                .build();
    }

}
