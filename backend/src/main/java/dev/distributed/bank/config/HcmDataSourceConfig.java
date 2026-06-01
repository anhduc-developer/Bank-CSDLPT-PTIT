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
public class HcmDataSourceConfig {

    @Bean(name = "hcmDataSource")
    public DataSource hcmDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(
                "jdbc:mysql://localhost:3309/bank_hcm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
        ds.setUsername("root");
        ds.setPassword("root");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setConnectionTimeout(5000);
        ds.setMaximumPoolSize(10);
        ds.setPoolName("HcmPool");
        return ds;
    }

    @Bean(name = "hcmJdbcTemplate")
    public JdbcTemplate hcmJdbcTemplate(@Qualifier("hcmDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "hcmTransactionManager")
    public PlatformTransactionManager hcmTransactionManager(
            @Qualifier("hcmDataSource") DataSource ds) {

        DataSourceTransactionManager txManager = new DataSourceTransactionManager(ds);

        txManager.setTransactionSynchronization(
                DataSourceTransactionManager.SYNCHRONIZATION_NEVER);

        return txManager;
    }
}
