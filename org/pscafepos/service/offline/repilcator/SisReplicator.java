package org.pscafepos.service.offline.repilcator;

import org.pscafepos.backends.database.jdbc.JdbcConnectorImpl;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.configuration.SisSettings;
import static org.pscafepos.util.StringUtils.isNotEmpty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bagmanov
 */
public class SisReplicator extends ReplicatorBase {
    private SisSettings localSis;
    private SisSettings remoteSis;
    private JdbcConnectorImpl localConnector;
    private JdbcConnectorImpl remoteConnector;


    public SisReplicator(SisSettings localSis, SisSettings remoteSis) {
        this.localSis = localSis;
        this.remoteSis = remoteSis;
        localConnector = new JdbcConnectorImpl("Local SIS", localSis);
        remoteConnector = new JdbcConnectorImpl("Remote SIS", remoteSis);
    }

    public List<String> getUnsynchronizedTablesList() throws ReplicatorConnectionException, ReplicatorException {
        List<String> tables = new ArrayList<String>();
        try {
            Connection remoteConnection = remoteConnector.getNewConnection();
            Connection localConnection = null;
            try {
                localConnection = localConnector.getNewConnection();
                int localCount = getTableRecordsCount(localConnection, localSis.getStudentsTable());
                int remoteCount = getTableRecordsCount(remoteConnection, remoteSis.getStudentsTable());
                if (localCount != remoteCount) {
                    tables.add(remoteSis.getStudentsTable());
                }
                int staffLocalCount = getTableRecordsCount(localConnection, "users");
                int staffRemoteCount = getTableRecordsCount(remoteConnection, "users");
                if(staffLocalCount != staffRemoteCount) {
                    tables.add("users");
                }
                return tables;
            } catch (JdbcConnectorException e) {
                throw new ReplicatorException("Couldn't connect to local SIS");
            } finally {
                JdbcUtils.closeIfNeededSilently(remoteConnection);
                JdbcUtils.closeIfNeededSilently(localConnection);
            }
        } catch (JdbcConnectorException e) {
            throw new ReplicatorConnectionException("Couldn't connect to remote SIS");
        }
    }

    public void replicate(List<String> tables) throws ReplicatorException {
        Connection remoteConnection = null;
        try {
            remoteConnection = remoteConnector.getNewConnection();
            Connection localConnection = null;
            try {
                localConnection = localConnector.getNewConnection();
                localConnection.setAutoCommit(false);
                clean(localConnection, localSis.getStudentsTable());
                replicateStudentDataFromRemoteSis(remoteConnection, localConnection);

                clean(localConnection, "users");
                replicateStaffDataFromRemoteSis(remoteConnection, localConnection);

                localConnection.commit();
            } catch (SQLException e) {
                JdbcUtils.rollbackSilently(localConnection);
                throw new ReplicatorException("Couldn't transfer data from remote to local SIS", e);
            } catch (JdbcConnectorException e) {
                throw new ReplicatorException("Couldn't connect to local SIS");
            } finally {
                JdbcUtils.closeIfNeededSilently(localConnection);
            }
        } catch(JdbcConnectorException ex) {
            throw new ReplicatorConnectionException("Couldn't connect to remote SIS", ex);
        } finally {
            JdbcUtils.closeIfNeededSilently(remoteConnection);
        }

    }

    private void replicateStaffDataFromRemoteSis(Connection remoteConnection, Connection localConnection) throws SQLException{
        String selectSQL = "select staff_id, first_name, last_name from users";
        String insertSQL = "insert into users (staff_id, first_name, last_name) values (?, ?, ?)";
        PreparedStatement insertStatement = localConnection.prepareStatement(insertSQL);
        Statement statement = remoteConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSQL);
        while(resultSet.next()){
            insertStatement.setLong(1, Long.valueOf(resultSet.getString("staff_id")));
            insertStatement.setString(2, resultSet.getString("first_name"));
            insertStatement.setString(3, resultSet.getString("last_name"));
            insertStatement.executeUpdate();
            insertStatement.clearParameters();
        }

    }


    private void replicateStudentDataFromRemoteSis(Connection source, Connection target) throws SQLException {
        String selectSQL = generateSelectSQL(remoteSis);
        String insertSQL = generateInsertSQL(localSis);

        PreparedStatement insertStatement = target.prepareStatement(insertSQL);
        Statement statement = source.createStatement();
        ResultSet resultSet = statement.executeQuery(selectSQL);
        while (resultSet.next()) {
            fillStatement(insertStatement, resultSet);
            insertStatement.executeUpdate();
            insertStatement.clearParameters();
        }
    }

    private void fillStatement(PreparedStatement insertStatement, ResultSet resultSet) throws SQLException {
        insertStatement.setString(1, resultSet.getString(1));
        insertStatement.setString(2, resultSet.getString(2));
        insertStatement.setString(3, resultSet.getString(3));
        int resultSetCoulmnPosition = 4;
        if (isNotEmpty(remoteSis.activeFieldName())) {
            insertStatement.setString(resultSetCoulmnPosition, resultSet.getString(resultSetCoulmnPosition));
            resultSetCoulmnPosition++;
        }
        if (isNotEmpty(remoteSis.getLastNameField())) {
            insertStatement.setString(resultSetCoulmnPosition, resultSet.getString(resultSetCoulmnPosition++));
            insertStatement.setString(resultSetCoulmnPosition, resultSet.getString(resultSetCoulmnPosition));
        }
    }

    private String generateInsertSQL(SisSettings sisSettings) {
        return "insert into " + sisSettings.getStudentsTable() + " (" +
                sisSettings.studentIDFieldName() + ", " +
                sisSettings.freeFieldName() + ", " +
                sisSettings.reducedFieldName() +
                (isNotEmpty(sisSettings.activeFieldName()) ? "," + sisSettings.activeFieldName() : "") +
                (isNotEmpty(sisSettings.getFirstNameField()) ? "," + sisSettings.getFirstNameField() : "") +
                (isNotEmpty(sisSettings.getLastNameField()) ? "," + sisSettings.getLastNameField() : "") +
                ") values ( ?, ?, ?" +
                (isNotEmpty(sisSettings.activeFieldName()) ? ", ?" : "") +
                (isNotEmpty(sisSettings.getFirstNameField()) ? ", ?" : "") +
                (isNotEmpty(sisSettings.getLastNameField()) ? ", ?" : "") +
                ")";
    }

    private String generateSelectSQL(SisSettings sisSettings) {
        String activeField = isNotEmpty(sisSettings.activeFieldName()) ? ", " + sisSettings.activeFieldName() : "";
        String nameFields = isNotEmpty(sisSettings.getLastNameField()) ?
                ", " + sisSettings.getFirstNameField() + ", " + sisSettings.getLastNameField() + " " : "";
        return "select " + sisSettings.studentIDFieldName() + "," +
                sisSettings.freeFieldName() + ", " +
                sisSettings.reducedFieldName() +
                activeField +
                nameFields +
                " from " + sisSettings.getStudentsTable();
    }
}
