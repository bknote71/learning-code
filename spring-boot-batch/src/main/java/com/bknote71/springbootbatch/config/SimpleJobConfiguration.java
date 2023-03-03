package com.bknote71.springbootbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job simpleJob() {
        return jobBuilderFactory.get("simpleJob")
                .incrementer(new RunIdIncrementer())
                .start(simpleStep(null, null))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep(
            @Value("#{jobParameters[requestDate]}") String requestDate,
            @Value("#{jobParameters[date]}") Long date
    ) {
        return stepBuilderFactory.get("simpleStep")
                .<Integer, Integer>chunk(2)
                .reader(new ItemReader<Integer>() {
                    int i = 0;
                    @Override
                    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return i++ < 10 ? i : null;
                    }
                })
                .writer(items -> items.stream()
                        .forEach(i -> System.out.println("execute simple job at " + requestDate + ", item: " + i)))
                .build();
    }
}
