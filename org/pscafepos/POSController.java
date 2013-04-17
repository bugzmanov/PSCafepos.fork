package org.pscafepos;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.ItemsPackage;
import org.pscafepos.event.POSEvent;
import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSStudentListener;
import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.model.MoneyBuffer;
import org.pscafepos.backends.pos.PointOfSaleSystem;
import org.pscafepos.backends.pos.PointOfSaleSystemException;
import org.pscafepos.configuration.SessionSettings;
import org.pscafepos.configuration.Constants;
import org.pscafepos.backends.sis.SisException;
import org.pscafepos.backends.sis.StudentInformationSystem;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;

import org.pscafepos.gui.*;
import org.pscafepos.gui.swing.components.POSButton;
import org.pscafepos.gui.swing.components.POSKeyPad;
import org.pscafepos.gui.swing.components.OrderItemPanel;
import org.pscafepos.gui.swing.EventQueueProxy;
import org.pscafepos.drawer.CashDrawerManager;
import org.pscafepos.drawer.PSCashDrawer;

public class POSController implements POSEventListener, POSStudentListener {
    private static final Logger logger = Logger.getLogger(POSController.class.getName());

    private POSGUI gui;
    private SessionSettings sessionSettings;
    private PointOfSaleSystem saleSystem;
    private StudentInformationSystem studentSystem;

    private Order order;
    private Order lastOrder;
    //todo: what is this??
    private PSSplashScreen splash;
    private PSCashDrawer cashDrawer;
    private Student student;
    private int intMode;
    private MoneyBuffer paymentBuffer;
    private PSOrderSummary summary;
    private NumberFormat money;


    public static final int MODE_LOGIN = 0;
    public static final int MODE_ITEMS = 1;
    public static final int MODE_CHECKOUT = 2;


    public POSController(POSGUI gui, SessionSettings sessionSettings, PointOfSaleSystem saleSystem, StudentInformationSystem studentSystem) {
        this.gui = gui;
        gui.addPOSEventListener(this);
        this.sessionSettings = sessionSettings;
        this.saleSystem = saleSystem;
        this.studentSystem = studentSystem;
        paymentBuffer = new MoneyBuffer();
        buildController();
    }

    //    private StudentInformationSystem studentInformationSystem;
//    public org.pscafepos.POSController(org.pscafepos.gui.POSGUI org.pscafepos.gui, String org.pscafepos.settings) {
//        this.org.pscafepos.gui = org.pscafepos.gui;
//        org.pscafepos.gui.addPOSEventListener(this);
//        paymentBuffer = new org.pscafepos.model.MoneyBuffer();
//        this.settingsFile = new File(org.pscafepos.settings);
//        buildController();
//    }

    private void buildController() {
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        queue.push(new EventQueueProxy());
        money = NumberFormat.getCurrencyInstance();
        ImagePackage ip = new ImagePackage();
        ip.setImage(ImagePackage.IMG_BACKGROUND, "images/" + sessionSettings.getImageBackGround());
        gui.loadGUI(ip);
        gui.setStatus("Preparing login screen...please wait...");
        setGlobalMode(POSController.MODE_LOGIN);
        initCashDrawer(sessionSettings, gui);

    }

    private void initCashDrawer(SessionSettings sessionSettings, POSGUI gui) {
        if (sessionSettings.isDrawerEnabled()) {
            cashDrawer = CashDrawerManager.getDrawer(sessionSettings.getDrawerName(),
                    sessionSettings.getDrawerClass(), this);
            if (cashDrawer != null) {
                gui.setStatus("Cash drawer is active.");
            } else {
                gui.setStatus("Cash drawer is disabled.");
            }
        } else {
            gui.setStatus("Cash drawer is disabled.");
            cashDrawer = null;
        }
    }

    private void setGlobalMode(int m) {
        intMode = m;
        gui.setMode(m);
    }


