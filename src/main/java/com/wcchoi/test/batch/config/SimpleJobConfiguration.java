package com.wcchoi.test.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SimpleJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job simpleJob() {
        return jobBuilderFactory.get("simpleJob")
                                .start(simpleStep1(null))
                                .next(simpleStep2(null))
                                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") final String requestDate) {
        return stepBuilderFactory.get("simpleStep1")
                                 .tasklet((contribution, chunkContext) -> {
//                                     throw new IllegalArgumentException("step1에서 실패합니다."); //일부러 step1에서 예외 발생
                                     log.info(">>>> This is Step1");
                                     log.info(">>>>> requestDate = {}", requestDate);
                                     return RepeatStatus.FINISHED;
                                 })
                                 .build();
    }

    @Bean
    @JobScope // JobParameter 사용시 붙혔음
    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") final String requestDate) {
        return stepBuilderFactory.get("simpleStep2")
                                 .tasklet((contribution, chunkContext) -> {
                                     log.info(">>>> This is Step2");
                                     log.info(">>>>> requestDate = {}", requestDate);
                                     return RepeatStatus.FINISHED;
                                 }).build();
    }

}
