package org.pscafepos.backends.database.jdbc;

import org.pscafepos.backends.database.jdbc.exception.JdbcAuthException;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public class JdbcConnectorImpl implements JdbcConnector{

    private ConnectionSettings settings;
    private String backendName;

    public JdbcConnectorImpl(String backendName, ConnectionSettings settings)  {
        this.settings = settings;
        this.backendName = backendName;
    }

    public Connection getNewConnection() throws JdbcAuthException, JdbcConnectorException {
        try {
            return DriverManager.getConnection(settings.getConnectionString());
        } catch (SQLException e) {
            if(e.getSQLState().startsWith("28")) {
                throw new JdbcAuthException("Couldn't connect to " + backendName, e);
            } else {
                throw new JdbcConnectorException("Couldn't connect to " + backendName, e);
            }
        }
    }


}
