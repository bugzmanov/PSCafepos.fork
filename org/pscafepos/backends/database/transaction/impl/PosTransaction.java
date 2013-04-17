package org.pscafepos.backends.database.transaction.impl;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.transaction.Transaction;
import org.pscafepos.backends.database.transaction.TransactionEventHandler;
import org.pscafepos.backends.database.transaction.TransactionException;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bagmanov
 *         Date: 27.08.2009
 */
public class PosTransaction implements Transaction {
    private final static int STARTED = 1;
    private final static int COMMITED = 2;
    private final static int ROLLBACKED = 3;
    private final static int FAILED = 4;

    private Connection connection;
    private IPosSettings posSettings;

    private int transactionState = 0;
    private List<TransactionEventHandler> eventHandlers;

    public PosTransaction(IPosSettings posSettings, Connection connection) {
        this.posSettings = posSettings;
        this.connection = connection;
        eventHandlers = new ArrayList<TransactionEventHandler>();
    }


    public void start() throws TransactionException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    public void addToTransaction(TransactionalDAO... daos){
        for (TransactionalDAO dao : daos) {
            dao.joinTransaction(this);
        }
    }

    public void commit() throws TransactionException {
        try {
            connection.commit();
            connection.setAutoCommit(false);
            for (TransactionEventHandler eventHandler : eventHandlers) {
                eventHandler.onCommit();
            }
            connection.close();
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    public void rollback() throws TransactionException {
        try {
            connection.rollback();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionException(e);
        } finally {
            JdbcUtils.closeIfNeededSilently(connection);
            for (TransactionEventHandler eventHandler : eventHandlers) {
                eventHandler.onRollback();
            }
        }
    }

    public Connection getConnection (){
        return connection;
    }

    public IPosSettings getPosSettings() {
        return posSettings;
    }

    public void addEventHandler(TransactionEventHandler eventHandler) {
        eventHandlers.add(eventHandler);

    }

}
