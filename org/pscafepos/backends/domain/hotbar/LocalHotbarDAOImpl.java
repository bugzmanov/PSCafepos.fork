package org.pscafepos.backends.domain.hotbar;

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.database.dao.DAOException;

import java.util.*;

/**
 * @author bagmanov
 */
public class LocalHotbarDAOImpl implements HotbarDAO{
    Map<OrderItem, ModifiebleInt> stat;
    private ArrayList<OrderItem> hotbarList;

    public LocalHotbarDAOImpl() {
        stat = new HashMap<OrderItem, ModifiebleInt>();
        hotbarList = new ArrayList<OrderItem>();
    }

    public void updateHotbar(OrderItem[] items, String cashier) throws DAOException {
        for (OrderItem item : items) {
            ModifiebleInt statValue = stat.get(item);
            if(statValue == null){
                statValue = new  ModifiebleInt();
                stat.put(item, statValue);
            }
            statValue.inc();
        }
        ArrayList<Map.Entry<OrderItem, ModifiebleInt>> temp = new ArrayList<Map.Entry<OrderItem, ModifiebleInt>>(stat.entrySet());
        Collections.sort(temp, new Comparator<Map.Entry<OrderItem, ModifiebleInt>>() {
            public int compare(Map.Entry<OrderItem, ModifiebleInt> o1, Map.Entry<OrderItem, ModifiebleInt> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        hotbarList.clear();
        for (Map.Entry<OrderItem, ModifiebleInt> orderItemModifiebleIntEntry : temp) {
            hotbarList.add(orderItemModifiebleIntEntry.getKey());
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearHotBar(String cashier) throws DAOException {
        hotbarList.clear();
    }

    public List<OrderItem> listHotBarItems(String cashier) throws DAOException {
        return Collections.unmodifiableList(hotbarList);
    }

    private class ModifiebleInt implements Comparable<ModifiebleInt> {
        int value = 0;

        public ModifiebleInt() {
        }

        private ModifiebleInt(int value) {
            this.value = value;
        }

        public void inc() {
            value++;
        }

        public int compareTo(ModifiebleInt o) {
            return this.value - o.value;
        }
    }
}
