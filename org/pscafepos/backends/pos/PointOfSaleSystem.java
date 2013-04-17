package org.pscafepos.backends.pos;

import org.pscafepos.backends.domain.items.ItemsPackage;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;
import org.pscafepos.model.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bagmanov
 */
public interface PointOfSaleSystem {

    public boolean connect (String login, String password);

    public Order createNewOrder(String orderTitle) ;
    public List<String> listItemCategories() throws PointOfSaleSystemException;
    public List<OrderItem> listItems(String category) throws PointOfSaleSystemException;
    public List<ItemsPackage> listItemsPackages(String category) throws PointOfSaleSystemException;
    public void processOrder(OrderTransaction orderTransaction) throws PointOfSaleSystemException;
    public boolean hadIdenticalOrderInCurrentSession(OrderTransaction orderTransaction);
    public List<OrderItem> listHotbarItems() throws PointOfSaleSystemException;

    public boolean isSpecialSaleAllowed(Student student, int mealType) throws PointOfSaleSystemException;

    public BigDecimal getStudentCredit(Student student) throws PointOfSaleSystemException;
    public List<OrderItem> findItemsByBarcode(String barCode) throws PointOfSaleSystemException;

    public Order getStudentsLastOrder(Student student) throws PointOfSaleSystemException;
    String getGlobalPosMessage();
    String getProperty(String key);
    Boolean getBooleanProperty(String key);

    public void close();
}
