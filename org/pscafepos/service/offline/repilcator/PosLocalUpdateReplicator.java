package org.pscafepos.service.offline.repilcator;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.configuration.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pscafepos.util.StringUtils;

/**
 * @author Bagmanov
 *         Date: 08.09.2009
 */
public class PosLocalUpdateReplicator {

    private static final Logger logger = Logger.getLogger(PosLocalUpdateReplicator.class.getName());

    private static final String[] TRANS_ITEMS_FIELDS = {"ti_itemid", "ti_pricesold", "ti_registerid",
            "ti_cashier", "ti_studentid", "ti_isfree", "ti_isreduced", "ti_datetime", "ti_istypea"};
    private JdbcConnector locaConnector;
    private JdbcConnector remoteConnector;
    private IPosSettings settings;
    private static final String[] CREDIT_LOG_FIELDS = {"scl_studentid", "scl_action", "scl_datetime"};

    public PosLocalUpdateReplicator(JdbcConnector locaConnector, JdbcConnector remoteConnector, IPosSettings settings) {
        this.locaConnector = locaConnector;
        this.remoteConnector = remoteConnector;
        this.settings = settings;
    }

    public void replicate() {
        Connection localPOSConnection = null;
        Connection remotePOSConnection = null;
        try {
            remotePOSConnection = remoteConnector.getNewConnection();
            localPOSConnection = locaConnector.getNewConnection();
            remotePOSConnection.setAutoCommit(false);
            Statement updateStatement = remotePOSConnection.createStatement();
            List<String> updateSQLs = getUpdateSQLs(localPOSConnection);
            for (String updateSQL : updateSQLs) {
                updateStatement.executeUpdate(updateSQL);
            }

            //todo: if local = null, i see no exceptional message on frontend. why??
            replicateOrderTransactions(remotePOSConnection, localPOSConnection);
            remotePOSConnection.commit();
            localPOSConnection.setAutoCommit(true);
            removeLocalUpdates(localPOSConnection);
            try {
                localPOSConnection.commit();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "WARN!!! After replication to remote database failed to clean local db. This will result in data doublication!");
            }
        } catch (JdbcConnectorException e) {
            logger.log(Level.SEVERE, "Couldn't connect to remote POS ");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            JdbcUtils.rollbackSilently(remotePOSConnection);
            JdbcUtils.rollbackSilently(localPOSConnection);
        } finally {
            JdbcUtils.closeIfNeededSilently(remotePOSConnection);
            JdbcUtils.closeIfNeededSilently(localPOSConnection);
        }
    }

    private void removeLocalUpdates(Connection localConnection) throws SQLException {
        Statement statement = null;
        try {
            statement = localConnection.createStatement();
//            statement.executeUpdate("delete from trans_item");
            statement.executeUpdate("delete from " + settings.getTablesPrefix() + "studentcredit_log");
            statement.executeUpdate("delete from " + settings.getTablesPrefix() + "trans_master");
            statement.executeUpdate("delete from " + settings.getTablesPrefix() + Constants.LOCALDB_UPDATEHOLDER_TABLE);
        } finally {
            closeSilently(statement);
        }


    }

    private void closeSilently(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e1) {
                //do nothing
            }
        }
    }

    private void replicateOrderTransactions(Connection remotePOSConnection, Connection localPOSConnection) throws SQLException {
        Statement statement = null;
        Statement remoteStatement = null;
        Statement subStatement = null;
        try {
            statement = localPOSConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            remoteStatement = remotePOSConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery("Select * from " + settings.getTablesPrefix() + "trans_master");
            subStatement = localPOSConnection.createStatement();
            while (resultSet.next()) {
                long remoteTransactionID = insertIntoTransMaster(resultSet, remoteStatement);
                long localTransactionID = resultSet.getLong("tm_id");
                ResultSet selectRS = subStatement.executeQuery("select * from  " + settings.getTablesPrefix() + "trans_item where ti_tmid = " + localTransactionID);
                replicateRelations(TRANS_ITEMS_FIELDS, "trans_item", selectRS, remotePOSConnection, "ti_tmid", remoteTransactionID);
                selectRS.close();
                subStatement.executeUpdate("update " + settings.getTablesPrefix() + "trans_item set ti_tmid = " + remoteTransactionID + " where ti_tmid = " + localTransactionID);
                selectRS = subStatement.executeQuery("select * from " + settings.getTablesPrefix() + "studentcredit_log where scl_transid = " + localTransactionID);
                replicateRelations(CREDIT_LOG_FIELDS, "studentcredit_log", selectRS, remotePOSConnection, "scl_transid", remoteTransactionID);
                selectRS.close();
                subStatement.executeUpdate("update " + settings.getTablesPrefix() + "studentcredit_log set scl_transid = " + remoteTransactionID + " where scl_transid = " + localTransactionID);
            }
            resultSet.close();
        } finally {
            closeSilently(remoteStatement);
            closeSilently(subStatement);
            closeSilently(statement);
        }

    }


    private void replicateRelations(String[] fields, String table, ResultSet selectRS, Connection remoteConnection, String foreignKeyName, long foreignKeyValue) throws SQLException {
        String fieldExpr = Arrays.toString(fields);
        fieldExpr = "(" + fieldExpr.substring(1, fieldExpr.length() - 1) + ", " + foreignKeyName + ")";
        String values = StringUtils.repeat("?, ", fields.length) + "?";

        PreparedStatement statement = null;
        try {
            statement = remoteConnection.prepareStatement("insert into " + settings.getTablesPrefix() + table + fieldExpr + " VALUES (" + values + ")");
            while (selectRS.next()) {
                for (int i = 0; i < fields.length; i++) {
                    statement.setObject(i + 1, selectRS.getObject(fields[i]));
                }
                statement.setLong(fields.length + 1, foreignKeyValue);
                statement.executeUpdate();
                statement.clearParameters();
            }
        } finally {
            closeSilently(statement);
        }


    }

    private long insertIntoTransMaster(ResultSet resultSet, Statement remoteStatement) throws SQLException {
        String strSql = "insert into " + settings.getTablesPrefix() + "trans_master ( tm_studentid, tm_total, tm_cashtotal, " +
                "tm_credittotal, tm_building, tm_register, tm_cashier, tm_datetime, tm_change, tm_buyer_type ) values( '" +
                resultSet.getString("tm_studentid") + "', " +
                resultSet.getBigDecimal("tm_total") + ", " +
                resultSet.getBigDecimal("tm_cashtotal") + ", " +
                resultSet.getBigDecimal("tm_credittotal") + ", '" +
                resultSet.getString("tm_building") + "', '" +
                resultSet.getString("tm_register") + "', '" +
                resultSet.getString("tm_cashier") + "', '" + resultSet.getTimestamp("tm_datetime") + "', " +
                resultSet.getBigDecimal("tm_change") + ",'" + resultSet.getString("tm_buyer_type") + "')";
        long masterID = -1;
        try {
            // this try is to catch postgres's inability to return auto generated ID's via JDBC :(
            remoteStatement.executeUpdate(strSql, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = remoteStatement.getGeneratedKeys();
            keys.next();
            masterID = keys.getLong(1);
            keys.close();
        } catch (Exception exRetKeys) {
            remoteStatement.executeUpdate(strSql);
            masterID = JdbcUtils.getLastInsertIDWorkAround(remoteStatement, settings.getTablesPrefix() + "trans_master_tm_id_seq");
        }
        return masterID;

    }

    private List<String> getUpdateSQLs(Connection localPOSConnection) {
        List<String> sqls = new ArrayList<String>();
        Statement selectStatement = null;
        try {
            selectStatement = localPOSConnection.createStatement();
            ResultSet updateSqlRS = selectStatement.executeQuery("select * from " + settings.getTablesPrefix() + Constants.LOCALDB_UPDATEHOLDER_TABLE);
            while (updateSqlRS.next()) {
                sqls.add(updateSqlRS.getString(1));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't get update expressions for replicating on remote POS", e);
        } finally {
            closeSilently(selectStatement);
        }
        return sqls;
    }
}
