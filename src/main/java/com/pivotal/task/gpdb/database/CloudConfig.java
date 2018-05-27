package com.pivotal.task.gpdb.database;

import javax.sql.DataSource;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CloudConfig extends AbstractCloudConfig {

    @Bean(name = "batch-processing-db")
    public DataSource firstDataSource() {
        return connectionFactory().dataSource("batch-processing-db");
    }

    @Primary
    @Bean(name = "relational-c5b28c17-9b47-4819-abed-73a858bd1c4f")
    public DataSource secondDataSource() {
        return connectionFactory().dataSource("relational-c5b28c17-9b47-4819-abed-73a858bd1c4f");
    }
    
    @Bean
    public TaskConfigurer taskConfigurer() {
        return new DefaultTaskConfigurer(secondDataSource());
    }
}
