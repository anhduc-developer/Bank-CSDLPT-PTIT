package dev.distributed.bank.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DanangSlaveDataSourceConfig {

    @Bean(name = "danangSlaveDataSource")
    public DataSource danangSlaveDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(
                "jdbc:mysql://localhost:3318/bank_danang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
        ds.setUsername("root");
        ds.setPassword("root");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setConnectionTimeout(5000);
        ds.setMaximumPoolSize(5);
        ds.setPoolName("DanangSlavePool");
        ds.setReadOnly(true);
        return ds;
    }

    @Bean(name = "danangSlaveJdbcTemplate")
    public JdbcTemplate danangSlaveJdbcTemplate(@Qualifier("danangSlaveDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
