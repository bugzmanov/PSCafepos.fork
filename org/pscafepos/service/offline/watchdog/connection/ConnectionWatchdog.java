package org.pscafepos.service.offline.watchdog.connection;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.service.offline.repilcator.BackendReplicationTimerTask;
import org.pscafepos.service.offline.ping.PingEventHandler;
import org.pscafepos.service.offline.ping.PingTimerTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Bagmanov
 *         Date: 08.09.2009
 */
public class ConnectionWatchdog implements IWatchDog {
    private BackendReplicationTimerTask replicatorTask;
    private String backendName;
    private JdbcConnector localConnector;
    private JdbcConnector remoteConnector;
    private WatchDogEventHandler eventHandler;

    private Connection currentConnection;
    private static final int PING_PERIOD = 15000;
    private static final int REPLICATION_EXECUTION_PERIOD = PING_PERIOD;
    private String localConnectionUrl;
    //    private Replicator replicator;
    private Lock posLock;
    private boolean isOfflineMode = true;

    private static final Logger logger = Logger.getLogger(ConnectionWatchdog.class.getName());
    ConcurrentMap<Connection, AtomicInteger> connections = new ConcurrentHashMap<Connection, AtomicInteger>();
    ExecutorService executors = Executors.newSingleThreadExecutor();
    ExecutorService connectionReleaseService = Executors.newFixedThreadPool(3);

    public ConnectionWatchdog(String backendName, JdbcConnector localConnector, JdbcConnector remoteConnector, WatchDogEventHandler eventHandler/*, Replicator replicator*/) {
        this.backendName = backendName;
        this.localConnector = localConnector;
        this.remoteConnector = remoteConnector;
        this.eventHandler = eventHandler;
        Timer pingTimer = new Timer(true);

        PingTimerTask pingTask = new PingTimerTask(remoteConnector, new PingEventHandler() {
            public void onConnectionEstablished() {
                switchToOnlineMode();
            }

            public void onConnectionLost() {
                switchToOfflineMode();
            }
        });
//        this.replicator = replicator;
        posLock = new ReentrantLock();
        try {
            this.currentConnection = localConnector.getNewConnection();
            localConnectionUrl = currentConnection.getMetaData().getURL();
        } catch (JdbcConnectorException e) {
            //todo
        } catch (SQLException e) {
            //todo
        }
        pingTimer.schedule(pingTask, new Date(), PING_PERIOD);

    }

    public boolean isOfflineMode(Connection connection) {
        try {
            if (posLock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    //todo: this is dirty hack
                    return connection.getMetaData().getURL().equalsIgnoreCase(localConnectionUrl);
                } catch (SQLException e) {
                    // todo what to do?
                    return true;
                } finally {
                    posLock.unlock();
                }
            }
        } catch (InterruptedException e) { //this should never happened
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        throw new IllegalStateException("Couldn't acquire lock on current connection");

    }

    public Connection getCurrentConnection() {
        try {
            if (posLock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    AtomicInteger count = connections.putIfAbsent(currentConnection, new AtomicInteger(1));
                    if (count != null) {
                        count.incrementAndGet();
                    }
                    return currentConnection;
                } finally {
                    posLock.unlock();
                }
            }
        } catch (InterruptedException e) { //this should never happened
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        throw new IllegalStateException("Couldn't acquire lock on current connection");
    }

    //todo lock this
    public Connection getNewConnection() throws JdbcConnectorException {
        if (isOfflineMode) {
            return localConnector.getNewConnection();
        } else {
            return remoteConnector.getNewConnection();
        }
    }

    private void switchToOnlineMode() {
        logger.log(Level.INFO, "Connection to remote " + backendName + " is established. Switching to online mode");
        Connection remoteConnection;
        try {
            remoteConnection = remoteConnector.getNewConnection();
            while (isOfflineMode) {
                try {
                    if (posLock.tryLock(1, TimeUnit.SECONDS)) {
                        closeCurrentConnection();
                        try {
                            isOfflineMode = false;
                            currentConnection = remoteConnection;
                        } finally {
                            posLock.unlock();
                        }
                        executors.execute(new Runnable() {
                            public void run() {
                                eventHandler.onOnlineMode();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
            }
            logger.log(Level.INFO, backendName + ": Successfully switched to online mode");
        } catch (JdbcConnectorException e) {
            logger.log(Level.SEVERE, backendName + ": Couldn't connect to remote " + backendName + " Switching to online mode failed");
        }
    }

    private void switchToOfflineMode() {
        logger.log(Level.INFO, "Connection to remote " + backendName + " is lost. Switching to service.offline mode..");
        try {
            Connection localConnection = localConnector.getNewConnection();
            while (!isOfflineMode) {
                try {
                    if (posLock.tryLock(1, TimeUnit.SECONDS)) {
                        closeCurrentConnection();
                        try {
                            isOfflineMode = true;
                            currentConnection = localConnection;
                        } finally {
                            posLock.unlock();
                        }
                        executors.execute(new Runnable() {
                            public void run() {
                                eventHandler.onOfflineMode();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
            }
            logger.log(Level.INFO, backendName + ": Successfully switched to service.offline mode");
        } catch (JdbcConnectorException e) {
            logger.log(Level.SEVERE, backendName + ": Couldn't connect to local " + backendName + " Switching to service.offline mode failed");
        }

    }

    private void closeCurrentConnection() {
        if (currentConnection != null) {
            final Connection connection = currentConnection;
            connectionReleaseService.execute(new Runnable() {
                public void run() {
                    AtomicInteger count = connections.get(connection);
                    while (count != null && count.intValue() != 0) {
                        synchronized (this) {
                            try {
                                this.wait(10000);
                            } catch (InterruptedException e) {/*this will never happen*/ }
                        }
                    }
                    connections.remove(connection);
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.log(Level.FINE, e.getMessage(), e);
                    }
                }
            });

        }
    }

    public void releaseConnection(Connection connection) {
        AtomicInteger count = connections.get(connection);
        count.getAndDecrement();
    }
}

