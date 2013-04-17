package org.pscafepos.service.offline.repilcator;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author bagmanov
 */
public class BillingFeesReplicator {
    private static final Logger LOG = Logger.getLogger(BillingFeesReplicator.class.getName());
    private JdbcConnector locaConnector;
    private JdbcConnector remoteConnector;

    public BillingFeesReplicator(JdbcConnector locaConnector, JdbcConnector remoteConnector) {
        this.locaConnector = locaConnector;
        this.remoteConnector = remoteConnector;
    }

    public void replicate() {
        Connection remoteConnection = null;
        Connection localConnection = null;
        try {
            localConnection = locaConnector.getNewConnection();
            remoteConnection = remoteConnector.getNewConnection();
            remoteConnection.setAutoCommit(false);
            Statement statement = localConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from billing_fees");
            PreparedStatement preparedStatement = remoteConnection.prepareStatement(
                    "insert into billing_fees (id, student_id, assigned_date, due_date, school_id, syear, title, amount)" +
                        " VALUES (nextval('BILLING_FEES_SEQ'), ?, ?, ?, ?, ?, ?, ?)");
            while(resultSet.next()){
                preparedStatement.setLong(1, resultSet.getLong("student_id"));
                preparedStatement.setTimestamp(2, resultSet.getTimestamp("assigned_date"));
                preparedStatement.setTimestamp(3, resultSet.getTimestamp("due_date"));
                preparedStatement.setLong(4, resultSet.getLong("school_id"));
                preparedStatement.setLong(5, resultSet.getInt("syear"));
                preparedStatement.setString(6, resultSet.getString("title"));
                preparedStatement.setBigDecimal(7, resultSet.getBigDecimal("amount"));
                preparedStatement.executeUpdate();
                preparedStatement.clearParameters();
            }
            preparedStatement.close();
            resultSet.close();
            remoteConnection.commit();
            try {
                statement.executeUpdate("delete from billing_fees");
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "WARN!!! After BILLING_FEES replication failed to clean local DB. This will result in data doublication");
            }
            statement.close();
        } catch (JdbcConnectorException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            JdbcUtils.rollbackSilently(remoteConnection);
        } finally {
            JdbcUtils.closeIfNeededSilently(localConnection);
            JdbcUtils.closeIfNeededSilently(remoteConnection);
        }
    }
}
