package org.pscafepos.backends.domain.credit;

import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 *         Date: 27.08.2009
 */
public class CreditManagerException extends DAOException {
    public CreditManagerException() {
    }

    public CreditManagerException(String message) {
        super(message);
    }

    public CreditManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreditManagerException(Throwable cause) {
        super(cause);
    }
}
