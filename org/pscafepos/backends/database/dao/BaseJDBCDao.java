package org.pscafepos.backends.database.dao;

import org.pscafepos.backends.database.dao.events.DaoEventHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author bagmanov
 */
public class BaseJDBCDao {
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = Logger.getLogger(BaseJDBCDao.class.getName());
    private static final Logger sqlLogger = Logger.getLogger(BaseJDBCDao.class.getName() + ".sql");
    private DaoEventHandler eventHandler;
    protected Connection connection;

    public BaseJDBCDao() {
        this.eventHandler = DO_NOTHING_EVENT_HANDLER;

    }

    protected BaseJDBCDao(Connection connection) {
        this();
        this.connection = connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setEventHandler(DaoEventHandler eventHandler) {
        if (eventHandler != null) {
            this.eventHandler = eventHandler;
        } else {
            this.eventHandler = DO_NOTHING_EVENT_HANDLER;
        }
    }

    protected <K> K executeSingleResultQuerySilently(String sql, ResultsetProcessor<K> processor) {
        try {
            return executeSingleResultQuery(sql, processor);
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "Couldn't execute query [" + sql + "]", e);
            return null;
        }
    }

    protected <K> K executeSingleResultQuery(String sql, ResultsetProcessor<K> processor) throws DAOException {
        List<K> result = executeQuery(sql, processor);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    protected <K> List<K> executeQuerySilently(String sql, ResultsetProcessor<K> processor) {
        try {
            return executeQuery(sql, processor);
        } catch (DAOException e) {
            logger.log(Level.WARNING, "Couldn't execute query [ " + sql + "]", e);
            return Collections.emptyList();
        }
    }

    protected <K> List<K> executeQuery(String sql, ResultsetProcessor<K> processor) throws DAOException {
        Statement stmt = null;
        ResultSet rs = null;
        List<K> result = new ArrayList<K>();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(processor.processEntity(rs));
            }
            eventHandler.onQuerySql(sql);
        } catch (SQLException ex) {
            throw new DAOException(ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    logger.log(Level.WARNING, "Couldn't close Result Set", sqlEx);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                    logger.log(Level.WARNING, "Couldn't close statement", sqlEx);
                }
            }

        }
        logger.log(Level.FINE, "Sql:[" + sql + "], Result:[" + Arrays.toString(result.toArray()) + "]");
        return result;

    }

    protected int executeUpdateSqlSilently(String sql) {
        try {
            return executeUpdateSql(sql);
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "Couln't execute sql update [" + sql + "]", e);
            return -1;
        }
    }

    protected String currentTimeStamp() {
        return DATE_FORMAT.format(new Date());
    }

    protected int executeUpdateSql(String sql) throws DAOException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            int updatedRowsCount = statement.executeUpdate(sql);
            logger.log(Level.FINE, "Sql:[" + sql + "], Result:[" + updatedRowsCount + " rows were updated]");
            eventHandler.onUpdateSql(connection, sql);
            return updatedRowsCount;
        } catch (SQLException ex) {
            throw new DAOException(ex);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "couldn't close sql statement", e);
                    this.getClass().getClassLoader().toString();
                }
            }
        }
    }

    private static final DaoEventHandler DO_NOTHING_EVENT_HANDLER = new DaoEventHandler() {
        public void onUpdateSql(Connection dao, String sql) {
            //do nothing
        }

        public void onQuerySql(String sql) {
            //do nothing;
        }
    };
}
