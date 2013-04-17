package org.pscafepos.backends.domain.items.impl;

import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.OrderItemDAO;
import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.util.Utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 *         Date: 25.08.2009
 */
public class OrderItemDAOImpl extends BasePOSDao implements OrderItemDAO {

    private static final Logger logger = Logger.getLogger(OrderItemDAOImpl.class.getName());
    public static final OrderItemProcessor ITEM_PROCESSOR = new OrderItemProcessor();
    private String buildingNumber;
    Map<String, List<OrderItem>> categoryItemscach;
    Map<Integer, OrderItem> itemIdItemscach;
    Map<String, List<OrderItem>> barCodeItemscach;
    Map<Integer, List<OrderItem>> regIdAutoAddItesmCach;

    public OrderItemDAOImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        categoryItemscach = new HashMap<String, List<OrderItem>>();
        itemIdItemscach = new HashMap<Integer, OrderItem>();
        barCodeItemscach = new HashMap<String, List<OrderItem>>();
        regIdAutoAddItesmCach = new HashMap<Integer, List<OrderItem>>();

    }

    public OrderItemDAOImpl(Connection connection, IPosSettings settings, String buildingNumber) {
        super(connection, settings);
        this.buildingNumber = buildingNumber;
    }

    private OrderItem findByItemId(int itemid) throws DAOException {
        OrderItem item = itemIdItemscach.get(itemid);
        if(item != null) {
            return (OrderItem) item.clone();
        }
        String sql = "select * from " + posTablesPrefix + "items where item_building = '" + buildingNumber + "' " +
                "and item_visible = '1' and item_id = " + itemid;
        item = executeSingleResultQuery(sql, ITEM_PROCESSOR);
        if (item.completeItem()){
            itemIdItemscach.put(itemid, item);
            return (OrderItem) item.clone();
        }else{
            logger.log(Level.WARNING, "OrderItem entity was not properly filled [" + item.toString() + "]");
            return null;
        }
    }

    public List<OrderItem> findByBarCode(String barCode) throws DAOException {
        List<OrderItem> items  = barCodeItemscach.get(barCode);
        if(items != null) {
            return items;
        }
        String sql = "select ibItemID, ibType from " + posTablesPrefix + "item_barcodes where ibBarcode = '" + barCode + "'";
        List<Integer> ids = executeQuery(sql, new ResultsetProcessor<Integer>() {
            public Integer processEntity(ResultSet resultSet) throws SQLException {
                if(resultSet.getString("ibType").equals("1")){
                    return resultSet.getInt("ibItemID");
                } else {
                    logger.log(Level.WARNING, "Adding Batch items via barcode scan is not yet supported!");
                    return null;
                }
            }
        });
        List<OrderItem> orderItemList = listByIds(ids);
        barCodeItemscach.put (barCode, Collections.unmodifiableList(orderItemList));
        return orderItemList;
    }

    private List<OrderItem> listByIds(List<Integer> ids) throws DAOException {
        List<OrderItem> result = new ArrayList<OrderItem>();
        for (Integer id : ids) {
            if(id != null) {
                OrderItem orderItem = findByItemId(id);
                if(orderItem != null) {
                    result.add(orderItem);
                }
            }
        }
        return result;
    }


    public List<OrderItem> findByCategory(String category) throws DAOException {
        List<OrderItem> items = categoryItemscach.get(category);
        if(items != null){
            return items;
        }
        String sql = "select * from " + posTablesPrefix + "items where item_building = '" + buildingNumber + "' and " +
                "item_visible = '1' " +
                "and item_category = '" + category + "' order by item_name";
        List<OrderItem> list = executeQuery(sql, ITEM_PROCESSOR);
        for (final OrderItem orderItem : list) {
            String selectItemDescSQL = "select * from " + posTablesPrefix + "item_desc where item_id = " + orderItem.getDBID();
            executeQuerySilently(selectItemDescSQL, new ResultsetProcessor<Object>() {
                public Object processEntity(ResultSet resultSet) throws SQLException {
                    orderItem.setIco(resultSet.getBytes("ico"));
                    return null;
                }
            });
        }
        categoryItemscach.put(category, Collections.unmodifiableList(list));
        return list;

    }
 

    public List<OrderItem> listAutoAddItems(int registrationId) throws DAOException {
        if(regIdAutoAddItesmCach.containsKey(registrationId)){
            return regIdAutoAddItesmCach.get(registrationId);
        }
        String sql = "select paa_itemid from " + posTablesPrefix + "pos_autoadditems where " +
                "paa_posid = " + registrationId + "";
        List<Integer> itemIds = executeQuery(sql, new ResultsetProcessor<Integer>() {
            public Integer processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt("paa_itemid");
            }
        });
        List<OrderItem> orderItemList = listByIds(itemIds);
        regIdAutoAddItesmCach.put(registrationId, Collections.unmodifiableList(orderItemList));
        return orderItemList;
    }

    public List<OrderItem> listLastOrderItems(String studentId) throws DAOException {
        String sql = "select tm_id from " + posTablesPrefix + "trans_master where " +
                "tm_register = '" + Utils.getHostName() + "' and  " +
                "tm_building = '" + buildingNumber + "' and " +
                "tm_studentid = '" + studentId + "' order by tm_datetime  desc";
//        String sql = "select max (ti_tmid) from "+posTablesPrefix+"trans_item where tm_studentid='" + studentId + "'";
        Long transactionId = executeSingleResultQuery(sql, new ResultsetProcessor<Long>() {
            public Long processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getLong("tm_id");
            }
        });
        if(transactionId != null) {
            String selectOrderSQL = "select * from " +
                "(SELECT * FROM " + posTablesPrefix + "trans_item WHERE ti_tmid = " + transactionId + ") " + posTablesPrefix + "trans_item " +
                "inner join (SELECT * FROM " + posTablesPrefix + "items WHERE item_visible = '1') " + posTablesPrefix + "items on " +
                "( " + posTablesPrefix + "trans_item.ti_itemid = " + posTablesPrefix + "items.item_id ) ";
            return executeQuery(selectOrderSQL, ITEM_PROCESSOR);
        } else {
            return Collections.emptyList();
        }
    }

    public static class OrderItemProcessor implements ResultsetProcessor<OrderItem> {
        public OrderItem processEntity(ResultSet resultSet) throws SQLException {
            int id = resultSet.getInt("item_id");
            String name = resultSet.getString("item_name");
            String desc = resultSet.getString("item_description");
            String category = resultSet.getString("item_category");
            String build = resultSet.getString("item_building");
            BigDecimal price = resultSet.getBigDecimal("item_price");
            BigDecimal redPrice = resultSet.getBigDecimal("item_reducedprice");
            boolean free = resultSet.getString("item_allowfree").equals("1");
            boolean reduced = resultSet.getString("item_allowreduced").equals("1") ;
            boolean typeA = resultSet.getString("item_istypea").equals("1");
            int freeBL = resultSet.getInt("item_fr_bl");
            return new OrderItem(id, name, desc, category, build, price, redPrice, free, reduced, typeA, freeBL);
        }
    }
}
