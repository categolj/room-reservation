package com.example.config;

import com.example.config.ReadOnlyTransactionRoutingDataSource.DataSourceType;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "read-replica.datasource.url")
public class DataSourceConfig {

	@Bean
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties primaryDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.hikari")
	public HikariDataSource primaryDataSource(
			@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		dataSource.setPoolName("read-write-pool");
		return dataSource;
	}

	@Bean
	@ConfigurationProperties("read-replica.datasource")
	public DataSourceProperties readReplicaDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("read-replica.datasource.hikari")
	public HikariDataSource readReplicaDataSource(
			@Qualifier("readReplicaDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		dataSource.setReadOnly(true);
		dataSource.setPoolName("read-replica-pool");
		return dataSource;
	}

	@Bean
	@Primary
	public DataSource actualDataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource,
			@Qualifier("readReplicaDataSource") DataSource readReplicaDataSource) {
		ReadOnlyTransactionRoutingDataSource routingDataSource = new ReadOnlyTransactionRoutingDataSource();
		routingDataSource.setTargetDataSources(
				Map.of(DataSourceType.READ_ONLY, readReplicaDataSource, DataSourceType.READ_WRITE, primaryDataSource));
		routingDataSource.afterPropertiesSet();
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}

}
