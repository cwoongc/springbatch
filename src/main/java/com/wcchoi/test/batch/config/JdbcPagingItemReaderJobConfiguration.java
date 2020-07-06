package com.wcchoi.test.batch.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.wcchoi.test.batch.domain.Pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcPagingItemReaderJobConfiguration {
    private static final int chunkSize = 10; //step 빌딩시에 사용될 chunk size.

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job jdbcPagingItemReaderJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingItemReaderJob")
                                .start(jdbcPagingItemReaderStep())
                                .build();
    }

    public Step jdbcPagingItemReaderStep() throws Exception {

        return stepBuilderFactory.get("jdbcPagingItemReaderStep")
                .<Pay, Pay>chunk(chunkSize)
                .reader(jdbcPagingItemReader())
                .writer(jdbcPagingItemWriter())
                .build();
    }

    private JdbcPagingItemReader<Pay> jdbcPagingItemReader() throws Exception {
        final Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("amount", 2000);

        final JdbcPagingItemReader<Pay> reader = new JdbcPagingItemReaderBuilder<Pay>()
                .pageSize(2)
                .fetchSize(2)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .queryProvider(createQueryProvider())
                .parameterValues(parameterValues)
                .name("jdbcPagingItemReader")
                .build();

        reader.afterPropertiesSet();

        return reader;

    }

    private PagingQueryProvider createQueryProvider() throws Exception {
        final SqlPagingQueryProviderFactoryBean queryProviderFactoryBean =
                new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
        queryProviderFactoryBean.setSelectClause("id, amount, tx_name, tx_date_time");
        queryProviderFactoryBean.setFromClause("from pay");
        queryProviderFactoryBean.setWhereClause("where amount >= :amount");

        final Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);

        queryProviderFactoryBean.setSortKeys(sortKeys);

        return queryProviderFactoryBean.getObject();

    }

    private ItemWriter<Pay> jdbcPagingItemWriter() {
        return list -> {
            for (final Pay pay : list) {
                log.info("Current Pay={}", pay);
            }
        };
    }

}
