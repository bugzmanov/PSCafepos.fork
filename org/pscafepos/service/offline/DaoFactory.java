package org.pscafepos.service.offline;

import org.pscafepos.service.offline.watchdog.connection.*;
import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.events.DaoEventHandler;
import org.pscafepos.backends.database.transaction.Transaction;
import org.pscafepos.backends.database.transaction.impl.PosTransaction;
import org.pscafepos.backends.domain.category.CategoryDAO;
import org.pscafepos.backends.domain.category.CategoryDAOImpl;
import org.pscafepos.backends.domain.credit.CreditManager;
import org.pscafepos.backends.domain.credit.CreditManagerImpl;
import org.pscafepos.backends.domain.inventory.InventoryDAO;
import org.pscafepos.backends.domain.inventory.InventoryDAOImpl;
import org.pscafepos.backends.domain.items.ItemsPackageDAO;
import org.pscafepos.backends.domain.items.OrderItemDAO;
import org.pscafepos.backends.domain.items.impl.ItemsPackageDAOImpl;
import org.pscafepos.backends.domain.items.impl.OrderItemDAOImpl;
import org.pscafepos.backends.domain.meal.MealManager;
import org.pscafepos.backends.domain.meal.MealManagerImpl;
import org.pscafepos.backends.domain.pos.GlobalPOSProperties;
import org.pscafepos.backends.domain.pos.GlobalPOSPropertiesImpl;
import org.pscafepos.backends.domain.pos.PosRegistrationAudit;
import org.pscafepos.backends.domain.pos.PosRegistrationAuditImpl;
import org.pscafepos.backends.domain.transaction.OrderTransactionManager;
import org.pscafepos.backends.domain.transaction.OrderTransactionManagerImpl;
import org.pscafepos.backends.domain.sis.StudentInformationDAO;
import org.pscafepos.backends.domain.sis.StudentInformationDAOImpl;
import org.pscafepos.backends.domain.sis.SchoolOfficerDAO;
import org.pscafepos.backends.domain.sis.SchoolOfficerDAOImpl;
import org.pscafepos.backends.domain.billing.BillingFeeDAO;
import org.pscafepos.backends.domain.billing.BillingFeeDAOImpl;
import org.pscafepos.backends.domain.hotbar.HotbarDAO;
import org.pscafepos.backends.domain.hotbar.LocalHotbarDAOImpl;
import org.pscafepos.service.offline.repilcator.*;
import org.pscafepos.service.offline.proxy.AbstractProxyHandler;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.Timer;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.backends.pos.PointOfSaleSystemException;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.configuration.SessionSettings;
import org.pscafepos.configuration.Constants;

/**
 * @author bagmanov
 */
public class DaoFactory {

    private OfflineSupportManager offlineManager;
    private IWatchDog posWatchdog;
    private IWatchDog sisWatchdog;
    private SessionSettings sessionSettings;
    DaoEventHandler offlineModeEventHandler;
    Timer replicatorTimer;

    private static final int REPLICATION_PERIOD = 15000;

    private static final DaoEventHandler EMPTY_HANDLER = new DaoEventHandler() {
        public void onUpdateSql(Connection connection, String sql) {
            //do nothing
        }

        public void onQuerySql(String sql) {
            //do nothing
        }
    };

    public DaoFactory() {
    }

