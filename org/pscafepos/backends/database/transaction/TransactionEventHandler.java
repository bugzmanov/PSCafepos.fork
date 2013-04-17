package org.pscafepos.backends.database.transaction;

/**
 * @author bagmanov
 *         Date: 03.09.2009
 */
public interface TransactionEventHandler {

    public void onCommit();

    public void onRollback();
}
