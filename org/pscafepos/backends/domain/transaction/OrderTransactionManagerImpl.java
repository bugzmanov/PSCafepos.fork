package org.pscafepos.backends.domain.transaction;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.impl.OrderItemDAOImpl;
import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.util.Utils;
import static org.pscafepos.util.Utils.bool2int;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class OrderTransactionManagerImpl extends BasePOSDao implements OrderTransactionManager {
    private static final Logger logger = Logger.getLogger(OrderItemDAOImpl.class.getName());
    private String buildingNumber;
    java.util.Collection<TransactionRecord>  currentTransactionRecords;

    public OrderTransactionManagerImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        currentTransactionRecords = new ArrayList<TransactionRecord>();
    }

    public OrderTransactionManagerImpl(Connection connection, IPosSettings settings, String buildingNumber) {
        super(connection, settings);
        this.buildingNumber = buildingNumber;
        currentTransactionRecords = new ArrayList<TransactionRecord>();
    }

    public OrderTransaction save(OrderTransaction transaction) throws TransactionManagerException {
        Statement stmt = null;
        if(transaction.getOrder().getOrderItems() == null){
            throw new TransactionManagerException("Can't proceed transaction if order has no items");
        }
        try {
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            long transactionId = saveTransaction(stmt, transaction);
            if(transactionId != -1){
                transaction.setTransactionId(transactionId);
                saveOrderItems(transaction.getOrder().getOrderItems(), transactionId, transaction.getCashier(), transaction.getStudent());
                TransactionRecord record = new TransactionRecord(transaction.getStudent(), transaction.getOrder());
                currentTransactionRecords.add(record);
                return transaction;
            } else {
                throw new TransactionManagerException("Coouldn't get generated key. Please submit a bug report!");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TransactionManagerException("Couldn't save order transaction to database", e);
        } catch (DAOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TransactionManagerException("Couldn't save order transaction to database", e);
        } finally {
            if(stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    private void saveOrderItems(OrderItem[] orderItems, long transactionId, String cashier, Student student) throws DAOException {
        String timeStamp = currentTimeStamp();
        for (OrderItem orderItem : orderItems) {
            String sql = "insert into " + posTablesPrefix + "trans_item ( " +
                    "ti_itemid, ti_tmid, ti_pricesold, ti_registerid, ti_cashier, ti_studentid, ti_isfree, ti_isreduced, ti_datetime, " +
                    "ti_istypea ) values(" + orderItem.getDBID() + ", " + transactionId + ", " +
                    orderItem.getEffectivePrice() + ", '" + Utils.getHostName() + "', '" +
                    cashier + "', '" + student.getStudentNumber() + "', '" + bool2int(orderItem.isSoldAsFree()) + "', '" +
                    bool2int(orderItem.isSoldAsReduced()) + "', '"+ timeStamp +"', '" + bool2int(orderItem.isTypeA()) + "')";
            if(executeUpdateSql(sql) != 1) {
                throw new TransactionManagerException("Sql statement [" + sql + "] wasn't executed successfully");
            }
        }

    }

    private long saveTransaction(Statement stmt, OrderTransaction transaction) throws SQLException, TransactionManagerException {
        int intSqlReturnVal;
        long masterID;
        String building = buildingNumber;
//        String strSql = "insert into " + posTablesPrefix + "trans_master ( tm_studentid, tm_total, tm_cashtotal, tm_credittotal, tm_building, tm_register, tm_cashier, tm_datetime, tm_change ) values( '" +
//                transaction.getStudent().getStudentNumber() + "', '" +
//                transaction.getOrder().getPrice() + "', '" +
//                transaction.getCash() + "', '" +
//                transaction.getCredit() + "', '" +
//                building + "', '" +
//                Utils.getHostName() + "', '" +
//                transaction.getCashier() + "', '" + currentTimeStamp() + "', '" +
//                transaction.getCash() + "')";
        String strSql = "insert into " + posTablesPrefix + "trans_master ( tm_studentid, tm_total, tm_cashtotal, tm_credittotal, tm_building, tm_register, tm_cashier, tm_datetime, tm_change,tm_buyer_type ) values( '" +
                transaction.getStudent().getStudentNumber() + "', " +
                transaction.getOrder().getPrice() + ", " +
                transaction.getCash() + ", " +
                transaction.getCredit() + ", '" +
                building + "', '" +
                Utils.getHostName() + "', '" +
                transaction.getCashier() + "', '" + currentTimeStamp() + "', " +
                transaction.getChange() + "," +
                "'"+transaction.getStudent().getType()+"')";
        try {
            // this try is to catch postgres's inability to return auto generated ID's via JDBC :(
            intSqlReturnVal = stmt.executeUpdate(strSql,Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            masterID = keys.getLong(1);
            keys.close();
        } catch (Exception exRetKeys) {
            intSqlReturnVal = stmt.executeUpdate(strSql);
            masterID = JdbcUtils.getLastInsertIDWorkAround(stmt, posTablesPrefix + "trans_master_tm_id_seq");
            if (masterID == -1){
                logger.log(Level.SEVERE, "Coouldn't get generated key. It looks like the work around failed, please submit a bug report!", exRetKeys);
            }
        }
        if(intSqlReturnVal != 1) {
            throw new TransactionManagerException("[" + strSql + "] was not properly executed");
        }
        return masterID;
    }

    public boolean hadIdenticalTransactionInCurrentSession(OrderTransaction transaction) {
        TransactionRecord record = new TransactionRecord(transaction.getStudent(), transaction.getOrder());
        return currentTransactionRecords.contains(record);
    }

    private class TransactionRecord {
        private Student student;
        private OrderItem[] orderItems;
        private TransactionRecord(Student student, Order order) {
            this.student = student;
            this.orderItems = Arrays.copyOf(order.getOrderItems(), order.getItemsCount());
            Arrays.sort(this.orderItems, orderItemsComparator);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransactionRecord that = (TransactionRecord) o;

            if( !Arrays.equals(orderItems, that.orderItems)) return false;
            if (student != null ? !student.equals(that.student) : that.student != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = student != null ? student.hashCode() : 0;
            result = 31 * result + (orderItems != null ? Arrays.hashCode(orderItems) : 0);
            return result;
        }

    }
    private OrderItemComparator orderItemsComparator = new OrderItemComparator();

    private class OrderItemComparator implements Comparator<OrderItem> {
        public int compare(OrderItem o1, OrderItem o2) {
            return o1.getDBID() - o2.getDBID();
        }
    }
}
