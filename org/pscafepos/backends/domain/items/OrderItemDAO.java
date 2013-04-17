package org.pscafepos.backends.domain.items;

import org.pscafepos.backends.database.TransactionalDAO;

import java.util.List;

import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 */
public interface OrderItemDAO extends TransactionalDAO {

    List<OrderItem> findByBarCode(String barCode) throws DAOException;

    List<OrderItem> findByCategory(String category) throws DAOException;

    List<OrderItem> listAutoAddItems(int registrationID) throws DAOException;

    List<OrderItem> listLastOrderItems(String studentId) throws DAOException;
}
