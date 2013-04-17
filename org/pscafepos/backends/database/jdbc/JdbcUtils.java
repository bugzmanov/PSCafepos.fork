package org.pscafepos.backends.database.jdbc;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author bagmanov
 *         Date: 01.09.2009
 */
public class JdbcUtils {
    private static Logger logger = Logger.getLogger(JdbcUtils.class.getName());

    private JdbcUtils() {}

    public static long getLastInsertIDWorkAround(Statement state, String seqName) throws SQLException {
        if (state != null) {
            String sql = "SELECT currval('" + seqName + "')";
            if (state.execute(sql)) {
                ResultSet rs = state.getResultSet();
                if (rs.first()) {
                    return rs.getLong(1);
                }
            }
            state.close();
        }
        return -1;
    }

    public static void closeIfNeededSilently(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't close connection", e);
        }

    }

    public static Connection openSilently(String connectionString) {
        try {
            return DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Couldn't open connection", e);
        }
        return null;
    }

    public static void rollbackSilently(Connection connection){
        try {
            connection.rollback();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couldn't rollback connection", e);
        }
    }

}