    public void init(SessionSettings sessionSettings) {
        this.sessionSettings = sessionSettings;
        offlineManager = new OfflineSupportManager(sessionSettings.getSisSettings(), sessionSettings.getPosSettings());
        try {
            String jdbcDriver = offlineManager.getLocalPosSettings().getDriverName();
            Class.forName(jdbcDriver).newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        initSisWatchDog();
        initPosWatchDog();
        new Thread(new Runnable() {
            public void run() {
                new OrderTransactionRemover(offlineManager.getLocalPOSConnector(),
                        offlineManager.getRemotePOSConnector(), offlineManager.getRemotePosSettings()).updateLocalTransactions();
            }
        }).start();
    }

    private void initPosWatchDog() {
        final Replicator posReplicator = new PosReplicator(offlineManager.getLocalPosSettings(), offlineManager.getRemotePosSettings());
        final PosLocalUpdateReplicator updateReplicator = new PosLocalUpdateReplicator(offlineManager.getLocalPOSConnector(),
                offlineManager.getRemotePOSConnector(), offlineManager.getRemotePosSettings());
        posWatchdog = new SimpleConnectionWatchdog("Point of Sale System", offlineManager.getLocalPOSConnector(),
                offlineManager.getRemotePOSConnector(), new DefaultWatchdogEventHandler(posReplicator, "Point of Sale System") {
            @Override
            public void onOnlineMode() {
                updateReplicator.replicate();
                super.onOnlineMode();
            }
        });
        this.offlineModeEventHandler = new LocalPosUpdatesEventHandler(posWatchdog,
                offlineManager.getLocalPosSettings().getTablesPrefix() + Constants.LOCALDB_UPDATEHOLDER_TABLE);
    }

    private void initSisWatchDog() {
        final Replicator sisReplicator = new SisReplicator(offlineManager.getLocalSisSettings(), offlineManager.getRemoteSisSettings());
        final BillingFeesReplicator billingReplicator = new BillingFeesReplicator(offlineManager.getLocalSISConnector(),
                offlineManager.getRemoteSISConnector());
        sisWatchdog = new SimpleConnectionWatchdog("Student Information System", offlineManager.getLocalSISConnector(),
                offlineManager.getRemoteSISConnector(), new DefaultWatchdogEventHandler(sisReplicator, "Student Information System") {
            @Override
            public void onOnlineMode() {
                billingReplicator.replicate();
                super.onOnlineMode();
            }
        });
    }

    public HotbarDAO createHotbarDAO() {
        return new LocalHotbarDAOImpl();
    }

    public CategoryDAO createCategoryDAO() {
        CategoryDAOImpl dao = new CategoryDAOImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (CategoryDAO) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{CategoryDAO.class}, handler);
    }

    public CreditManager createCreditManager() {
        CreditManagerImpl creditManager = new CreditManagerImpl();
//        creditManager.setEventHandler(eventHandler);
        String updateHolderTable = offlineManager.getLocalPosSettings().getTablesPrefix() + Constants.LOCALDB_UPDATEHOLDER_TABLE;
        InvocationHandler handler = new PosDAOInvocationHandler(creditManager, new LocalPosUpdatesEventHandler(posWatchdog, updateHolderTable) {
            @Override
            public void onUpdateSql(Connection connection, String sql) {
                if (sql.contains(offlineManager.getLocalPosSettings().getTablesPrefix() + "studentcredit") &&
                        !sql.contains("studentcredit_log")) {
                    super.onUpdateSql(connection, sql);
                }

            }
        });
        return (CreditManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{CreditManager.class}, handler);
    }

    public InventoryDAO createInventoryDAO() {
        //todo: handler should be more cool.
        InventoryDAOImpl dao = new InventoryDAOImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao, EMPTY_HANDLER);
        return (InventoryDAO)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{InventoryDAO.class}, handler);
