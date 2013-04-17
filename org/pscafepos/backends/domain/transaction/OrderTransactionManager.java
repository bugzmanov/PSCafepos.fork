package org.pscafepos.backends.domain.transaction;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.domain.transaction.TransactionManagerException;
import org.pscafepos.model.OrderTransaction;

/**
 * @author bagmanov
 */
public interface OrderTransactionManager extends TransactionalDAO {
    OrderTransaction save(OrderTransaction transaction) throws TransactionManagerException;
    boolean hadIdenticalTransactionInCurrentSession(OrderTransaction transaction);
}
