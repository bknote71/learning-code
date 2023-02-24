package com.bknote71.springbootbatch.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

// job을 web상에서 제어할 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/jobs")
@Controller
public class JobController {
    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer; // 조회만 가능
    private final JobOperator jobOperator; // job 제어 <<

    @PostMapping("/start")
    public @ResponseBody String startJob(@RequestBody JobParamDto param) throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = jobRegistry.getJob(iterator.next());
            System.out.println("jobName: " + job.getName());
            jobOperator.start(job.getName(), param.toParameterString());
        }
        return "batch start!";
    }

    // 실행중인 Job 재실행
    @PostMapping("/stop")
    public @ResponseBody String stopJob() throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = (SimpleJob) jobRegistry.getJob(iterator.next());
            System.out.println("jobName: " + job.getName());
            // 실행중인 Job 가져오기 : JobExplorer
            Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(job.getName());
            for (JobExecution jobExecution : runningJobExecutions) {
                // 실행중인 Step 모두 종료 후에 종료시키기.
                jobOperator.stop(jobExecution.getId());
            }
        }
        return "batch stop!";
    }

    // 실패하거나 중단된 Job 재실행
    @PostMapping("/restart")
    public @ResponseBody String restartJob(@RequestBody JobParamDto param) throws NoSuchJobException, JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, JobParametersInvalidException, JobRestartException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = (SimpleJob) jobRegistry.getJob(iterator.next());
            System.out.println("jobName: " + job.getName());

            // 마지막 JobExecution 을 재실행해야 한다.
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(job.getName());
            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
            jobOperator.restart(lastJobExecution.getId());
        }
        return "batch restart!";
    }
}
