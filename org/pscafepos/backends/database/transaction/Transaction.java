package org.pscafepos.backends.database.transaction;

import org.pscafepos.backends.database.TransactionalDAO;

/**
 * @author bagmanov
 *         Date: 03.09.2009
 */
public interface Transaction {
    void start() throws TransactionException;

    void addToTransaction(TransactionalDAO... daos);

    public void addEventHandler(TransactionEventHandler  eventHandler);

    void commit() throws TransactionException;

    void rollback() throws TransactionException;
}
