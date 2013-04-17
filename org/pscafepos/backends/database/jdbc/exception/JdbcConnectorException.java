package org.pscafepos.backends.database.jdbc.exception;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public class JdbcConnectorException extends Exception {
    public JdbcConnectorException(String message) {
        super(message);
    }

    public JdbcConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcConnectorException(Throwable cause) {
        super(cause);
    }
}
