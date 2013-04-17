package org.pscafepos.backends.pos;

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.OrderItemDAO;
import org.pscafepos.backends.domain.items.ItemsPackageDAO;
import org.pscafepos.backends.domain.items.ItemsPackage;
import org.pscafepos.backends.domain.category.CategoryDAO;
import org.pscafepos.backends.domain.meal.MealManager;
import org.pscafepos.backends.domain.credit.CreditManager;
import org.pscafepos.backends.domain.credit.CreditManagerException;
import org.pscafepos.backends.domain.transaction.OrderTransactionManager;
import org.pscafepos.backends.domain.transaction.TransactionManagerException;
import org.pscafepos.backends.domain.inventory.InventoryDAO;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.domain.pos.GlobalPOSProperties;
import org.pscafepos.backends.domain.pos.PosRegistrationAudit;
import org.pscafepos.backends.domain.billing.BillingFeeDAO;
import org.pscafepos.backends.domain.hotbar.HotbarDAO;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;

import org.pscafepos.model.OrderTransaction;
import org.pscafepos.model.Student;
import org.pscafepos.backends.pos.PosOrder;
import org.pscafepos.model.Order;
import org.pscafepos.service.auth.AuthenticateService;
import org.pscafepos.service.auth.AuthFailedException;
import org.pscafepos.service.offline.DaoFactory;
import org.pscafepos.configuration.SessionSettings;
import org.pscafepos.backends.database.transaction.Transaction;
import org.pscafepos.backends.database.transaction.TransactionException;

/**
 * @author bagmanov
 */
public class PointOfSaleSystemImpl implements PointOfSaleSystem {
    private static final Logger logger = Logger.getLogger(PointOfSaleSystemImpl.class.getName());

    private OrderItemDAO orderItemDAO;
    private ItemsPackageDAO itemsPackageDAO;
    private CategoryDAO categoryDAO;
    private MealManager mealManager;
    private CreditManager creditManager;
    private OrderTransactionManager orderTransactionManager;
    private InventoryDAO inventoryDAO;
    private AuthenticateService authenticateService;
    private DaoFactory daoFactory;
    private GlobalPOSProperties posExSet;
    private PosRegistrationAudit registrationAuditDAO;
    private HotbarDAO hotbarDAO;
    private BillingFeeDAO feeDAO;

    private String cashier;
    private static final Map<String, String> defaultSettings = new HashMap<String, String>() {{
        put("displayLastOrder", "1");
    }};
    private int registrationId = -1;

    public PointOfSaleSystemImpl(AuthenticateService authenticateService, DaoFactory daoFactory) {
        this.authenticateService = authenticateService;
        this.daoFactory = daoFactory;
        cashier = null;
    }

