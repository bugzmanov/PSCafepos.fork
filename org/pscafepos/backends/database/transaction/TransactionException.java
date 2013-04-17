package org.pscafepos.backends.database.transaction;

/**
 * @author bagmanov
 *         Date: 27.08.2009
 */
public class TransactionException extends Exception{
    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
