package org.pscafepos.backends.domain.credit;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;

import java.math.BigDecimal;

/**
 * @author bagmanov
 */
public interface CreditManager extends TransactionalDAO {
    BigDecimal getCredit(Student student) throws CreditManagerException;

    void saveCreditTransaction(OrderTransaction transaction) throws CreditManagerException;
}
