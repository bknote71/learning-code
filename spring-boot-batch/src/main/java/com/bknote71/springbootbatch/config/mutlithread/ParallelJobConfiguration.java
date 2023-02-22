package com.bknote71.springbootbatch.config.mutlithread;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// AsyncItemProcessor 를 활용하기 위해서는 spring-batch-integration 의존성을 추가해야한다.
@RequiredArgsConstructor
@Configuration
public class ParallelJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job parallelJob() {
        return jobBuilderFactory.get("parallelJob")
                .start(flow1())
                .split(taskExecutor()) // 해당 taskExecutor 는 빈으로 등록된 executor이어야 한다.
                .add(flow2(), flow3())
                .end()
                .build();
    }

    private Flow flow1() {
        TaskletStep step1 = stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
        return new FlowBuilder<Flow>("flow1")
                .start(step1)
                .build();
    }

    private Flow flow2() {
        TaskletStep step2 = stepBuilderFactory.get("step2")
                .tasklet(tasklet())
                .build();
        TaskletStep step3 = stepBuilderFactory.get("step3")
                .tasklet(tasklet())
                .build();
        return new FlowBuilder<Flow>("flow2")
                .start(step2)
                .next(step3)
                .build();
    }

    private Flow flow3() {
        TaskletStep step4 = stepBuilderFactory.get("step4")
                .tasklet(tasklet())
                .build();
        TaskletStep step5 = stepBuilderFactory.get("step5")
                .tasklet(tasklet())
                .build();
        return new FlowBuilder<Flow>("flow3")
                .start(step4)
                .next(step5)
                .build();
    }

    @Bean
    public Tasklet tasklet() {
        return new AggregationTasklet();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("my-thread-");
        return executor;
    }

    static class AggregationTasklet implements Tasklet {
        private int sum = 0;
        private Object lock = new Object();

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            synchronized (lock) {
                for (int i = 0; i < 100000000; ++i) {
                    sum++;
                }
                System.out.println(String.format("%s has been executed on thread %s",
                        chunkContext.getStepContext().getStepName(),
                        Thread.currentThread().getName()));
                System.out.println("sum: " + sum);
            }
            return RepeatStatus.FINISHED;
        }
    }
}
