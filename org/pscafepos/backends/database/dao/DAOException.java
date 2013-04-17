package org.pscafepos.backends.database.dao;

/**
 * @author bagmanov
 *         Date: 27.08.2009
 */
public class DAOException extends Exception{

    public DAOException() {
    }

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }
}