    public boolean connect(String cashier, String password) {
        try {
            SessionSettings sessionSettings = authenticateService.auth(cashier, password);
            this.cashier = cashier;
            initDAOs(sessionSettings);
            if (registrationAuditDAO.logRegisterEvent(cashier)) {
                try {
                    registrationId = registrationAuditDAO.getPOSRegistrationID();
                    posExSet = daoFactory.createPosSettingsDAO(registrationId);
                    logger.log(Level.INFO, "POS registration/update completed!");
                } catch (DAOException e) {
                    logger.log(Level.WARNING, "Couldn't get registration ID, you won't be able to load global POS messages and properties");
                }
            } else {
                logger.log(Level.WARNING, "POS Registration failed! Unable log register event in POS.  " +
                        "You will still be allowed to use this POS, but some settings and server communication may not be functional!");
            }
            return true;
        } catch (AuthFailedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    private void initDAOs(SessionSettings sessionSettings) {
        daoFactory.init(sessionSettings);
        itemsPackageDAO = daoFactory.createBatchItemDAO();
        categoryDAO = daoFactory.createCategoryDAO();
        creditManager = daoFactory.createCreditManager();
        inventoryDAO = daoFactory.createInventoryDAO();
        mealManager = daoFactory.createMealManager();
        orderItemDAO = daoFactory.createOrderItemDAO();
        orderTransactionManager = daoFactory.createOrderTransactionManager();
        registrationAuditDAO = daoFactory.createRegistrationAudit();
        feeDAO = daoFactory.createBillingFeeDAO();
        hotbarDAO = daoFactory.createHotbarDAO();
    }

    public List<String> listItemCategories() throws PointOfSaleSystemException {
        try {
            List<String> categories = categoryDAO.listVisibleCategories();
            List<String> batchCategories = categoryDAO.listVisibleBatchCategories();
            for (String batchCategory : batchCategories) {
                if (!categories.contains(batchCategory)) {
                    categories.add(batchCategory);
                }
            }
            return categories;
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }
    }

    public List<OrderItem> listItems(String category) throws PointOfSaleSystemException {
        try {
            List<OrderItem> items = orderItemDAO.findByCategory(category);
            Iterator<OrderItem> iterator = items.iterator();
            while (iterator.hasNext()) {
                OrderItem orderItem = iterator.next();
                if (!orderItem.completeItem()) {
                    iterator.remove();
                }
            }
            return items;
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }
    }

    public void processOrder(OrderTransaction orderTransaction) throws PointOfSaleSystemException {
        orderTransaction.setCashier(cashier);
        Transaction transaction = daoFactory.createTransaction(orderTransactionManager, inventoryDAO, creditManager, feeDAO);
        try {
            transaction.start();
            feeDAO.saveBillingFees(orderTransaction.getStudent(), orderTransaction.getOrder());
            orderTransaction = orderTransactionManager.save(orderTransaction);
            for (OrderItem orderItem : orderTransaction.getOrder().getOrderItems()) {
                inventoryDAO.deleteItemFromInventory(orderItem.getDBID());
            }
            hotbarDAO.updateHotbar(orderTransaction.getOrder().getOrderItems(),
                    cashier);
            if (orderTransaction.getCredit().compareTo(BigDecimal.ZERO) > 0) {
                // must update student credit information
                creditManager.saveCreditTransaction(orderTransaction);
            }
            transaction.commit();
        } catch (TransactionManagerException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            rollBack(transaction);
            throw new PointOfSaleSystemException("Error occured during Writting of Transaction Master Record.", ex);
        } catch (CreditManagerException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            rollBack(transaction);
            throw new PointOfSaleSystemException("Error cccured during saving credit record", e);
        } catch (DAOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            rollBack(transaction);
            throw new PointOfSaleSystemException("Error occured during updating database", e);
        } catch (TransactionException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            rollBack(transaction);
            throw new PointOfSaleSystemException("Critical Error: couldn't commit changes!", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            rollBack(transaction);
            throw new PointOfSaleSystemException(e);
        }

        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void rollBack(Transaction conn) {
        try {
            conn.rollback();
        } catch (TransactionException e) {
            logger.log(Level.SEVERE, "Couldn't rollback order processing", e);
        }
    }

    public List<OrderItem> listHotbarItems() throws PointOfSaleSystemException {
        try {
            return hotbarDAO.listHotBarItems(cashier);
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }

    }

    public BigDecimal getStudentCredit(Student student) throws PointOfSaleSystemException {
        if(student.getType().equalsIgnoreCase("staff")){
            return BigDecimal.ZERO;
        }
        try {
            return creditManager.getCredit(student);
        } catch (CreditManagerException e) {
            throw new PointOfSaleSystemException(e);
        }
    }

    public List<OrderItem> findItemsByBarcode(String barCode) throws PointOfSaleSystemException {
        try {
            return orderItemDAO.findByBarCode(barCode);
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);

        }
    }


    public void close() {
        try {
            hotbarDAO.clearHotBar(cashier);
        } catch (DAOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<ItemsPackage> listItemsPackages(String category) throws PointOfSaleSystemException {
        try {
            return itemsPackageDAO.findByCategory(category);
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }
    }

    public String getGlobalPosMessage() {
        try {
            if (posExSet != null) {
                return posExSet.getMessage();
            } else {
                return null;
            }
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Couldn't read global pos messages", e);
            return null;
        }
    }

    public boolean isSpecialSaleAllowed(Student student, int mealType) throws PointOfSaleSystemException {
        try {
            return mealManager.canGetSpecialMeal(student, mealType);
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }
    }


    public Order createNewOrder(String orderTitle)  {
        Order order = new PosOrder(orderTitle, mealManager);
        try {
            if(registrationId == -1){
                registrationId = registrationAuditDAO.getPOSRegistrationID();
            }
            order.addItems(orderItemDAO.listAutoAddItems(registrationId));
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Failed to load auto add items", e);
        }
        return order;
    }

    public Order getStudentsLastOrder(Student student) throws PointOfSaleSystemException {
        try {
            List<OrderItem> items = orderItemDAO.listLastOrderItems(student.getStudentNumber());
            Order order = new PosOrder("Last order", mealManager);
            order.setBuyer(student);
            order.addItems(items);
            return order;
        } catch (DAOException e) {
            throw new PointOfSaleSystemException(e);
        }

    }

    public String getProperty(String key) {
        try {
            String value = null;
            if (posExSet != null) {
                value = posExSet.getGeneralSettings(key);
            }
            if (value == null) {
                value = defaultSettings.get(key);
            }
            return value;
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Couldn't get property: " + key, e);
            return defaultSettings.get(key);
        }

    }

    public Boolean getBooleanProperty(String key) {
        String value = getProperty(key);
        return value != null && value.equals("1");
    }

    public boolean hadIdenticalOrderInCurrentSession(OrderTransaction orderTransaction) {
        return orderTransactionManager.hadIdenticalTransactionInCurrentSession(orderTransaction);
    }
}
