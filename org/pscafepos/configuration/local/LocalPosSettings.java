package org.pscafepos.configuration.local;

import org.pscafepos.configuration.Constants;
import org.pscafepos.util.StringUtils;
import org.pscafepos.configuration.PosSettings;

/**
 * @author bagmanov
 *         Date: 01.09.2009
 */
public class LocalPosSettings extends PosSettings {

    private String tablesPrefix;

    public LocalPosSettings(String tablesPrefix) {
        this.tablesPrefix = tablesPrefix;
    }

    @Override
    public String getTablesPrefix() {
        return tablesPrefix;
    }

    @Override
    public String getConnectionString() {
        if(StringUtils.isNotEmpty(Constants.LOCALDB_USERNAME)){
            return Constants.LOCALDB_CONNECTION_STRING + "?user="+ Constants.LOCALDB_USERNAME + "&password=" + Constants.LOCALDB_PASSWORD;
        } else {
            return Constants.LOCALDB_CONNECTION_STRING ;
        }
    }

    @Override
    public String getDriverName() {
        return Constants.LOCALDB_JDBC_DRIVER;
    }
}
