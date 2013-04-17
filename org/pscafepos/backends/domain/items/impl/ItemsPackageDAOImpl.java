package org.pscafepos.backends.domain.items.impl;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.domain.items.ItemsPackageDAO;
import org.pscafepos.backends.domain.items.ItemsPackage;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class ItemsPackageDAOImpl extends BasePOSDao implements ItemsPackageDAO {

    private static final ItemsPackageResultProcessor ITEMS_PACKAGE_RESULT_PROCESSOR =  new ItemsPackageResultProcessor();
    private String buildingNumber;

    public ItemsPackageDAOImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public ItemsPackageDAOImpl(Connection connection, IPosSettings settings, String buildingNumber) {
        super(connection, settings);
        this.buildingNumber = buildingNumber;
    }

    public List<ItemsPackage> findByCategory(String category) throws DAOException {
        String sql = "select " + posTablesPrefix + "batch_master.mb_name, " + posTablesPrefix + "batch_master.mb_id " +
                "from " + posTablesPrefix + "batch_master where " +
                "" + posTablesPrefix + "batch_master.mb_active = '1' and " +
                "" + posTablesPrefix + "batch_master.mb_building = '" + buildingNumber + "' " +
                "and " + posTablesPrefix + "batch_master.mb_category = '" + category + "' " +
                "order by " + posTablesPrefix + "batch_master.mb_name";
        List<ItemsPackage> result = executeQuery(sql, ITEMS_PACKAGE_RESULT_PROCESSOR);
        for (ItemsPackage itemsPackage : result) {
            String subSql = "select " + posTablesPrefix + "items.* from " +
                    "(SELECT * FROM " + posTablesPrefix + "batch_items WHERE ib_batchid = '" + String.valueOf(itemsPackage.getId()) + "') " +
                    posTablesPrefix + "batch_items " +
                    "inner join " +
                    "(SELECT * FROM " + posTablesPrefix + "items WHERE item_visible = '1' and item_building = '" + buildingNumber + "') " +
                    posTablesPrefix + "items on " +
                    "( " + posTablesPrefix + "batch_items.ib_itemid = " + posTablesPrefix + "items.item_id ) " +
                    "order by item_name";
            List<OrderItem> orderItemList = executeQuery(subSql, OrderItemDAOImpl.ITEM_PROCESSOR);
            itemsPackage.addItems(orderItemList);
        }
        return result;
    }

    public static class ItemsPackageResultProcessor implements ResultsetProcessor<ItemsPackage> {
        public ItemsPackage processEntity(ResultSet rs) throws SQLException {
            String batchName = rs.getString("mb_name");
            int batchID = rs.getInt("mb_id");
            ItemsPackage result = new ItemsPackage(batchName);
            result.setId(batchID);
            return result;
        }
    }
}
