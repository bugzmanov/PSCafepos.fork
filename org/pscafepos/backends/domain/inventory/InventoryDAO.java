package org.pscafepos.backends.domain.inventory;

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.TransactionalDAO;

import java.util.List;

/**
 * @author bagmanov
 */
public interface InventoryDAO extends TransactionalDAO {
    //TODO: should throw exception
    void deleteItemFromInventory(int itemId) throws DAOException;

  
}