    private void pushLastOrder() {
        boolean shouldShowLastOrder = saleSystem.getBooleanProperty("displayLastOrder");
        if (shouldShowLastOrder && student != null && !student.isAnonStudent()) {
            String id = student.getStudentNumber();
//            lastOrder = new PosOrder("Student's Last PosOrder");
            if (intMode == MODE_ITEMS && (id != null && !id.equals(Student.NOSTUDENT))) {
                gui.setStatus("Searching for student's last order...");
                //todo what to do with this replacement?
                id = id.replaceAll("[/']", "");

                try {
                    lastOrder = saleSystem.getStudentsLastOrder(student);
                    if (lastOrder != null && lastOrder.getItemsCount() > 0) {
                        gui.loadStudentsLastOrder(lastOrder);
                    }
                } catch (PointOfSaleSystemException e) {
                    logger.log(Level.SEVERE, "Couldn't get students last order", e);
                    gui.setStatus("Failed to load students last order, please refer to log", false);
                }
//                List<OrderItem> lastOrderItems = saleSystem.getItemsFromLastSale(id);
//                lastOrder.addItems(lastOrderItems);
//                if (!lastOrderItems.isEmpty()) {
//                    org.pscafepos.gui.loadStudentsLastOrder(lastOrder);
//                }
            }
        }
    }

    public void processStudentScan(String id) {
        if (intMode == MODE_ITEMS) {
            gui.setEnabled(false);
            gui.setStatus("Processing Scan...");
            id = normalizeId(id);
            try {
                Student tmpStu = studentSystem.getStudent(id);
                if (tmpStu.isAnonStudent() || tmpStu.getExistsInDB()) {
                    student = tmpStu;
                    order.setBuyer(student);
                    pushStudentObjToGUI();
                    pushLastOrder();
                    gui.setStatus("Student: " + id + " found!");
                    studentSystem.loadStudentImageAsync(student, this);
                    if (!tmpStu.isAnonStudent() && sessionSettings.isAutoCheckOutMode()) {
                        gui.setEnabled(true);
                        loadCheckOutScreen();
                        return;
                    }
                } else {
                    gui.setStatus("Student: " + id + " was not found, checking if it is an item's barcode");
                    tryAddItemByBarcode(id);
                }
            } catch (SisException e) {
                gui.setStatus("Error: " + e.getMessage(), true);
                tryAddItemByBarcode(id);
            }
            //TODO : can order be null here?
            gui.refreshOrder(order);
            gui.setEnabled(true);
        }
    }

    private String normalizeId(String id) {
        id = id.replaceAll("[/']", "");
        gui.setStatus("Checking for a prefix to remove from front of string...");
        String prefix = saleSystem.getProperty("rmPrefix");
        if (prefix != null) {
            if (id.toLowerCase().startsWith(prefix.toLowerCase())) {
                StringBuffer buf = new StringBuffer(id);
                buf.delete(0, prefix.length());
                id = buf.toString();
            }
        }
        return id;
    }

    private boolean isStaffId(String id) {
        return id.startsWith(Constants.STAFF_ID_PREFIX);
    }

    private void tryAddItemByBarcode(String barCode) {
        try {
            List<OrderItem> items = saleSystem.findItemsByBarcode(barCode);
            for (OrderItem item : items) {
                addItemToOrder(item);
            }
        } catch (PointOfSaleSystemException e) {
            logger.log(Level.INFO, "Couldn't retrieve items by barcode [" + barCode + "]", e);
            gui.setStatus("Failed to retrieve items by barcode " + barCode, true);
        }
    }

    private void pushStudentObjToGUI() {
        gui.removeLastOrderScreen();
        double studentCredit = 0;
        boolean gotSpecialBreakfast = false;
        boolean gotSpecialLunch = false;
        try {
            studentCredit = saleSystem.getStudentCredit(student).doubleValue();
            gotSpecialBreakfast = !saleSystem.isSpecialSaleAllowed(student, OrderItem.MEAL_TYPE_FREE_REDUCED_BREAKFAST);
            gotSpecialLunch = !saleSystem.isSpecialSaleAllowed(student, OrderItem.MEAL_TYPE_FREE_REDUCED_LUNCH);
        } catch (PointOfSaleSystemException e) {
            logger.log(Level.SEVERE, "Failed to load student's info", e);
            gui.setStatus("Warning: Student's meal and credit data was not loaded properly", true);
        }
        gui.loadStudent(student, sessionSettings.isShowFreeReducedData(),
                gotSpecialBreakfast,
                gotSpecialLunch,
                studentCredit);
        pushLastOrder();
    }

