package org.pscafepos.backends.domain.billing;

import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.TransactionalDAO;

/**
 * @author bagmanov
 */
public interface BillingFeeDAO extends TransactionalDAO {
    public void saveBillingFees(Student student, Order order) throws DAOException;
}
