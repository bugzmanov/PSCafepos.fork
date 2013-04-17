package org.pscafepos.service.offline.watchdog.connection;

import org.pscafepos.backends.database.dao.events.DaoEventHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bagmanov
 *         Date: 03.09.2009
 */
public class LocalPosUpdatesEventHandler implements DaoEventHandler {
    private static final Logger logger = Logger.getLogger(LocalPosUpdatesEventHandler.class.getName());

    private IWatchDog dog;
    private String tableName;

    public LocalPosUpdatesEventHandler(IWatchDog dog, String tableName) {
        this.dog = dog;
        this.tableName = tableName;
    }

    public void onUpdateSql(Connection connection, String sql) {
        if (!dog.isOfflineMode(connection)) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into " + tableName + " (SQL_EXPR) VALUES (?)");
            statement.setString(1, sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't save update pos query in local holder", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Couldn't close prepared statement", e);
                }
            }
        }
    }


    public void onQuerySql(String sql) {
        //do nothing
    }
}
