package org.pscafepos.service.offline.watchdog.connection;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.service.offline.ping.PingTimerTask;
import org.pscafepos.service.offline.ping.PingEventHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.Date;

/**
 * @author bagmanov
 */
public class SimpleConnectionWatchdog implements IWatchDog {
    private static final int PING_PERIOD = 15000;
    private static final Logger logger = Logger.getLogger(ConnectionWatchdog.class.getName());
    private String backendName;
    private JdbcConnector localConnector;
    private JdbcConnector remoteConnector;
    private WatchDogEventHandler eventHandler;
    private boolean isOfflineMode;
    private boolean tryReconnect;
    private String localConnectionUrl;
    private Timer pingTimer;

    public SimpleConnectionWatchdog(String backendName, JdbcConnector localConnector, JdbcConnector remoteConnector, WatchDogEventHandler eventHandler/*, Replicator replicator*/) {
        this.backendName = backendName;
        this.localConnector = localConnector;
        this.remoteConnector = remoteConnector;
        this.eventHandler = eventHandler;
        tryReconnect = true;

        Connection connection = null;
        try {
            connection = localConnector.getNewConnection();
            localConnectionUrl = connection.getMetaData().getURL();
        } catch (JdbcConnectorException e) {
            //todo
        } catch (SQLException e) {
            //todo
        } finally {
            JdbcUtils.closeIfNeededSilently(connection);
        }
        isOfflineMode = true;
        tryReconnect = true;

    }

    public boolean isOfflineMode(Connection connection) {
        try {
            return connection.getMetaData().getURL().equalsIgnoreCase(localConnectionUrl);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Couldn't determine if connection is local db connection or not", e);
            return isOfflineMode;
        }
    }

    public Connection getCurrentConnection() {
        return getConnection();
    }

    private Connection getConnection() {
        if (!tryReconnect) {
            return getLocalConnection();
        }
        try {
            Connection connection = remoteConnector.getNewConnection();
            if (isOfflineMode) {
                logger.log(Level.INFO, backendName + " switching to online mode");
                isOfflineMode = false;
                eventHandler.onOnlineMode();
            }
            return connection;

        } catch (JdbcConnectorException e) {
            logger.log(Level.FINE, "Coulnd't get connection to remote " + backendName, e);
            tryReconnect = false;
            if (!isOfflineMode) {
                logger.log(Level.INFO, backendName + " switching to offline mode");
                isOfflineMode = true;
                eventHandler.onOfflineMode();
            }
            reschedulePing();
            return getLocalConnection();
        }

    }

    private void reschedulePing() {
        if (pingTimer != null) {
            pingTimer.cancel();
        }

        pingTimer = new Timer(true);
        PingTimerTask pingTask = new PingTimerTask(remoteConnector, new PingEventHandler() {
            public void onConnectionEstablished() {
                pingTimer.cancel();
                tryReconnect = true;
            }

            public void onConnectionLost() {/* do nothing */ }
        });

        pingTimer.schedule(pingTask, new Date(), PING_PERIOD);
    }

    private Connection getLocalConnection() {
        try {
            return localConnector.getNewConnection();
        } catch (JdbcConnectorException e) {
            logger.log(Level.SEVERE, "Couldn't get connection tolocal system", e);
            return null;
        }
    }

    public Connection getNewConnection() throws JdbcConnectorException {
        return getConnection();
    }

    public void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Watchdog couldn't release connection", e);
        }
    }
}
