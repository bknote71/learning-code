package com.bknote71.springbootbatch.config.chunk.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemProcessorBuilder;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ClassifierItemProcessorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job classifierJob() {
        return jobBuilderFactory.get("classifierJob")
                .start(step())
                .build();

    }

    private Step step() {
        return stepBuilderFactory.get("step")
                .<XDto, XDto>chunk(10)
                .reader(new ItemReader<>() {
                    int i = 0;
                    @Override
                    public XDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        XDto xDto = new XDto(++i);
                        return i > 10 ? null : xDto;
                    }
                })
                .processor(classifierCompositeItemProcessor())
                .writer(System.out::println)
                .build();
    }

    private ItemProcessor<? super XDto, ? extends XDto> classifierCompositeItemProcessor() {
        // Classifier<C, T>: C를 기반으로 T 분류
        Classifier<XDto, ItemProcessor<?, ? extends XDto>> classifier = new Classifier<>() {
            @Override public ItemProcessor classify(XDto xDto) {
                return xDto.id > 5 ? new CustomItemProcessor2() : new CustomItemProcessor1();
            }
        };

        return new ClassifierCompositeItemProcessorBuilder<XDto, XDto>()
                .classifier(classifier)
                .build();
    }

    @Getter
    @ToString
    static class XDto {
        int id;

        public XDto(int id) {
            this.id = id;
        }
    }

    static class CustomItemProcessor1 implements ItemProcessor<XDto, XDto> {

        @Override public XDto process(XDto item) throws Exception {
            System.out.println(item.id + ": processor 1!!");
            return item;
        }
    }

    static class CustomItemProcessor2 implements ItemProcessor<XDto, XDto> {
        @Override public XDto process(XDto item) throws Exception {
            System.out.println(item.id + ": processor 2!!");
            return item;
        }
    }


}
