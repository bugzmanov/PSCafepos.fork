package org.pscafepos.service.offline;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.pos.IPosSettings;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bagmanov
 */
public class OrderTransactionRemover {
    private static final Logger logger = Logger.getLogger(OrderTransactionRemover.class.getName());

    private JdbcConnector localConnector;
    private JdbcConnector remoteConnector;
    private IPosSettings settings;
    public OrderTransactionRemover(JdbcConnector localConnector, JdbcConnector remoteConnector, IPosSettings settings) {
        this.localConnector = localConnector;
        this.remoteConnector = remoteConnector;
        this.settings = settings;
    }

    public void updateLocalTransactions() {
        Connection local = null;
        Connection remote = null;
        try {
            local = localConnector.getNewConnection();
            remote = remoteConnector.getNewConnection();
            local.setAutoCommit(false);
            try {
                Statement remoteStatement = remote.createStatement();
                ResultSet resultSet = remoteStatement.executeQuery("select * from " + settings.getTablesPrefix() + "removed_trans");
                Statement localStatement = local.createStatement();
                while(resultSet.next()){
                Long transactionId = resultSet.getLong("trans_id");
                    BigDecimal amount =  resultSet.getBigDecimal("refund_amount");
                    String studentId = resultSet.getString("student_id");
                    localStatement.executeUpdate("delete from "+settings.getTablesPrefix() +"trans_item where ti_tmid="+transactionId);
                    localStatement.executeUpdate("update "+settings.getTablesPrefix() +"studentcredit set credit_amount = ( credit_amount + " + amount + ") where credit_studentid = '"+studentId+"'");
                }
                local.commit();
                try {
                    remoteStatement.executeUpdate("delete from " + settings.getTablesPrefix()+"removed_trans");
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "WARN! Order transactions was removed from local db, but wasn't removed from remote", e);
                }
            } catch (SQLException e) {
                JdbcUtils.rollbackSilently(local);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            JdbcUtils.closeIfNeededSilently(local);
            JdbcUtils.closeIfNeededSilently(remote);
        }
    }
}
