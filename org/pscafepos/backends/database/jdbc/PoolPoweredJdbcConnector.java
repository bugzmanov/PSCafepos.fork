package org.pscafepos.backends.database.jdbc;

import org.pscafepos.backends.database.jdbc.exception.JdbcAuthException;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author bagmanov
 */
public class PoolPoweredJdbcConnector implements JdbcConnector{

    private String backendName;
    private ConnectionSettings connectionSettings;
    private BasicDataSource dataSource;

    public PoolPoweredJdbcConnector(String backendName, ConnectionSettings connectionSettings, long timeout) {
        this.backendName = backendName;
        this.connectionSettings = connectionSettings;
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(connectionSettings.getDriverName());
        dataSource.setUrl(connectionSettings.getConnectionString());
        dataSource.setMaxActive(3);
        dataSource.setMaxIdle(3);
        dataSource.setMinEvictableIdleTimeMillis(5*60*1000);
        dataSource.setMaxWait(timeout);
        dataSource.setInitialSize(3);
        dataSource.setMinIdle(3);
    }

    public Connection getNewConnection() throws JdbcAuthException, JdbcConnectorException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            if(e.getSQLState()!= null &&e.getSQLState().startsWith("28")) {
                throw new JdbcAuthException("Couldn't connect to " + backendName, e);
            } else {
                throw new JdbcConnectorException("Couldn't connect to " + backendName, e);
            }
        }
    }
}
