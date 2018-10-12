package com.eney.batch.job;

import com.eney.batch.domain.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcCursorItemReaderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int chunkSize = 2000;

    @Bean
    public Job jdbcCursorItemReaderJob(){
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                .start(jdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep(){
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<Map<String,Object>,Map<String,Object>>chunk(chunkSize) // 첫번째 Pay는 Reader에서 반환할 타입, 두번째 Pay는 Writer에 파라미터로 넘어올 타입
                                //인자값을 넣은 경우는 Reader & Writer가 묶일 Chunk 트랜잭션 범위
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Map<String,Object>> jdbcCursorItemReader(){
        return new JdbcCursorItemReaderBuilder<Map<String,Object>>()
                //.fetchSize(chunkSize) // Database에서 한번에 가져올 데이터 양
                .dataSource(dataSource) //
                .rowMapper(new ColumnMapRowMapper())
                .sql("SELECT * FROM " + "test.전체페이지뷰")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemWriter<Map<String,Object>> jdbcCursorItemWriter(){
        return list -> {
            for(Map<String,Object> map : list){

                SimpleJdbcInsertOperations insertion = new SimpleJdbcInsert(dataSource).withTableName("전체페이지뷰_비교");
                RowMapper<Map<String,Object>> mapper = new ColumnMapRowMapper();
               // SqlParameterSource params = new BeanPropertySqlParameterSource(map);
                /*SqlParameterSource params = new MapSqlParameterSource()
                        .addValue("날짜", map.get("날짜"))
                        .addValue("기기카테고리", map.get("기기카테고리"))
                        .addValue("연도", map.get("연도"))
                        .addValue("월", map.get("월"))
                        .addValue("일", map.get("일"))
                        .addValue("주", map.get("주"))
                        .addValue("요일", map.get("요일"))
                        .addValue("페이지뷰", map.get("페이지뷰"));*/

                //insertion.execute(mapper);
                //log.info("CURRENT PAY = {}", map);
            }
        };
    }


}
