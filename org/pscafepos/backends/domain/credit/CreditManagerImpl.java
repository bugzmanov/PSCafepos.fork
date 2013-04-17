package org.pscafepos.backends.domain.credit;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;
import org.pscafepos.backends.pos.IPosSettings;
import static org.pscafepos.util.Utils.round2Places;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class CreditManagerImpl extends BasePOSDao implements CreditManager {
    private static final Logger logger = Logger.getLogger(CreditManagerImpl.class.getName());

    public CreditManagerImpl() {
    }

    public CreditManagerImpl(Connection connector, IPosSettings settings) {
        super(connector, settings);
    }

    public BigDecimal getCredit(final Student student) throws CreditManagerException {
        String sql = "select * from " + posTablesPrefix + "studentcredit where credit_studentid = '" + student.getStudentNumber() + "' and credit_active = '1'";
        try {
            BigDecimal credit = executeSingleResultQuery(sql, new ResultsetProcessor<BigDecimal>() {
                public BigDecimal processEntity(ResultSet resultSet) throws SQLException {
                    return resultSet.getBigDecimal("credit_amount");
                }
            });
            return credit != null ? credit : BigDecimal.ZERO;
        } catch (DAOException e) {
            throw new CreditManagerException("POS database communication error", e);
        }
    }

    private boolean hasCreditStory(final Student student) throws CreditManagerException {
        String sql = "select count(*) as num from " + posTablesPrefix + "studentcredit where credit_studentid = '" + student.getStudentNumber() + "' and credit_active = '1'";
        try {
            Integer creditStoriesCount = executeSingleResultQuery(sql, new ResultsetProcessor<Integer>() {
                public Integer processEntity(ResultSet resultSet) throws SQLException {
                    return resultSet.getInt("num");
                }
            });
            if(creditStoriesCount != null) {
                if(creditStoriesCount > 1) {
                    throw new CreditManagerException("Student '"+ student.getStudentNumber() + "' has more than 1 credit stories associated");
                }
                return creditStoriesCount == 1;
            } else {
                return false;
            }
        } catch (DAOException e) {
            throw new CreditManagerException("POS database communication error", e);
        }
    }

    public void saveCreditTransaction(OrderTransaction transaction) throws CreditManagerException {
        if (hasCreditStory(transaction.getStudent())) {
            try {
                String updateCreditSQL = "update " + posTablesPrefix + "studentcredit set credit_amount = credit_amount - " +
                        transaction.getCredit() + ", credit_lastused = '" + currentTimeStamp() + "' where credit_active = '1' and credit_studentid = '" +
                        transaction.getStudent().getStudentNumber() + "'";
                int rowsUpdated = executeUpdateSql(updateCreditSQL);
                if (rowsUpdated > 1) {
                    throw new CreditManagerException("There is more than 1 credit record associated with student " + transaction.getStudent().getStudentNumber());
                }
            } catch (DAOException e) {
                throw new CreditManagerException("Couldn't update credit log entry", e);
            }
        } else {
            createNewCreditStory(transaction.getStudent(), transaction.getCredit());
        }
        createCreditLog(transaction);
    }

    private void createNewCreditStory(Student student, BigDecimal creditAmount) throws CreditManagerException {
        String createCreditSql = "insert into " + posTablesPrefix + "studentcredit (credit_amount,credit_active,credit_studentid, credit_createdatetime, " +
                "credit_lastused) values(" + round2Places((-1) * creditAmount.doubleValue()) + ",'1','" + student.getStudentNumber()
                + "', '" + currentTimeStamp() + "', '" + currentTimeStamp() + "')";
        try {
            if (executeUpdateSql(createCreditSql) != 1) {
                throw new CreditManagerException("Couldn't create credit story");
            }
        } catch (DAOException e) {
            throw new CreditManagerException("Couldn't create credit story", e);
        }
    }

    private void createCreditLog(OrderTransaction transaction) throws CreditManagerException {
        String createCreditLogSql = "insert into " + posTablesPrefix + "studentcredit_log ( scl_studentid, scl_action, scl_transid, scl_datetime ) " +
                "values( '" + transaction.getStudent().getStudentNumber() + "', " +
                transaction.getCredit().negate() + ", " + transaction.getTransactionId() + ", '" + currentTimeStamp() + "' )";
        try {
            if (executeUpdateSql(createCreditLogSql) != 1) {
                throw new CreditManagerException("Couldn't create credit log entry");
            }
        } catch (DAOException e) {
            throw new CreditManagerException("Couldn't create credit log entry", e);
        }
    }
}
