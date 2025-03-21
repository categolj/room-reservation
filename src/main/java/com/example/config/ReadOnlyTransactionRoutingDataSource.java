package com.example.config;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReadOnlyTransactionRoutingDataSource extends AbstractRoutingDataSource {

	private final boolean debug;

	public enum DataSourceType {

		READ_ONLY, READ_WRITE

	}

	public ReadOnlyTransactionRoutingDataSource(boolean debug) {
		this.debug = debug;
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? DataSourceType.READ_ONLY
				: DataSourceType.READ_WRITE;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = super.getConnection();
		if (!debug) {
			return connection;
		}
		Object lookupKey = determineCurrentLookupKey();
		return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class },
				(proxy, method, args) -> {
					Object result = method.invoke(connection, args);
					if ("toString".equals(method.getName())) {
						return "<<%s>> %s".formatted(lookupKey, result);
					}
					return result;
				});
	}

}