package org.pscafepos.service.offline.ping;

import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;

import java.sql.Connection;
import java.util.TimerTask;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public class PingTimerTask extends TimerTask {

    private JdbcConnector connector;

    private boolean isConnected = false;
    private PingEventHandler eventHandler;

    public PingTimerTask(JdbcConnector connector, PingEventHandler eventHandler) {
        this.connector = connector;
        this.eventHandler = eventHandler;
    }

    public void run() {

        Connection connection = null;
        try {
            connection = connector.getNewConnection();
            if (!isConnected) {
                eventHandler.onConnectionEstablished();
                isConnected = true;
            }
        } catch (JdbcConnectorException e) {
            if (isConnected) {
                eventHandler.onConnectionLost();
                isConnected = false;
            }
        } finally {
            JdbcUtils.closeIfNeededSilently(connection);
        }
    }
}
