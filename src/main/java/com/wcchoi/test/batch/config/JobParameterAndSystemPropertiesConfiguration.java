package com.wcchoi.test.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * System Properties(-D) 사용시 Spring Batch의 JobParameter에 의한 JobInstance 관리기능을 사용할 수 없음.
 *  => Job Parameter가 아니므로 Spring Batch는 모름.
 * Scope Bean 선언 -> JobParameter 사용을 해야 JobInstance 별 중복수행 방지 관리가 가능해짐.
 * Scope Bean 선언 -> JobParameter 사용을 해야 API를 통한 job 실행이 가능
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class JobParameterAndSystemPropertiesConfiguration {


    @Bean
    /**
     * JobScope, StepScope
     *
     * 꼭 Application 시작시점이 아니더라도 Controller, Service와 같은 비즈니스 로직 처리 단계에서 "Job Parameter"를 할당 시킬 수 있음.
     * => 어플리케이션을 띄워놓고 API로 parameterized batch 실행
     * (Late Binding)
     */
    @StepScope //Step의 실행시점에 Bean이 생성됨 (application 시작시점이 아님)
    public FlatFileItemReader<String> reader(
            @Value("#{jobParameters[pathToFile]}") String pathToFile)
    {
        FlatFileItemReader<String> itemReader = new FlatFileItemReader<>();
        itemReader.setLineMapper(new DefaultLineMapper<>());
        itemReader.setResource(new ClassPathResource(pathToFile));
        return itemReader;
    }



}
