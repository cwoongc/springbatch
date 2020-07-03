package com.wcchoi.test.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JobLauncherController {

    private final JobLauncher jobLauncher;
//    @Qualifier("simpleJob")
    private final Job simpleJob;

    @GetMapping("/launchjob")
    public String handle(@RequestParam("requestDate") String requestDate) throws Exception {
        try {
            //Controller에서 동적으로 JobParameter 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("requestDate", requestDate) // API로 받은 requestDate을 jobParameter로 만든다.
//                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            //동적으로 Job을 실행하면서 생성한 JobParameter 패싱
            //(동기로 동작)
            jobLauncher.run(simpleJob, jobParameters);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return "Done";
    }


}
