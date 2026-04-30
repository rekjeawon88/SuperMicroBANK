package com.bank.global.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties({
        OracleDataSourceProperties.class,
        TiberoDataSourceProperties.class
})
public class MultiDataSourceConfig {

    @Bean(name = "oracleDataSource")
    @Primary
    @Profile("oracle")
    public DataSource oracleDataSource(OracleDataSourceProperties oracleDataSourceProperties) {
        // oracle 프로파일에서는 Oracle을 기본 데이터소스로 사용한다.
        return createDataSource(
                oracleDataSourceProperties.getDriverClassName(),
                oracleDataSourceProperties.getJdbcUrl(),
                oracleDataSourceProperties.getUsername(),
                oracleDataSourceProperties.getPassword(),
                oracleDataSourceProperties.getMaximumPoolSize(),
                oracleDataSourceProperties.getMinimumIdle(),
                oracleDataSourceProperties.getConnectionTimeout()
        );
    }

    @Bean(name = "tiberoDataSource")
    @Profile("oracle")
    public DataSource tiberoSecondaryDataSource(TiberoDataSourceProperties tiberoDataSourceProperties) {
        // oracle 프로파일에서도 Tibero는 추가 데이터소스로 함께 등록한다.
        return createDataSource(
                tiberoDataSourceProperties.getDriverClassName(),
                tiberoDataSourceProperties.getJdbcUrl(),
                tiberoDataSourceProperties.getUsername(),
                tiberoDataSourceProperties.getPassword(),
                tiberoDataSourceProperties.getMaximumPoolSize(),
                tiberoDataSourceProperties.getMinimumIdle(),
                tiberoDataSourceProperties.getConnectionTimeout()
        );
    }

    @Bean(name = "tiberoDataSource")
    @Primary
    @Profile("tibero")
    public DataSource tiberoPrimaryDataSource(TiberoDataSourceProperties tiberoDataSourceProperties) {
        // tibero 프로파일에서는 Tibero를 기본 데이터소스로 전환한다.
        return createDataSource(
                tiberoDataSourceProperties.getDriverClassName(),
                tiberoDataSourceProperties.getJdbcUrl(),
                tiberoDataSourceProperties.getUsername(),
                tiberoDataSourceProperties.getPassword(),
                tiberoDataSourceProperties.getMaximumPoolSize(),
                tiberoDataSourceProperties.getMinimumIdle(),
                tiberoDataSourceProperties.getConnectionTimeout()
        );
    }

    @Bean(name = "oracleDataSource")
    @Profile("tibero")
    public DataSource oracleSecondaryDataSource(OracleDataSourceProperties oracleDataSourceProperties) {
        // tibero 프로파일에서도 Oracle은 추가 데이터소스로 함께 등록한다.
        return createDataSource(
                oracleDataSourceProperties.getDriverClassName(),
                oracleDataSourceProperties.getJdbcUrl(),
                oracleDataSourceProperties.getUsername(),
                oracleDataSourceProperties.getPassword(),
                oracleDataSourceProperties.getMaximumPoolSize(),
                oracleDataSourceProperties.getMinimumIdle(),
                oracleDataSourceProperties.getConnectionTimeout()
        );
    }

    private DataSource createDataSource(
            String driverClassName,
            String jdbcUrl,
            String username,
            String password,
            Integer maximumPoolSize,
            Integer minimumIdle,
            Long connectionTimeout
    ) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(driverClassName);
        hikariDataSource.setJdbcUrl(jdbcUrl);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        hikariDataSource.setMaximumPoolSize(maximumPoolSize);
        hikariDataSource.setMinimumIdle(minimumIdle);
        hikariDataSource.setConnectionTimeout(connectionTimeout);
        return hikariDataSource;
    }
}
