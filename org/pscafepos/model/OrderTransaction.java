package org.pscafepos.model;

import org.pscafepos.backends.domain.items.OrderItem;

import java.math.BigDecimal;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.text.NumberFormat;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class OrderTransaction implements Serializable {

    private long transactionId;
    private Order order;
    private Student student;
    private BigDecimal cash;
    private BigDecimal credit;
    private BigDecimal change;
    private String cashier;

    public OrderTransaction(Order order) {
        this.order = order;
        this.student = order.getBuyer();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Student getStudent() {
        return student;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(transactionId);
        out.writeObject(this.cash);
        out.writeObject(this.cashier);
        out.writeObject(this.change);
        out.writeObject(this.credit);
        out.writeObject(this.student);
        out.writeObject(this.order.getOrderItems());
        out.writeObject(this.order.getPrice());

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.transactionId = in.readLong();
        this.cash = (BigDecimal) in.readObject();
        this.cashier = (String) in.readObject();
        this.change = (BigDecimal) in.readObject();
        this.credit =(BigDecimal) in.readObject();
        this.student = (Student) in.readObject();
        final OrderItem[] items = (OrderItem[]) in.readObject();
        final BigDecimal price = (BigDecimal) in.readObject();
        this.order = new Order() {
            public String getTitle() {
                return "restored title";
            }

            public void addItem(OrderItem item) {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public void addItems(List<OrderItem> items) {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public void clearItems() {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public boolean removeItem(OrderItem item) {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public void removeLasItem() {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public BigDecimal getPrice() {
                return price;
            }

            public String getOrderTotalString() {
                return NumberFormat.getCurrencyInstance().format(price);
            }

            public int getItemsCount() {
                return items.length;
            }

            public OrderItem[] getOrderItems() {
                return items;
            }

            public void setBuyer(Student student) {
                throw new UnsupportedOperationException("This object should be only used to update pos database");
            }

            public Student getBuyer() {
                return OrderTransaction.this.student;
            }
        };

    }
    
}
