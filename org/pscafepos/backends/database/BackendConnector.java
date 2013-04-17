package org.pscafepos.backends.database;

import java.sql.Connection;

/**
 * @author bagmanov
 */
public interface BackendConnector {

    public Connection getConnection();
    public void releaseConnection(Connection connection);
}
