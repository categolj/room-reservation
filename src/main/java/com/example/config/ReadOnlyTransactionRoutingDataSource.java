package com.example.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReadOnlyTransactionRoutingDataSource extends AbstractRoutingDataSource {

	public enum DataSourceType {

		READ_ONLY, READ_WRITE

	}

	@Override
	protected Object determineCurrentLookupKey() {
		return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? DataSourceType.READ_ONLY
				: DataSourceType.READ_WRITE;
	}

}