    private void pushStudentImage(ImageIcon i) {
        if (i != null) {
            if (intMode == MODE_ITEMS) {
                gui.loadStudentImage(i);
            }
        }
    }

    private void loadItems(boolean resetOrder) {
        setGlobalMode(MODE_ITEMS);
        if (resetOrder || order == null) {
            order = saleSystem.createNewOrder("Current Order");

        }
        gui.refreshOrder(order);
        pushHotbarItemsToGUI();
        resetStudent();
    }

    private void pushHotbarItemsToGUI() {
        try {
            gui.addHotbarItems(saleSystem.listHotbarItems());
        } catch (PointOfSaleSystemException e) {
            logger.log(Level.SEVERE, "Couldn't load hotbar items", e);
            gui.setStatus("Failed to load hotbar items for current cashier", true);
        }
    }

    private void resetStudent() {
        gui.loadStudent(student, false, false, false, 0d);
        pushLastOrder();
        if (intMode == MODE_ITEMS || intMode == MODE_CHECKOUT) {
            order.setBuyer(student);
            gui.refreshOrder(order);
        }
//        updateOrderPricing();
    }

    private void attemptLogin() {
        gui.setStatus("Attempting Login...");
        String login = gui.getLoginUserName();
        String password = gui.getLoginPassword();
        if (saleSystem.connect(login, password)) {
            sessionSettings.getPosSettings().setUserName(login);
            sessionSettings.getPosSettings().setPassword(password);
            String message = saleSystem.getGlobalPosMessage();
            if (message != null) {
                gui.setStatus(message, true);
            }
            gui.setStatus("Login Successful!");
            student = studentSystem.getAnonymousStudent();
            resetStudent();
            gui.loadKeyManager(this);
            loadItems(true);
        } else {
            gui.setStatus("Login Failed, please try again.", true);
        }
    }

    private void loadCatsToGUI() {
        try {
            gui.loadCatgories(saleSystem.listItemCategories());
        } catch (PointOfSaleSystemException e) {
            logger.log(Level.SEVERE, "Failed to load categories", e);
            gui.setStatus("Failed to load categories. Please refer to log", true);
        }
    }

    private void loadItemsToGUI(String cat) {
        try {
            List<OrderItem> orderItemList = saleSystem.listItems(cat);
//            List<ItemsPackage> list = saleSystem.listItemsPackages(cat);
            List<ItemsPackage> list = new ArrayList<ItemsPackage>();//saleSystem.listItemsPackages(cat);
            gui.loadItems(orderItemList, list);
        } catch (PointOfSaleSystemException e) {
            logger.log(Level.SEVERE, "Failed to load Category items [category =" + cat, e);
            gui.setStatus("Failed to load '" + cat + "' items. Please refer to log", true);
        }
    }

    private void addItemToOrder(OrderItem item) {
        if (intMode == MODE_ITEMS) {
            if (item != null) {
                if (orderHasItem(order, item)) {
                    if (!gui.promt("Are you sure you want to add this item twice?")) {
                        return;
                    }
                }
                gui.setStatus("Adding item: " + item.getName() + " please wait...");
                order.addItem((OrderItem) item.clone());
                gui.refreshOrder(order);
                gui.setStatus("Added item: " + item.getName());
            } else {
                gui.setStatus("Unable to add item because object reference is null.", true);
            }
        }
    }

