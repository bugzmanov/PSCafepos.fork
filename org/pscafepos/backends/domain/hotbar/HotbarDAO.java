package org.pscafepos.backends.domain.hotbar;

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.database.dao.DAOException;

import java.util.List;

/**
 * @author bagmanov
 */
public interface HotbarDAO {

     void updateHotbar(OrderItem[] items, String cashier) throws DAOException;

    void clearHotBar(String cashier) throws DAOException;

    List<OrderItem> listHotBarItems(String cashier) throws DAOException;
}
