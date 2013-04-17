package org.pscafepos.service.offline.repilcator;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bagmanov
 */
public abstract class ReplicatorBase implements Replicator{
    protected static final Logger logger = Logger.getLogger(ReplicatorBase.class.getName());

    protected void close(Statement statement, ResultSet resultSet) {
        try {
            if(resultSet != null){
                resultSet.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't close resultSet", e);
        }
        try {
            if(statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't close statement", e);
        }
    }

    protected void close(Statement statement) {
        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Couldn't close statement", e);
            }
        }
    }

    protected int getTableRecordsCount(Connection connection, String table) throws ReplicatorException {
        String sql = "select count(*) from " + table;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                return resultSet.getInt(1);
            } else {
                throw new ReplicatorException("Sql statement didn't return any value [" + sql + "]");
            }
        } catch(SQLException e) {
            throw new ReplicatorException("Couldn't count table rows [table = "+ table, e);
        } finally {
            close(statement, resultSet);
        }
    }

    protected void clean(Connection connection, String table) throws ReplicatorException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("delete from " + table);
        } catch (SQLException e) {
            throw new ReplicatorException("Couldn't purge table " + table, e);
        } finally {
            close(statement);
        }
    }
}
