package dev.distributed.bank.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DanangDataSourceConfig {

    @Bean(name = "danangDataSource")
    public DataSource danangDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(
                "jdbc:mysql://localhost:3308/bank_danang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
        ds.setUsername("root");
        ds.setPassword("root");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setConnectionTimeout(5000);
        ds.setMaximumPoolSize(10);
        ds.setPoolName("DanangPool");
        return ds;
    }

    @Bean(name = "danangJdbcTemplate")
    public JdbcTemplate danangJdbcTemplate(@Qualifier("danangDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "danangTransactionManager")
    public PlatformTransactionManager danangTransactionManager(
            @Qualifier("danangDataSource") DataSource ds) {

        DataSourceTransactionManager txManager = new DataSourceTransactionManager(ds);

        txManager.setTransactionSynchronization(
                DataSourceTransactionManager.SYNCHRONIZATION_NEVER);

        return txManager;
    }
}
