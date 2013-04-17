package org.pscafepos.service.offline.watchdog.connection;

import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;

import java.sql.Connection;

/**
 * @author bagmanov
 */
public interface IWatchDog {
    boolean isOfflineMode(Connection connection);

    Connection getCurrentConnection();//todo lock this

    Connection getNewConnection() throws JdbcConnectorException;

    void releaseConnection(Connection connection);
}
