package com.eney.batch.job;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor // 생성자 DI를 위한 lombok 어노테이션
@Configuration //모든 job은 configuration으로 등록해서 사용
public class SimpleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory; // 생성자 DI 받음
    private final StepBuilderFactory stepBuilderFactory; // 생성자 DI 받음

    @Bean
    public Job simpleJob() {
        return jobBuilderFactory.get("simpleJob") //simpleJob 이라는 이름으로 job을 생성, job의 이름은 별도로 지정하지 않고 builder를 통하여 지정
                .start(simpleStep1(null))
                .next(simpleStep2(null))
                .build();
    }

    @Bean
    @JobScope
    //동일한 Job이 Job parameter가 달라지면 그때마다 BATCH_JOB_INSTANCE에 생성되며, 동일한 JobParameter는 여러개 존재할 수 없다.
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        //Tasklet 하나와 reader, processor, writer 한 묶음이 같은 레벨
        return stepBuilderFactory.get("simpleStep1") //simpleStep1 이라는 이름으로 step 생성, jobBuilderFactory와 마찬가지로 builder를 통해 지정
                .tasklet((contribution, chunkContext) -> { //Step 안에 수행될 기능 명시, tasklet은 step 안에 단일로 수행될 커스텀한 기능 선언시에 사용
                    log.info(">>>>> This is Step1"); //배치 수행 시 실행
                    log.info(">>>>> requestDate= {}" , requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate){
        return stepBuilderFactory.get("simpleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>> This is Step2");
                    log.info(">>>>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}