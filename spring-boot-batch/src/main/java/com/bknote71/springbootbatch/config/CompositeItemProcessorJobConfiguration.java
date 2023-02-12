package com.bknote71.springbootbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class CompositeItemProcessorJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job compositeJob() {
        return jobBuilderFactory.get("compositeJob")
                .start(step1())
                .build();
    }

    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String, String>chunk(10)
                .reader(new ItemReader<>() {
                    int i = 1;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return i++ > 10 ? null : "item";
                    }
                })
                .processor(compositeProcessor())
                .writer(System.out::println)
                .build();
    }

    private ItemProcessor<? super String, String> compositeProcessor() {
        List itemProcessors = new ArrayList<>();
        itemProcessors.add(new CustomItemProcessor1());
        itemProcessors.add(new CustomItemProcessor2());

        return new CompositeItemProcessorBuilder<String, String>()
                .delegates(itemProcessors)
                .build();
    }

    static class CustomItemProcessor1 implements ItemProcessor<String, String> {

        int i = 1;
        @Override public String process(String item) throws Exception {
            return (item + i++);
        }
    }

    static class CustomItemProcessor2 implements ItemProcessor<String, String> {
        int i = 1;
        @Override public String process(String item) throws Exception {
            return (item + i++);
        }
    }

}
