package org.pscafepos.configuration;

import org.pscafepos.backends.database.jdbc.ConnectionSettings;
import org.pscafepos.util.StringUtils;

import java.util.logging.Logger;

import org.pscafepos.backends.pos.IPosSettings;

/**
 * @author bagmanov
 *         Date: 01.09.2009
 */
public class PosSettings implements IPosSettings, ConnectionSettings {
    private static final Logger logger = Logger.getLogger(PosSettings.class.getName());

    private String posDriver;
    private String hostName;
    private String databaseName;
    private String dbPort;
    private String dbType;
    private boolean isSslEnabled;
    private String userName;
    private String password;

    private String tablesPrefix;

    public String getTablesPrefix() {
        return tablesPrefix;
    }

    public String getConnectionString() {
        String sslString = isSslEnabled ? "&useSSL=true&requireSSL=true" : "";
        String dbPortStr = StringUtils.isEmpty(dbPort) ? "" : ":" + dbPort;
        if(password == null){
            password = "";
        }
        if (userName == null) {
            userName = "";
        }
        return "jdbc:" + dbType + "://" + hostName + dbPortStr + "/" + databaseName + "?user=" + userName + "&password=" + password + sslString;
    }

    public String getDriverName() {
         return posDriver;
    }


    public void setPosDriver(String posDriver) {
        this.posDriver = posDriver;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setSslEnabled(boolean sslEnabled) {
        isSslEnabled = sslEnabled;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTablesPrefix(String tablesPrefix) {
        this.tablesPrefix = tablesPrefix;
    }

    public String getTestConnectionQuery() {
        return "select * from "+getTablesPrefix()+"items";
    }
}
