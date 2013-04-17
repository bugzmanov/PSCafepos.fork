package org.pscafepos.service.offline.repilcator;

import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.configuration.PosSettings;

import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * @author bagmanov
 *         TODO: this is really ugly class
 */
public class PosReplicator extends ReplicatorBase {

    private static final String[] posTablesForShallowComparison = {
            "batch_items",
            "batch_master",
            "cashierusers_ref",
            "hotbar",
            "inventory",
            "item_barcodes",
            "item_desc",
            "items",
            "pos_autoadditems",
            "pos_messages",
            "pos_registration",
            "pos_settings",
            "studentcredit",
//            "studentcredit_log",
            "trans_item"
//            "trans_master"
    };
    private static final Map<String, String> tablesForDeepComparison = new HashMap<String, String>() {{
        put("items", "item_id");
        put("studentcredit", "credit_studentid");
    }};

    private static final Map<String, List<String>> fieldsShouldNotBecompared = new HashMap<String, List<String>>() {{
        put("studentcredit", Arrays.asList(new String[]{"credit_id"}));
    }};
    //    private static final String[] tablesForDeepComparison = {
//        "items", "studentcredit"
//    };
    private PosSettings remotePOS;
    private PosSettings localPOS;
    private String localPrefix;
    private String remotePrefix;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public PosReplicator(PosSettings localPOS, PosSettings remotePOS) {
        this.localPOS = localPOS;
        this.remotePOS = remotePOS;
        this.localPrefix = localPOS.getTablesPrefix();
        this.remotePrefix = remotePOS.getTablesPrefix();
    }

    public void replicate(List<String> tables) throws ReplicatorException, ReplicatorConnectionException {
        Connection remoteConnection = null;
        Connection localConnection = null;
        try {
            remoteConnection = getConnection(remotePOS);
            localConnection = getConnection(localPOS);
            try {
                if (remoteConnection != null && localConnection != null) {
                    localConnection.setAutoCommit(false);
                    boolean isTransItemUnsync = tables.remove("trans_item");
                    for (String table : tables) {
                        replicate(remoteConnection, localConnection, table);
                    }
                    if (isTransItemUnsync) {
                        String transItemSelectSql = "select * from " + remotePrefix + "trans_item where ti_datetime > '" + monthAgo() + "'";
                        replicate(remoteConnection, localConnection, "trans_item", transItemSelectSql);
                        tables.add("trans_item");
                    }
                    localConnection.commit();
                }
            } catch (Exception ex) {
                JdbcUtils.rollbackSilently(localConnection);
                throw new ReplicatorException("Error during POS replicating", ex);
            }
        } catch (SQLException e) {
            throw new ReplicatorConnectionException(e.getMessage(), e);
        } finally {
            JdbcUtils.closeIfNeededSilently(localConnection);
            JdbcUtils.closeIfNeededSilently(remoteConnection);
        }
    }

