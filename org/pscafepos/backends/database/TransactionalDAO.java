package org.pscafepos.backends.database;

import org.pscafepos.backends.database.transaction.Transaction;

/**
 * @author bagmanov
 *         Date: 03.09.2009
 */
public interface TransactionalDAO {
     boolean isInTransaction();
     void joinTransaction(Transaction transaction);
}
