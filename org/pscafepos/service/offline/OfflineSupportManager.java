package org.pscafepos.service.offline;

import org.pscafepos.configuration.PosSettings;
import org.pscafepos.backends.database.jdbc.JdbcConnector;
import org.pscafepos.backends.database.jdbc.JdbcConnectorImpl;
import org.pscafepos.backends.database.jdbc.PoolPoweredJdbcConnector;
import org.pscafepos.configuration.local.LocalSisSettings;
import org.pscafepos.configuration.local.LocalPosSettings;
import org.pscafepos.configuration.SisSettings;

/**
 * @author Bagmanov
 *         Date: 08.09.2009
 */
public class OfflineSupportManager {
    private SisSettings sisSettings;
    private PosSettings posSettings;
    private PosSettings localPosSettings;
    private SisSettings locaSisSettings;

    public OfflineSupportManager(SisSettings sisSettings, PosSettings posSettings) {
        this.sisSettings = sisSettings;
        this.posSettings = posSettings;
        locaSisSettings = new LocalSisSettings(sisSettings);
        localPosSettings = new LocalPosSettings(posSettings.getTablesPrefix());
    }

    public SisSettings getLocalSisSettings(){
        return locaSisSettings;
    }

    public SisSettings getRemoteSisSettings(){
        return sisSettings;
    }

    public PosSettings getLocalPosSettings() {
        return localPosSettings;
    }

    public PosSettings getRemotePosSettings() {
        return posSettings;
    }

    public JdbcConnector getRemoteSISConnector() {
        return new PoolPoweredJdbcConnector("Remote SIS", sisSettings, 5 * 1000);
//        return new JdbcConnectorImpl("Remote SIS", sisSettings);
    }

    public JdbcConnector getLocalSISConnector() {
        return new PoolPoweredJdbcConnector("Local SIS", locaSisSettings, 10 * 1000);
    }

    public JdbcConnector getRemotePOSConnector(){
        return new PoolPoweredJdbcConnector("Remote POS", posSettings, 5 * 1000);
//        return new JdbcConnectorImpl("Remote POS", posSettings);

    }

    public JdbcConnector getLocalPOSConnector() {
        return new PoolPoweredJdbcConnector("Local POS", localPosSettings, 10 * 1000);
    }
}
