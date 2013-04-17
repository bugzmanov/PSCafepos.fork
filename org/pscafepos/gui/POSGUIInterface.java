package org.pscafepos.gui;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.model.MoneyBuffer;

import javax.swing.*;

import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSStudentListener;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.ItemsPackage;

public interface POSGUIInterface {
    void loadGUI(ImagePackage iPackage);

    void setStatus(String status);

    void setStatus(String status, boolean vis);

    void setMode(int mode);

    void setCriticalMessage(String message);

    String getLoginUserName();

    String getLoginPassword();

    void refreshOrder(Order order);

    void loadCatgories(java.util.List<String> cats);

    public void loadItems(java.util.List<OrderItem> items, java.util.List<ItemsPackage> itemsPackageList);

    void loadStudent(Student stu, boolean showMealStatus, boolean blGotFRBreakfastToday, boolean blGotFRLunchToday, double creditAmount);

    void loadKeyManager(POSStudentListener p);

    void loadStudentImage(ImageIcon i);

    void loadCheckOutInfo(Order order);

    void updateBuffer(MoneyBuffer b, Order o);

    void disableUI();

    void enableUI();

    void toggleButtonText();

    void addHotbarItems(java.util.List<OrderItem> items);

    void loadKeyPad(POSEventListener listener);

    void loadStudentsLastOrder(Order o);

    void removeLastOrderScreen();
}
