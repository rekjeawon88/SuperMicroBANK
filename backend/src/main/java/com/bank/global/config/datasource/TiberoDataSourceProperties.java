package com.bank.global.config.datasource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource.tibero")
public class TiberoDataSourceProperties {

    private String driverClassName;
    private String jdbcUrl;
    private String username;
    private String password;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private Long connectionTimeout;
}
