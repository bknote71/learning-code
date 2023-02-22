package com.bknote71.springbootbatch.config.mutlithread;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// AsyncItemProcessor 를 활용하기 위해서는 spring-batch-integration 의존성을 추가해야한다.
@RequiredArgsConstructor
@Configuration
public class MultiJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job multiJob() {
        return jobBuilderFactory.get("multiJob")
                .start(multiStep())
                .build();
    }

    private Step multiStep() {
        return stepBuilderFactory.get("multiStep")
                .<Integer, Integer>chunk(10)
                .reader(threadSafeItemReader())
                .processor(new SleepItemProcessor())
                .writer(System.out::println)
                .listener(new ItemWriteListener<Integer>() {
                    @Override public void beforeWrite(List<? extends Integer> items) {

                    }

                    @Override public void afterWrite(List<? extends Integer> items) {
                        System.out.println(Thread.currentThread().getName() + " thread's item size: " + items.size());
                    }

                    @Override public void onWriteError(Exception exception, List<? extends Integer> items) {

                    }
                })
                .taskExecutor(myTaskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor myTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("my-thread-");
        return executor;
    }

    private ItemReader<Integer> threadSafeItemReader() {
        return new ItemReader<Integer>() {
            AtomicInteger i = new AtomicInteger();

            @Override
            public Integer read() {
                int ret = i.getAndIncrement();
                return ret < 30 ? ret : null;
            }
        };
    }

    static class SleepItemProcessor implements ItemProcessor<Integer, Integer> {

        @Override public Integer process(Integer item) throws Exception {
            Thread.sleep(10);
            return item;
        }
    }


}