    private String monthAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return DATE_FORMAT.format(calendar.getTime());
    }

    private void replicate(Connection source, Connection target, String table, String selectSQL) throws ReplicatorException {
        clean(target, localPrefix + table);
        Statement selectStatement = null;
        ResultSet selectRS = null;
        try {
            selectStatement = source.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            selectRS = selectStatement.executeQuery(selectSQL);
            ResultSetMetaData rsMetaData = selectRS.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            StringBuilder insertSQL = generateSQLStatement(table, columnCount);
            PreparedStatement insertStatement = null;
            try {
                insertStatement = target.prepareStatement(insertSQL.toString());
                while (selectRS.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        int type = rsMetaData.getColumnType(i);
                        if (type == Types.BLOB || type == Types.LONGVARBINARY) {
                            insertStatement.setBlob(i, selectRS.getBlob(i));
                        } else {
                            insertStatement.setObject(i, selectRS.getObject(i), rsMetaData.getColumnType(i));
                        }
                    }
                    insertStatement.executeUpdate();
                    insertStatement.clearParameters();
                }
            } finally {
                close(insertStatement);
            }
        } catch (SQLException e) {
            throw new ReplicatorException("Couldn't replicate table " + table, e);
        } finally {
            close(selectStatement, selectRS);
        }

    }

    private void replicate(Connection source, Connection target, String table) throws ReplicatorException {
        replicate(source, target, table, "SELECT * FROM " + remotePrefix + table);
    }

    private StringBuilder generateSQLStatement(String table, int columnCount) {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + localPrefix + table + " VALUES ( ");
        for (int i = 0; i < columnCount - 1; i++) {
            insertSQL.append("?, ");
        }
        insertSQL.append("?)");
        return insertSQL;
    }

    public List<String> getUnsynchronizedTablesList() throws ReplicatorException {
        List<String> unsynchronizedTables = new ArrayList<String>();
        Connection remoteConnection = null;
        Connection localConnection = null;
        try {
            remoteConnection = getConnection(remotePOS);
            localConnection = getConnection(localPOS);
            try {
                for (String posTable : posTablesForShallowComparison) {
                    if (!isSynchronized(remoteConnection, localConnection, posTable)) {
                        unsynchronizedTables.add(posTable);
                    }
                }
                for (Map.Entry<String, String> entry : tablesForDeepComparison.entrySet()) {
                    String posTable = entry.getKey();
                    String primaryKey = entry.getValue();
                    if (!isSynchronizedDeepChek(remoteConnection, localConnection, posTable, primaryKey)) {
                        unsynchronizedTables.add(posTable);
                    }
                }
            } catch (SQLException e) {
                throw new ReplicatorException(e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new ReplicatorConnectionException(e.getMessage(), e);
        } finally {
            JdbcUtils.closeIfNeededSilently(localConnection);
            JdbcUtils.closeIfNeededSilently(remoteConnection);
        }
        return unsynchronizedTables;
    }

    private boolean isSynchronized(Connection source, Connection target, String table) throws ReplicatorException {
        int sourceCount = getTableRecordsCount(source, remotePrefix + table);
        int targetCount = getTableRecordsCount(target, localPrefix + table);
        return sourceCount == targetCount;
    }

    //todo: get rid of this
    private boolean isSynchronizedDeepChek(Connection source, Connection target, String table, String primaryKeyColumnName) throws ReplicatorException, SQLException {
        if (isSynchronized(source, target, table)) {
            String localSelectSql = "select * from " + localPrefix + table + " order by " + primaryKeyColumnName;
            String remoteSelectSql = "select * from " + remotePrefix + table + " order by " + primaryKeyColumnName;
            Statement sourceStatement = null;
            Statement targetStatement = null;
            ResultSet sourceRS = null;
            ResultSet targetRS = null;
            List<String> notComparableFeilds = fieldsShouldNotBecompared.get(table);
            if (notComparableFeilds == null) {
                notComparableFeilds = Collections.emptyList();
            }
            try {
                sourceStatement = source.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                targetStatement = target.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                sourceRS = sourceStatement.executeQuery(remoteSelectSql);
                targetRS = targetStatement.executeQuery(localSelectSql);
                ResultSetMetaData sourceMeta = sourceRS.getMetaData();
                while (sourceRS.next() && targetRS.next()) {
                    for (int i = 1; i <= sourceMeta.getColumnCount(); i++) {
                        if (notComparableFeilds.contains(sourceMeta.getColumnName(i))) {
                            continue;
                        }
                        int type = sourceMeta.getColumnType(i);
                        Object sourceObj;
                        Object targetObj;
                        if (type == Types.NUMERIC) {
                            sourceObj = sourceRS.getBigDecimal(i);
                            targetObj = targetRS.getBigDecimal(i);
                        } else if (type == Types.BIGINT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
                            sourceObj = sourceRS.getLong(i);
                            targetObj = targetRS.getLong(i);
                        } else {
                            sourceObj = sourceRS.getObject(i);
                            targetObj = targetRS.getObject(i);
                        }
                        if (sourceObj instanceof Comparable && targetObj instanceof Comparable) {
                            if (((Comparable) sourceObj).compareTo(targetObj) != 0) {
                                return false;
                            }
                        } else {
                            if ((sourceObj != targetObj) && ((sourceObj == null ^ targetObj == null) || !sourceObj.equals(targetObj))) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } finally {
                close(sourceStatement, sourceRS);
                close(targetStatement, targetRS);
            }
        } else {
            return false;
        }
    }

    private Connection getConnection(PosSettings settings) throws SQLException {
        return DriverManager.getConnection(settings.getConnectionString());
    }
}
