package org.pscafepos.backends.domain.inventory;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.impl.OrderItemDAOImpl;
import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author bagmanov
 *         Date: 27.08.2009
 */
public class InventoryDAOImpl extends BasePOSDao implements InventoryDAO {
    private String buildingNumber;

    public InventoryDAOImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public InventoryDAOImpl(Connection connector, IPosSettings settings, String buildingNumber) {
        super(connector, settings);
        this.buildingNumber = buildingNumber;
    }

    public void deleteItemFromInventory(int itemId) throws DAOException {
        String selectSql = "SELECT inv_id from " + posTablesPrefix + "inventory where inv_menuid = " + itemId;
        Integer inventoryId = executeSingleResultQuery(selectSql, new ResultsetProcessor<Integer>() {
            public Integer processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt("inv_id");
            }
        });
        if (inventoryId != null && inventoryId != -1) {
            String deleteSql = "delete from " + posTablesPrefix + "inventory where inv_id = " + inventoryId;
            executeUpdateSql(deleteSql);
        }
    }

    public void updateHotbar(OrderItem[] items, String cashier) throws DAOException {
        for (OrderItem item : items) {
            if (isHotbarForItemExists(item, cashier)) {
                incrementItemPopularity(item, cashier);
            } else {
                createHotbarForItem(item, cashier);
            }
        }
    }

    public void clearHotBar(String cashier) throws DAOException {
        String sql = "delete from " + posTablesPrefix + "hotbar where " +
                "hb_building = '" + buildingNumber + "' " +
                "and hb_register = '" + Utils.getHostName() + "' " +
                "and hb_cashier = '" + cashier + "'";
        executeUpdateSql(sql);
    }

    private boolean isHotbarForItemExists(OrderItem item, String cashier) throws DAOException {
        String sql = "select count(*) from " + posTablesPrefix + "hotbar where " +
                "hb_itemid = " + item.getDBID() + " " +
                "and hb_building = '" + buildingNumber + "' " +
                "and hb_register = '" + Utils.getHostName() + "' " +
                "and hb_cashier = '" + cashier + "'";
        Integer count = executeSingleResultQuery(sql, new ResultsetProcessor<Integer>() {
            public Integer processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt(1);
            }
        });
        return count > 0;
    }

    private void createHotbarForItem(OrderItem item, String cashier) throws DAOException {
        String sql = "insert into " + posTablesPrefix + "hotbar " +
                "( hb_itemid, hb_building, hb_register, hb_cashier, hb_count ) values" +
                "( " + item.getDBID() + ", '" + buildingNumber + "', '" + Utils.getHostName() + "', '" + cashier + "', 1 )";
        executeUpdateSql(sql);
    }

    public List<OrderItem> listHotBarItems(String cashier) throws DAOException {
        String sql = "select * from (SELECT * FROM " + posTablesPrefix + "hotbar WHERE " +
                "hb_register = '" + Utils.getHostName() + "' AND " +
                "hb_cashier = '" + cashier + "' and " +
                "hb_building = '" + buildingNumber + "' ) " + posTablesPrefix + "hotbar inner join " +
                "(SELECT * FROM " + posTablesPrefix + "items WHERE item_visible = '1') " + posTablesPrefix + "items on " +
                    "( " + posTablesPrefix + "hotbar.hb_itemid = " + posTablesPrefix + "items.item_id ) order by hb_count desc ";
        return executeQuery(sql, OrderItemDAOImpl.ITEM_PROCESSOR);
    }
    
    private void incrementItemPopularity(OrderItem item, String cashier) throws DAOException {
        String sql = "update " + posTablesPrefix + "hotbar set hb_count = hb_count + 1 where " +
                "hb_itemid = " + item.getDBID() + " " +
                "and hb_building = '" + buildingNumber + "' " +
                "and hb_register = '" + Utils.getHostName() + "' " +
                "and hb_cashier = '" + cashier + "'";
        executeUpdateSql(sql);
    }
}
