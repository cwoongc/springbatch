package com.wcchoi.test.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepNextConditionalJob() {
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalJobStep1())
                    // step의 결과(ExistStatus)가 FAILED 일 경우. step의 결과를 catch하여 branch하는 것이 on 구문.
                    // on-to 혹은 on-end 로 귀결
                    //on을 통해 flow 빌딩을 시작한다. end가 호출되면
                    .on("FAILED")
                    .to(conditionalJobStep3())
                    //step3의 결과와 무관하게 (step3의 모든 ExistStatus를 catch)
                    .on("*")
                    .end()
                .from(conditionalJobStep1())
                    //step1결과 FAILED 이외에 모두
                    .on("*")
                    //step2로 이동
                    .to(conditionalJobStep2())
                    //step2가 정상종료하면 step3로 이동(next는 step이 실패하면 다음으로 더진행안함)
                    .next(conditionalJobStep3())
                    .on("*")
                    // on("*")뒤의 end는 FlowBuilder를 반환하는 end
                    .end()
                //build앞의 end는 FlowBuilder를 종료하는 end
                .end()
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return stepBuilderFactory.get("conditionalJobStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>> THIS IS stepNextConditionalJob Step1");

                    /**
                     * contribution에
                     * 탈출상태(ExitStatus)를 FAILED(실패)로 지정
                     * -> 해당 status보고 flow가 진행됨.
                     */
                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return stepBuilderFactory.get("conditionalJobStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> THIS IS stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return stepBuilderFactory.get("conditionalJobStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> THIS IS stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


}