//        return new InventoryDAOImpl(posConnector, dbSettings);
    }

    public ItemsPackageDAO createBatchItemDAO() {
        ItemsPackageDAOImpl dao = new ItemsPackageDAOImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (ItemsPackageDAO)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{ItemsPackageDAO.class}, handler);

    }

    public OrderItemDAO createOrderItemDAO() {
        OrderItemDAOImpl dao = new OrderItemDAOImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (OrderItemDAO)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{OrderItemDAO.class}, handler);
    }

    public MealManager createMealManager() {
        MealManagerImpl dao = new MealManagerImpl();
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (MealManager)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{MealManager.class}, handler);

    }

    public PosRegistrationAudit createRegistrationAudit() {
        PosRegistrationAuditImpl dao = new PosRegistrationAuditImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (PosRegistrationAudit)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{PosRegistrationAudit.class}, handler);
    }

    public GlobalPOSProperties createPosSettingsDAO(int registrationId) {
        GlobalPOSPropertiesImpl dao = new GlobalPOSPropertiesImpl(registrationId);
        InvocationHandler handler = new PosDAOInvocationHandler(dao);
        return (GlobalPOSProperties)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{GlobalPOSProperties.class}, handler);

    }

    public OrderTransactionManager createOrderTransactionManager() {
        OrderTransactionManagerImpl dao = new OrderTransactionManagerImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new PosDAOInvocationHandler(dao, EMPTY_HANDLER);
        return (OrderTransactionManager)
                Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{OrderTransactionManager.class}, handler);
    }

    public StudentInformationDAO createStudentInformationDAO() {
        StudentInformationDAOImpl dao = new StudentInformationDAOImpl();
        InvocationHandler handler = new AbstractProxyHandler<StudentInformationDAOImpl>(sisWatchdog, dao) {
            public void updateDao(boolean isOfflineMode) {
                if (isOfflineMode) {
                    dao.setSettings(offlineManager.getLocalSisSettings());
                } else {
                    dao.setSettings(offlineManager.getRemoteSisSettings());
                }
            }
        };
        return (StudentInformationDAO) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{StudentInformationDAO.class}, handler);
    }

    public SchoolOfficerDAO createScholOfficerDAO() {
        SchoolOfficerDAOImpl dao = new SchoolOfficerDAOImpl();
        InvocationHandler handler = new AbstractProxyHandler<SchoolOfficerDAOImpl>(sisWatchdog, dao) {
            public void updateDao(boolean isOfflineMode) {
                if (isOfflineMode) {
                    dao.setSettings(offlineManager.getLocalSisSettings());
                } else {
                    dao.setSettings(offlineManager.getRemoteSisSettings());
                }
            }
        };
        return (SchoolOfficerDAO) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{SchoolOfficerDAO.class}, handler);
    }

    public BillingFeeDAO createBillingFeeDAO() {
        BillingFeeDAOImpl dao = new BillingFeeDAOImpl(sessionSettings.getBuildingNumber());
        InvocationHandler handler = new AbstractProxyHandler<BillingFeeDAOImpl>(posWatchdog, dao) {
            public void updateDao(boolean isOfflineMode) {
                if (isOfflineMode) {
                    dao.setAutoIncrementMode();
                } else {
                    dao.setSequencedMode();
                }
            }
        };
        return (BillingFeeDAO) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{BillingFeeDAO.class}, handler);


    }

    //todo: need special exception type here and this  method should be moved somewhere else
    public Transaction createTransaction(TransactionalDAO... daos) throws PointOfSaleSystemException {
        try {
            Connection newConnection = posWatchdog.getNewConnection();
            boolean isOfflineMode = posWatchdog.isOfflineMode(newConnection);
            IPosSettings settings = isOfflineMode ? offlineManager.getLocalPosSettings() : offlineManager.getRemotePosSettings();
            PosTransaction transaction = new PosTransaction(settings, newConnection);
            transaction.addToTransaction(daos);
            return transaction;
        } catch (JdbcConnectorException e) {
            throw new PointOfSaleSystemException("Couldn't start POS system transaction", e);
        }
    }


    private class DefaultWatchdogEventHandler implements WatchDogEventHandler {
        private TimerTask timerTask;
        private Replicator replicator;
        private String backendTitle;

        private DefaultWatchdogEventHandler(Replicator replicator, String backendTitle) {
            this.replicator = replicator;
            this.backendTitle = backendTitle;
            replicatorTimer = new Timer(true);
        }

        public void onOfflineMode() {
            if (timerTask != null) {
                timerTask.cancel();
            }

        }

        public void onOnlineMode() {
            timerTask = new BackendReplicationTimerTask(replicator, backendTitle);
            replicatorTimer.schedule(timerTask, new Date(), REPLICATION_PERIOD);
        }
    }

    private class PosDAOInvocationHandler implements InvocationHandler {

        private BasePOSDao dao;

        public PosDAOInvocationHandler(BasePOSDao dao) {
            this.dao = dao;
            dao.setEventHandler(DaoFactory.this.offlineModeEventHandler);
//            updateDAO();
            //todo: init
        }

        public PosDAOInvocationHandler(BasePOSDao dao, DaoEventHandler eventHandler) {
            this.dao = dao;
            dao.setEventHandler(eventHandler);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Connection connection = null;
            if (!methodName.equals("isInTransaction") && !methodName.equals("joinTransaction") && !dao.isInTransaction()) {
                connection = updateDAO();
            }
            try {
                Object object = method.invoke(dao, args);
                return object;
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            } finally {
                if (connection != null) {
                    posWatchdog.releaseConnection(connection);
                }
            }
        }

        private Connection updateDAO() {
            Connection connection = posWatchdog.getCurrentConnection();

            dao.setConnection(connection);
            if (posWatchdog.isOfflineMode(connection)) {
                dao.setSettings(offlineManager.getLocalPosSettings());
            } else {
                dao.setSettings(offlineManager.getRemotePosSettings());
            }
            return connection;
        }
    }
}
