package com.eney.batch.job;

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
    public Job stepNextConditionalJob(){
        return jobBuilderFactory.get("stepNextConditionalJob")
                // step1 실패 시나리오 : step1 -> step3
                // step1 성공 시나리오 : step1 -> step2 -> step3
                .start(conditionalJobStep1())
                    .on("FAILED") //캐치할 ExitStatus
                    .to(conditionalJobStep3()) //다음으로 이동할 step
                    .on("*") //*일 경우 모든 ExitStatus
                    .end() //step3로 이동 시 flow 종료
                .from(conditionalJobStep1()) //일종의 이벤트 리스너, 상태값을 보고 일치하는 상태라면 to()에 포함된 step을 호출
                    //step1의 이벤트 캐치가 FAILED로 되어있는 상태에서 추가로 이벤트를 캐치하려면 from을 써야만 함
                    .on("*") //FAILED 외의 모든 경우에
                    .to(conditionalJobStep2()) // step2로 이동
                    .next(conditionalJobStep3()) // step2가 정상 종료되면 step3로 이동
                    .on("*") // step3의 결과와 관계 없이
                    .end() //step3로 이동하면 Flow가 종료된다.
                .end() // Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1(){
        return stepBuilderFactory.get("conditinalJobStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>>This is stepNextConditinalJob Step1");

                    //ExitStatus를 보고 flow가 진행된다
                   // contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep2(){
        return stepBuilderFactory.get("conditionalJobStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>This is stepNextConditionalJob step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep3(){
        return stepBuilderFactory.get("conditionalJobStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>>This is stepNextConditionalJob step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
