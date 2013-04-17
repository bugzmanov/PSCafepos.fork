package org.pscafepos.backends.database.jdbc;

import org.pscafepos.backends.database.jdbc.exception.JdbcAuthException;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;

import java.sql.Connection;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public interface JdbcConnector {
    Connection getNewConnection() throws JdbcAuthException, JdbcConnectorException;
}
