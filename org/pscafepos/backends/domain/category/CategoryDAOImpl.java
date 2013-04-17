package org.pscafepos.backends.domain.category;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Collections;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class CategoryDAOImpl extends BasePOSDao implements CategoryDAO {
    private String buildingNumber;
    private List<String> categories;
    private List<String> batchCategories;

    public CategoryDAOImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public CategoryDAOImpl(Connection connector, IPosSettings settings, String buildingNumber) {
        super(connector, settings);
        this.buildingNumber = buildingNumber;
    }

    public List<String> listVisibleCategories() throws DAOException {
        if(categories != null) {
            return categories;
        }
        String sql = "select item_category from " + posTablesPrefix + "items where item_building = '"
                + buildingNumber + "' and item_visible = '1' " +
                "group by item_category order by item_category";
        List<String> visibleCategories = executeQuery(sql, new ResultsetProcessor<String>() {
            public String processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getString("item_category");
            }
        });
        categories = Collections.unmodifiableList(visibleCategories);
        return categories;
    }

    public List<String> listVisibleBatchCategories() throws DAOException {
        if(batchCategories != null) {
            return batchCategories;
        }
        String sql = "select " + posTablesPrefix + "batch_master.mb_category from " +
                "( SELECT * FROM " + posTablesPrefix + "batch_master WHERE " + posTablesPrefix + "batch_master.mb_active = '1' " +
                "and " + posTablesPrefix + "batch_master.mb_building = '" + buildingNumber + "') " +
                posTablesPrefix + "batch_master inner join " + posTablesPrefix + "batch_items on " +
                "( " + posTablesPrefix + "batch_master.mb_id = " + posTablesPrefix + "batch_items.ib_batchid ) " +
                "group by " + posTablesPrefix + "batch_master.mb_category";

        List<String> visibleBatchCategories = executeQuery(sql, new ResultsetProcessor<String>() {
            public String processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getString("mb_category");
            }
        });
        batchCategories = Collections.unmodifiableList(visibleBatchCategories);
        return batchCategories;
    }
}
