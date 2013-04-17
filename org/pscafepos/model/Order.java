package org.pscafepos.model;

import org.pscafepos.backends.domain.items.OrderItem;

import java.util.List;
import java.math.BigDecimal;

/**
 * @author Bagmanov
 *         Date: Sep 9, 2009
 */
public interface Order {
    String getTitle();

    void addItem(OrderItem item);

    void addItems(List<OrderItem> items);

    void clearItems();

    boolean removeItem(OrderItem item);

    void removeLasItem();

    BigDecimal getPrice();

    String getOrderTotalString();

    int getItemsCount();

    OrderItem[] getOrderItems();

    void setBuyer(Student student);

    Student getBuyer();
}
