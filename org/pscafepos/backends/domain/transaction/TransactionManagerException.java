package org.pscafepos.backends.domain.transaction;

import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class TransactionManagerException extends DAOException {
    public TransactionManagerException() {
    }

    public TransactionManagerException(String message) {
        super(message);
    }

    public TransactionManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionManagerException(Throwable cause) {
        super(cause);
    }
}
