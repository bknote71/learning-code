package com.bknote71.springbootbatch;

import com.bknote71.springbootbatch.config.SimpleJobConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBatchTest
// test를 위해서는 job 설정 클래스를 하나만 빈으로 등록해야 한다.
// + 추가 의존성 주입을 위한 설정 클래스
@SpringBootTest(classes = {SimpleJobConfiguration.class, TestBatchConfig.class})
public class SimpleJobTest {

    // @SpringBatchTest 를 설정하면 자동으로 등록해준다.
    // @Autowired setJob 에는 위에서 설정한 Job 설정 클래스의 Job 을 읽어온다. (그렇기 때문에 job 설정 클래스는 단 한개만 필요함.)
    @Autowired JobLauncherTestUtils jobLauncherTestUtils;

    // batch test는 public 이어야 하는 듯 하다.
    @Test
    public void simpleJob_test() throws Exception {

        // job을 실행시키기 위해서는 jobParameters 가 필요하다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestDate", "20230224")
                .addLong("date", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) // 파라미터를 겹치지 않게 함으로써 JobInstance 를 계속 생성시킬 수 있게끔한다.
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    public void simpleStep_test() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestDate", "20230224")
                .addLong("date", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) // 파라미터를 겹치지 않게 함으로써 JobInstance 를 계속 생성시킬 수 있게끔한다.
                .toJobParameters();

        // step 만 실행
        JobExecution stepJobExecution = jobLauncherTestUtils.launchStep("simpleStep");
        List<StepExecution> stepExecutions = stepJobExecution.getStepExecutions()
                .stream()
                .toList();
        StepExecution stepExecution = stepExecutions.get(0);

        // simplStep:
        // - ChunkOrientedTasklet = (commit, read, count)

        // Commit Count: 실제_했던_커밋수 + 1
        // - read 마지막에서 모두 읽었는지 확인하려고 커밋을 한번 더 돌기 때문에 하나 더 추가한다.
        // write count: write 한 "item" 수 (not List<Item> << count)
        assertThat(stepExecution.getCommitCount()).isEqualTo(6);
        assertThat(stepExecution.getReadCount()).isEqualTo(10);
        assertThat(stepExecution.getWriteCount()).isEqualTo(10);
    }
}
