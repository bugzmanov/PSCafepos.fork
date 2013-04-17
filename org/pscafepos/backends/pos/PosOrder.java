/*   PSCafePOS is an Open Source Point of Sale System for Schools
 *   Copyright (C) 2007 Charles Syperski
 *
 *   This program is free software; you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation; either version 2 of the License, or 
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License 
 *   along with this program; if not, write to the 
 *   Free Software Foundation, Inc., 
 *   59 Temple Place, Suite 330, 
 *   Boston, MA 02111-1307 USA
 */
package org.pscafepos.backends.pos;

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.meal.MealManager;

import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;

import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.model.Order;
import org.pscafepos.model.Student;

public class PosOrder implements Order {

    private static final Logger logger = Logger.getLogger(PosOrder.class.getName());
    private String orderTitle;
    private List<OrderItem> vItems;

    private NumberFormat moneyFormat;
    private Student student;
    private MealManager mealManager;

    public PosOrder(String title, MealManager mealManager) {
        this.mealManager = mealManager;
        orderTitle = title;
        vItems = new ArrayList<OrderItem>();
        moneyFormat = NumberFormat.getCurrencyInstance();
    }

    public String getTitle() {
        return orderTitle;
    }

    public void addItem(OrderItem item) {
        vItems.add(item);
        updateItemsPricingForStudent(student);
    }

    public void addItems(List<OrderItem> items) {
        vItems.addAll(items);
        updateItemsPricingForStudent(student);
    }

    public void clearItems() {
        vItems.clear();
    }

    public boolean removeItem(OrderItem item) {
        try {
            boolean result = vItems.remove(item);
            updateItemsPricingForStudent(student);
            return result;
        }
        catch (Exception e) {
            return false;
        }
    }

//    public void removeItem(int itemIndex) {
//        if(itemIndex < vItems.size()) {
//            vItems.remove(vItems.get(itemIndex));
//        }
//    }

    public void removeLasItem() {
        if (!vItems.isEmpty()) {
            OrderItem lastOrderItem = vItems.get(vItems.size() - 1);
            vItems.remove(lastOrderItem);
            updateItemsPricingForStudent(student);
        }
    }

    public BigDecimal getPrice() {
//        double dblTotal = 0d;
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : vItems) {
            if (item.completeItem()) {
                total = total.add(item.getEffectivePrice());
//                dblTotal += item.getEffectivePrice();
            }
        }
//        return dblTotal;
        return total;
    }

    public String getOrderTotalString() {
        return moneyFormat.format(getPrice());
    }

    public int getItemsCount() {
        return vItems.size();
    }

    public OrderItem[] getOrderItems() {
        if (getItemsCount() > 0) {
            OrderItem items[] = new OrderItem[vItems.size()];
            vItems.toArray(items);
            return items;
        }
        return null;
    }

    public void setBuyer(Student student) {
        this.student = student;
        updateItemsPricingForStudent(student);
    }

    public Student getBuyer(){
        return this.student;
    }

    private void updateItemsPricingForStudent(Student student) {
        if (getItemsCount() > 0) {
            OrderItem[] items = getOrderItems();
            if (student != null && (student.canGetFreeMeal() || student.canGetReducedMeal())) {
                boolean gotSpecialBreakfast = !canGetSpecialBreakfast(student);
                boolean gotSpecialLunch = !canGetSpecialLunch(student);
                if (student.canGetFreeMeal()) {
                    for (OrderItem item : items) {
                        if (item.isFree()) {
                            if (item.isSpecialBrekfast() && !gotSpecialBreakfast) {
                                item.sellAsFree(true);
                                gotSpecialBreakfast = true;
                            } else if (item.isSpecialLunch() && !gotSpecialLunch) {
                                item.sellAsFree(true);
                                gotSpecialLunch = true;
                            } else {
                                item.sellAsFree(false);
                            }
                        }
                    }
                } else if (student.canGetReducedMeal()) {
                    for (OrderItem item : items) {
                        if (item.isReduced()) {
                            if (item.isSpecialBrekfast() && !gotSpecialBreakfast) {
                                item.sellAsReduced(true);
                                gotSpecialBreakfast = true;
                            } else if (item.isSpecialLunch() && !gotSpecialLunch) {
                                item.sellAsReduced(true);
                                gotSpecialLunch = true;
                            } else {
                                item.sellAsReduced(false);
                            }
                        }
                    }
                }
            } else {
                // student is null
                for (OrderItem item : items) {
                    item.sellAsFree(false);
                    item.sellAsReduced(false);
                }
            }
        }

    }

    private boolean canGetSpecialLunch(Student student) {
        try {
            return mealManager.canGetSpecialMeal(student, OrderItem.MEAL_TYPE_FREE_REDUCED_LUNCH);
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Could get whether can studet get free lunch [" + student.toString() + "]");
        }
        return false;
    }

    private boolean canGetSpecialBreakfast(Student student)  {
        try {
            return mealManager.canGetSpecialMeal(student, OrderItem.MEAL_TYPE_FREE_REDUCED_BREAKFAST);
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Could get whether can studet get free breakfast [" + student.toString() + "]");
        }
        return false;
    }
}
