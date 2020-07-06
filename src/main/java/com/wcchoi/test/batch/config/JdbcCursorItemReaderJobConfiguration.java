package com.wcchoi.test.batch.config;

import javax.sql.DataSource;

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

import com.wcchoi.test.batch.domain.Pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
/**
 * Database Cursor : DB Connection을 하나 맺어놓고, 해당 소켓을 유지하면서 SQL을 DB에 실행하고나서 fetchSize를 단위로 row를 fetch하고,
 * fetch된 fetchSize만큼의 row를 cursor로 한개씩 읽다가, 다시 fetch가 필요하면 사용자 투명하게 fetch operation 수행.
 * 쿼리는 단 1번만 실행하고, 컨넥션을 유지하며 내부에서 fetch를 반복하며 cursor를 채워 제공하는 방법.
 * 쿼리수행은 적으나 컨넥션 timeout을 신경써야되는 문제가 존재.
 *
 *
 * CursorItemReader의 주의 사항
 * CursorItemReader를 사용하실때는 Database와 SocketTimeout을 충분히 큰 값으로 설정해야만 합니다.
 * Cursor는 하나의 Connection으로 Batch가 끝날때까지 사용되기 때문에 Batch가 끝나기전에 Database와 어플리케이션의 Connection이 먼저 끊어질수 있습니다.
 *
 * 그래서 Batch 수행 시간이 오래 걸리는 경우에는 PagingItemReader를 사용하시는게 낫습니다.
 * Paging의 경우 한 페이지를 읽을때마다 Connection을 맺고 끊기 때문에 아무리 많은 데이터라도 타임아웃과 부하 없이 수행될 수 있습니다.
 */
public class JdbcCursorItemReaderJobConfiguration {

    private static final int chunkSize = 4;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private int index = 1;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                                .start(jdbcCursorItemReaderStep())
                                .build();
    }

    public Step jdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                                 // ItemReader/ItemWriter를 사용하는 Step을 선언할때,
                                 // StepBuilder에 step설정을 할때, ItemReader의 반환타입, ItemWriter의 파라메터, chunk size를 선언적으로 입력한다.
                .<Pay, Pay>chunk(chunkSize)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        //ItemReader도 Builder로 셋팅하고 생성
        return new JdbcCursorItemReaderBuilder<Pay>()
                //CursorItemReader는 쿼리는 limit, offset 구문 없이 DB에 실행시키고, 사용자 투명하게 JDBC 레벨에서 fetch를 fetchSize 설정값으로 수행한후 read시에는 1건씩 사용자에게 넘겨준다.
                .fetchSize(2)
                .dataSource(dataSource)
                //RowMapper : 쿼리결과를 Java 인스턴스로 매핑하는 역할을 수행한다.
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql("select id, amount, tx_name, tx_date_time from pay")
                .name(("jdbcCursorItemReader")) //reader 이름
                .build();
    }

    /**
     * ItemWriter는 Item을 chunkSize로 저장한 List를 받아 처리한다.
     * @return
     */
    private ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            log.info("ItemWriter Start!! [{}]", index++);
            for (final Pay pay : list) {
                log.info("Current Item (Pay)={}", pay);
            }
        };
    }

}
