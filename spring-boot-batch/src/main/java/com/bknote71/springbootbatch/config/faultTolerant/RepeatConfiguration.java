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

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class RepeatConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job repeatJob() {
        return jobBuilderFactory.get("repeatJob")
                .start(step())
                .build();
    }

    public Step step() {
        return stepBuilderFactory.get("step")
                .<String, String>chunk(10)
                .reader(new ItemReader<>() {
                    int i = 1;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return i++ > 10 ? null : "item";
                    }
                })
                .processor(new ItemProcessor<String, String>() {

                    RepeatTemplate repeatTemplate = new RepeatTemplate();

                    @Override
                    public String process(String item) throws Exception {

//                        repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(3)); // chunkSize 만큼 반복
//                        repeatTemplate.setCompletionPolicy(new TimeoutTerminationPolicy(30)); // 지정한 시간만큼 반복 (millisecond 단위)
                        // 여러개의 CompletionPolicy 중에 하나라도 조건에 부합하면 종료
//                        CompositeCompletionPolicy completionPolicy = new CompositeCompletionPolicy();
//                        CompletionPolicy[] completionPolicies = new CompletionPolicy[]{new SimpleCompletionPolicy(3), new TimeoutTerminationPolicy(3)};
//                        completionPolicy.setPolicies(completionPolicies);
//                        repeatTemplate.setCompletionPolicy(completionPolicy);
                        // 예외 처리 핸들러 설정
//                        repeatTemplate.setExceptionHandler(new SimpleLimitExceptionHandler(3)); // 이렇게 전달하면 안됨 << threshold 적용이 안됨
                        repeatTemplate.setExceptionHandler(simpleLimitExceptionHandler()); // 이런식으로 빈으로 등록해야 하는 듯?
                        repeatTemplate.iterate(new RepeatCallback() {
                            int i = 0;
                            @Override
                            public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                                System.out.println(item + " repeat " + i++);
                                throw new RuntimeException("exception hoho!");
//                                return RepeatStatus.CONTINUABLE;
                            }
                        });

                        return item;
                    }
                })
                .writer(System.out::println)
                .build();
    }

    @Bean
    public ExceptionHandler simpleLimitExceptionHandler() {
        return new SimpleLimitExceptionHandler(3);
    }
}

