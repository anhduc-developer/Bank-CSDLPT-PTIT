package dev.distributed.bank.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class HanoiSlaveDataSourceConfig {

    @Bean(name = "hanoiSlaveDataSource")
    public DataSource hanoiSlaveDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(
                "jdbc:mysql://localhost:3317/bank_hanoi?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
        ds.setUsername("root");
        ds.setPassword("root");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setConnectionTimeout(5000);
        ds.setMaximumPoolSize(5);
        ds.setPoolName("HanoiSlavePool");
        ds.setReadOnly(true);
        return ds;
    }

    @Bean(name = "hanoiSlaveJdbcTemplate")
    public JdbcTemplate hanoiSlaveJdbcTemplate(@Qualifier("hanoiSlaveDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
