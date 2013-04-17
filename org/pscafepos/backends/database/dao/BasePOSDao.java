package org.pscafepos.backends.database.dao;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.transaction.Transaction;
import org.pscafepos.backends.database.transaction.TransactionEventHandler;
import org.pscafepos.backends.database.transaction.impl.PosTransaction;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;

/**
 * @author bagmanov
 *         Date: 25.08.2009
 */
public class BasePOSDao extends BaseJDBCDao implements TransactionalDAO {
    protected IPosSettings settings;
    protected String posTablesPrefix;
    private PosTransaction currentTransaction;
    private Connection connectionTempHolder;
    private IPosSettings settingsTempHolder;

    protected BasePOSDao() {
        super();
    }
    
    protected BasePOSDao(Connection connection, IPosSettings settings) {
        super(connection);
        setSettings(settings);
    }

    public void setSettings(IPosSettings settings) {
        this.settings = settings;
        if(settings != null){
            this.posTablesPrefix = settings.getTablesPrefix();
        }
    }


    public boolean isInTransaction() {
        return currentTransaction != null;
    }

    public void joinTransaction(Transaction transaction) {
        if(currentTransaction != null) {
            throw new IllegalStateException ("Already participating in transaction");
        }
        if (!(transaction instanceof PosTransaction)) {
            throw new IllegalArgumentException("Only Pos transaction are supported by POS dao");
        }
        currentTransaction = (PosTransaction) transaction;
        currentTransaction.addEventHandler(new TransactionEventHandler() {
            public void onCommit() {
                leaveTransaction();
            }

            public void onRollback() {
                leaveTransaction();
            }
        });
        connectionTempHolder = connection;
        settingsTempHolder = settings;
        setConnection(currentTransaction.getConnection());
        setSettings(currentTransaction.getPosSettings());
    }

    protected void leaveTransaction() {
        setConnection(connectionTempHolder);
        setSettings(settingsTempHolder);
        currentTransaction = null;
        connectionTempHolder = null;
        settingsTempHolder = null;

    }

}