    private boolean orderHasItem(Order order, OrderItem item) {
        if (order.getOrderItems() == null) {
            return false;
        }
        for (OrderItem o : order.getOrderItems()) {
            if (o.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private void drawerOpened() {
        gui.disableUI();
    }

    private void drawerClosed() {
        if (splash != null)
            splash.kill();

        if (summary != null) {
            summary.kill();
            resetOrder();
        }

        gui.enableUI();
    }

    private void processNoSale() {
        if (intMode == MODE_ITEMS) {
            if (order == null || order.getItemsCount() == 0) {
                if (cashDrawer != null) {
                    gui.setStatus("Processing no sale.  ");
                    cashDrawer.openDrawer();
                } else {
                    gui.setStatus("No cash drawer is currently loaded.", true);
                }
            } else
                gui.setStatus("No sale can only be done when there are no items in the current order.", true);
        } else
            gui.setStatus("No sale can only be done in item mode.", true);
    }


//    //todo this is busines logic should be moved to SaleSystem
//    private void updateOrderPricing() {
//        if (intMode != MODE_ITEMS && intMode != MODE_CHECKOUT) {
//            return;
//        }
//        saleSystem.updateOrderForStudent(order, student);
//        org.pscafepos.gui.refreshOrder(order);
//    }

    private void voidLastItem() {
        if (intMode == MODE_ITEMS) {
            if (order != null && order.getItemsCount() != 0) {
                order.removeLasItem();
                gui.refreshOrder(order);
//                updateOrderPricing();
            }
        }
    }

    private void voidItem(OrderItem item) {
        if (intMode == MODE_ITEMS) {
            if (order != null && order.getItemsCount() != 0) {
                if (item != null) {
                    if (order.removeItem(item)) gui.setStatus("Voided Item: " + item.getName());
                    gui.refreshOrder(order);
//                    updateOrderPricing();
                }
            }
        }
    }

    private void voidOrder() {
        if (intMode == MODE_ITEMS) {
            if (order != null && order.getItemsCount() != 0) {
                order.clearItems();
                gui.refreshOrder(order);
            }
        }
    }

    private void resetAnonStudent() {
        gui.setStatus("Reseting Student to an anonymous Student.");
        processStudentScan(Student.NOSTUDENT);
//        updateOrderPricing();
        gui.setStatus("Student reset.");
    }

    private void loadCheckOutScreen() {
        if (order == null || order.getItemsCount() == 0) {
            gui.setStatus("Can't load checkout screen because there are no items in the current order.", true);
            return;
        }
        if (!sessionSettings.isAnonymousTransactionsAllowed() && !student.isStudentSet()) {
            gui.setStatus("Anonymous transactions are currently not allowed.", true);
            return;
        }
        if (sessionSettings.isAllowOnlyActiveStudents() && !student.getIsActiveStudent()) {
            gui.setStatus("POS is set to process only students that are active in your sis ", true);
            return;
        }
        if (sessionSettings.isAllowOnlyStudentsExistedInSIS() && !student.getExistsInDB()) {
            gui.setStatus("POS is set to process only students that exist in your sis ", true);
            return;
        }
        if (sessionSettings.isSellOnCreditOnlyMode()) {
            paymentBuffer.flushAll();
            paymentBuffer.setCredit(order.getPrice());
            intMode = MODE_CHECKOUT;
            long time = System.currentTimeMillis();
            processOrder();
            System.out.println(System.currentTimeMillis() - time);
        } else {
            gui.setStatus("Loading Checkout Screen...");
            setGlobalMode(MODE_CHECKOUT);
            gui.removeLastOrderScreen();
            gui.loadCheckOutInfo(order);
            paymentBuffer.flushAll();
            gui.setStatus("Checkout screen ready");
        }

    }

    private void updateCashBuffer(BigDecimal amt) {
        if (intMode == MODE_CHECKOUT) {
            if (paymentBuffer != null) {
                if (amt.compareTo(BigDecimal.ZERO) > 0)
                    paymentBuffer.addCash(amt);
                else if (amt.compareTo(BigDecimal.ZERO) == 0) {
                    paymentBuffer.setCash(order.getPrice().subtract(paymentBuffer.getCredit()));
                }

                gui.updateBuffer(paymentBuffer, order);
            }
        }

    }

    private void updateCreditBuffer(BigDecimal amt) {
        if (intMode == MODE_CHECKOUT) {
            if (paymentBuffer != null) {

                if (amt.compareTo(BigDecimal.ZERO) > 0)
                    paymentBuffer.addCredit(amt);
                else if (amt.compareTo(BigDecimal.ZERO) == 0) {
                    //double sCred = getStudentCredit();
                    //if ( sCred > 0d )
                    //{
                    //    if ( sCred >= (order.getPrice() - paymentBuffer.getCash() ) )
                    //    {
                    paymentBuffer.setCredit(order.getPrice().subtract(paymentBuffer.getCash()));
                    //    }
                    //    else
                    //    {
                    //        paymentBuffer.setCredit( sCred );
                    //    }
                    //}
                }

                gui.updateBuffer(paymentBuffer, order);
            }
        }
    }

    private void clearCash() {
        if (intMode == MODE_CHECKOUT) {
            if (paymentBuffer != null) {
                paymentBuffer.flushCash();
                gui.updateBuffer(paymentBuffer, order);
            }
        }
    }

    private void clearCredit() {
        if (intMode == MODE_CHECKOUT) {
            if (paymentBuffer != null) {
                paymentBuffer.flushCredit();
                gui.updateBuffer(paymentBuffer, order);
            }
        }
    }

    private void returnToItemsScreen() {
        if (intMode == MODE_CHECKOUT) {
            gui.setStatus("Returning to items screen...");
            setGlobalMode(MODE_ITEMS);
            processStudentScan(student.getStudentNumber());
//            updateOrderPricing();
            pushHotbarItemsToGUI();
            gui.setStatus("Item Screen Loaded.");
        }
    }

    private void processOrder() {
        BigDecimal neg = sessionSettings.getMaxNegativeBalance();
        if (intMode == MODE_CHECKOUT) {
            if (paymentBuffer.getBufferTotal().compareTo(order.getPrice()) >= 0) {
                BigDecimal cash, credit, allowedCredit;
                BigDecimal studentsCredit;
                try {
                    studentsCredit = saleSystem.getStudentCredit(student);
                } catch (PointOfSaleSystemException e) {
                    logger.log(Level.SEVERE, "Couldn't get student's credit", e);
                    gui.setStatus("Failed to get student's credit info. Operation aborted", true);
                    return;
                }
                allowedCredit = studentsCredit;
                if (sessionSettings.isNegativeBalanceAllowed()) {
                    allowedCredit = allowedCredit.add(neg);
                }
                if (paymentBuffer.getCredit().compareTo(allowedCredit) <= 0) {
                    if (student.isStudentSet() || paymentBuffer.getCredit().compareTo(studentsCredit) <= 0) {
                        credit = order.getPrice().min(paymentBuffer.getCredit());
                        cash = order.getPrice().subtract(credit);
                        BigDecimal change = paymentBuffer.getCash().subtract(cash);
                        try {
                            if (process(cash, credit, change)) {
                                showOrderCompleted(credit.doubleValue(), change.doubleValue());
                            }
                        } catch (PointOfSaleSystemException e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                            gui.setStatus(e.getMessage(), true);
                        }
                    } else {
                        gui.setStatus("Can't allow negative balance on an anonymous student!", true);
                    }
                } else {
                    if (sessionSettings.isNegativeBalanceAllowed()) {
                        gui.setStatus("Sorry, maximum negative balance is " + money.format(neg) + "!", true);
                    } else {
                        gui.setStatus("Student does not have enough credit to process this order.", true);
                    }
                }
            } else {
                gui.setStatus("Buffer total is less then the order total.", true);
            }
        }
    }

    private boolean process(BigDecimal cash, BigDecimal credit, BigDecimal change) throws PointOfSaleSystemException {
        order.setBuyer(student);
        OrderTransaction orderTransaction = new OrderTransaction(order);
        orderTransaction.setCash(cash);
        orderTransaction.setCredit(credit);
        orderTransaction.setChange(change);
        if (saleSystem.hadIdenticalOrderInCurrentSession(orderTransaction)) {
            if (!gui.promt("You had already the same transaction during this session. \n Are you sure you want to proceed?")) {
                resetOrder();
                return false;
            }
        }
        saleSystem.processOrder(orderTransaction);
        return true;
    }

    private void showOrderCompleted(double credit, double change) {
        if (sessionSettings.isAutoCheckOutMode() && sessionSettings.isSellOnCreditOnlyMode()) {
            String status = "Order Complete. (" + student.getType() + " id: " + student.getStudentNumber() + ", Price: " + money.format(credit) + ")";
            resetOrder();
            gui.setStatus(status);
        } else {
            gui.setStatus("Order Complete.");
            gui.disableUI();
            summary = new PSOrderSummary(gui);

            if (cashDrawer != null)
                cashDrawer.openDrawer();
            else
                summary.setPOSEventListener(this);

            String studentRemainingCredit;
            try {
                studentRemainingCredit = money.format(saleSystem.getStudentCredit(student).doubleValue());
            } catch (PointOfSaleSystemException e) {
                logger.log(Level.SEVERE, "Failed to retrieve students credit data", e);
                studentRemainingCredit = "[Data is not available]";
            }
            summary.display(money.format(order.getPrice()),
                    money.format(paymentBuffer.getCash()),
                    money.format(credit),
                    money.format(change),
                    studentRemainingCredit);
        }
    }

    private void resetOrder() {
        gui.setStatus("Order Processed, preparing POS.");
        paymentBuffer.flushAll();
        setGlobalMode(MODE_ITEMS);
        processStudentScan(Student.NOSTUDENT);
        pushHotbarItemsToGUI();
        order = saleSystem.createNewOrder("Current Order");
        gui.refreshOrder(order);
//        updateOrderPricing();
        gui.setStatus("Order complete");
        gui.requestFocus();

        String message = saleSystem.getGlobalPosMessage();
        if (message != null) {
            gui.setStatus(message, true);
        }

    }

    private void exitSystem() {
        if (intMode == MODE_ITEMS) {
            if (order == null || order.getItemsCount() == 0) {
                gui.setStatus("Exiting System by user's request.");
                saleSystem.close();
                System.exit(0);
            } else
                gui.setStatus("Can not exit while there is an active order.", true);
        } else if (intMode == MODE_LOGIN) {
            gui.setStatus("Exiting System");
            System.exit(0);
        } else
            gui.setStatus("You must be in Item Mode to exit this POS.", true);
    }

    private void loadKeyPad() {
        gui.loadKeyPad(this);
    }

    private void processItemAdd(List<OrderItem> items) {
        for (Object item : items) {
            OrderItem singleItem = (OrderItem) item;
            if (singleItem != null)
                addItemToOrder((OrderItem) (singleItem.clone()));
        }
        gui.setStatus(items.size() + " item" + (items.size() > 1 ? "s" : "") + " added.");
    }

    private void activateLastStudentOrder() {
        gui.setStatus("Activating order...");
        gui.removeLastOrderScreen();
        order = lastOrder;
        gui.refreshOrder(order);
        gui.setStatus("Order is now active.");
    }

    public void onPosEvent(POSEvent posEvent) {
        if (posEvent.getEventType() == POSEvent.SYSTEM_LOGIN) {
            attemptLogin();
        } else if (posEvent.getEventType() == POSEvent.SYSTEM_EXIT) {
            exitSystem();
        } else if (posEvent.getEventType() == POSEvent.ITEMS_LOADCAT) {
            gui.setStatus("Searching for categories...");
            loadCatsToGUI();
            gui.setStatus("Catgories loaded.");
        } else if (posEvent.getEventType() == POSEvent.ITEMS_LOADITEMS) {
            POSButton b = (POSButton) posEvent.getSource();
            gui.setStatus("Searching for items...");
            loadItemsToGUI((String) b.getObjectRef());
            gui.setStatus("items loaded.");
        } else if (posEvent.getEventType() == POSEvent.ITEMS_ADDITEM) {
            POSButton b = (POSButton) posEvent.getSource();
            processItemAdd((List<OrderItem>) b.getObjectRef());

        } else if (posEvent.getEventType() == POSEvent.DRAWER_OPENED) {
            drawerOpened();
        } else if (posEvent.getEventType() == POSEvent.DRAWER_CLOSED) {
            drawerClosed();
        } else if (posEvent.getEventType() == POSEvent.NO_SALE) {
            if (intMode == MODE_ITEMS) {
                if (order != null && order.getItemsCount() == 0) {
                    processNoSale();
                } else {
                    gui.setStatus("Can not open drawer while items exist in the order.", true);
                }
            } else {
                gui.setStatus("No sale can only be done in Item Mode.", true);
            }
        } else if (posEvent.getEventType() == POSEvent.ORDER_VOIDLAST) {
            if (intMode == MODE_ITEMS)
                voidLastItem();
            else if (intMode == MODE_CHECKOUT) {
                returnToItemsScreen();
                gui.toggleButtonText();
                voidLastItem();
            } else
                gui.setStatus("Voiding can't be done in this mode.", true);
        } else if (posEvent.getEventType() == POSEvent.ORDER_VOIDORDER) {
            if (intMode == MODE_ITEMS)
                voidOrder();
            else if (intMode == MODE_CHECKOUT) {
                returnToItemsScreen();
                gui.toggleButtonText();
                voidOrder();
            } else
                gui.setStatus("Voiding can't be done in this mode.", true);
        } else if (posEvent.getEventType() == POSEvent.STUDENT_RESET) {
            if (intMode == MODE_ITEMS) {
                resetAnonStudent();
            } else
                gui.setStatus("Changing students can only be done in Item Mode", true);
        } else if (posEvent.getEventType() == POSEvent.ITEMS_TOGGLECHECKOUT) {
            if (intMode == MODE_ITEMS)
                loadCheckOutScreen();
            else if (intMode == MODE_CHECKOUT)
                returnToItemsScreen();

            gui.toggleButtonText();
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_ADDCASH) {
            try {
                if (posEvent.getSource() != null) {
                    POSKeyPad key = (POSKeyPad) posEvent.getSource();
                    updateCashBuffer(key.getBigDecimalValue());
                    key.reset();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_CLEARCASH) {
            clearCash();
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_CLEARCREDIT) {
            clearCredit();
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_ADDCREDIT) {
            try {
                if (posEvent.getSource() != null) {
                    POSKeyPad key = (POSKeyPad) posEvent.getSource();
                    updateCreditBuffer(key.getBigDecimalValue());
                    key.reset();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_PROCESS) {
            processOrder();
        } else if (posEvent.getEventType() == POSEvent.CHECKOUT_SUMMARYSCREENCLOSED) {
            drawerClosed();
        } else if (posEvent.getEventType() == POSEvent.IMAGE_LOADED) {
            if (posEvent.getSource() != null)
                pushStudentImage((ImageIcon) posEvent.getSource());
        } else if (posEvent.getEventType() == POSEvent.IMAGE_LOAD_FAILED) {
            logger.fine("Image load failed");
        } else if (posEvent.getEventType() == POSEvent.ITEMS_MANUALENTRY) {
            if (intMode == MODE_ITEMS)
                loadKeyPad();
            else
                gui.setStatus("Changing students can only be done in Item Mode", true);
        } else if (posEvent.getEventType() == POSEvent.ITEMS_ACTIVATELASTORDER) {
            if (intMode == MODE_ITEMS)
                activateLastStudentOrder();
            else
                gui.setStatus("Activating a previous order can only be done in Item Mode", true);
        } else if (posEvent.getEventType() == POSEvent.ITEMS_MANUALENTRYSUBMIT) {
            try {
                if (posEvent.getSource() != null) {
                    PSEntryPad key = (PSEntryPad) posEvent.getSource();
                    processStudentScan(key.getKeyPad().getValueString());
                    key.kill();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (posEvent.getEventType() == POSEvent.ORDER_VOIDITEM) {
            try {
                if (intMode == MODE_ITEMS) {
                    if (posEvent.getSource() != null) {
                        OrderItemPanel itemPnl = (OrderItemPanel) posEvent.getSource();
                        if (itemPnl != null) {
                            if (itemPnl.isTapped()) {
                                OrderItem item = (OrderItem) (itemPnl.getObjectRef());
                                voidItem(item);
                            } else {
                                itemPnl.tap();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
