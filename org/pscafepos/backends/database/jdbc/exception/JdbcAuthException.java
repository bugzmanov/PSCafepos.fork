package org.pscafepos.backends.database.jdbc.exception;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public class JdbcAuthException extends JdbcConnectorException{

    public JdbcAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
