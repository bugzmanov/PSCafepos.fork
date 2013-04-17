package org.pscafepos.backends.domain.billing;

import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.backends.database.dao.BaseJDBCDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * extending BasePOSDao is actually a hack to not use JTA transaction.. well BillingFee is a SIS table
 *
 * @author bagmanov
 */
public class BillingFeeDAOImpl extends BasePOSDao implements BillingFeeDAO{
    private static final Logger logger = Logger.getLogger(BillingFeeDAOImpl.class.getName());
    private static final String INSERT_SQL_SEQENCED  = "insert into billing_fees (id, student_id, assigned_date, due_date, school_id, syear, title, amount)" +
                        " VALUES (nextval('BILLING_FEES_SEQ'), ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_SQL_AUTOINC = "insert into billing_fees (student_id, assigned_date, due_date, school_id, syear, title, amount)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";

    private String buildingNumber;
    private Boolean useSequence = false;

    public BillingFeeDAOImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;

    }

    public BillingFeeDAOImpl(Connection connection, String buildingNumber) {
        super();
        setConnection(connection);
        this.buildingNumber = buildingNumber;
    }

    public void setSequencedMode() {
        useSequence = true;
    }
    public void setAutoIncrementMode() {
        useSequence = false;
    }

    public void saveBillingFees(Student student, Order order) throws DAOException {
        if(student.getType().equalsIgnoreCase("staff")){
            return;
        }
        Date assignedDate = new Date();
        Date dueDate = getDueDate();
        int schoolYear = currentSchoolYear();
        boolean newTransactionStarted = false;
        PreparedStatement statement = null;
        try {
            newTransactionStarted = connection.getAutoCommit();
            connection.setAutoCommit(false);
            if(useSequence) {
                statement = connection.prepareStatement(INSERT_SQL_SEQENCED);
            } else {
                statement = connection.prepareStatement(INSERT_SQL_AUTOINC);
            }
            statement.setLong(1, new Long(student.getStudentNumber()));
            statement.setTimestamp(2, new Timestamp(assignedDate.getTime()));
            statement.setTimestamp(3, new Timestamp(dueDate.getTime()));
            statement.setLong(4, new Long(buildingNumber));
            statement.setLong(5, schoolYear);
            for(OrderItem orderItem : order.getOrderItems()){
                statement.setString(6, orderItem.getName());
                statement.setBigDecimal(7, orderItem.getEffectivePrice());
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            JdbcUtils.rollbackSilently(connection);
            throw new DAOException(e);
        } finally {
            close(newTransactionStarted, statement);
        }
    }

    private Date getDueDate() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 1);
        return date.getTime();
    }

    private void close(boolean newTransactionStarted, PreparedStatement statement) {
        try {
            statement.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            if(newTransactionStarted){
            connection.setAutoCommit(false);
        }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static int currentSchoolYear(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        if(month < Calendar.AUGUST) {
            currentYear--;
        }
        return currentYear;
    }

    @Override
    public void setSettings(IPosSettings settings) {
        /* do nothing. this dao doesn't need settings */
    }
}
