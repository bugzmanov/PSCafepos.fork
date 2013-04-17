/*   PSCafePOS is an Open Source Point of Sale System for Schools
 *   Copyright (C) 2007 Charles Syperski
 *
 *   This program is free software; you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation; either version 2 of the License, or 
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License 
 *   along with this program; if not, write to the 
 *   Free Software Foundation, Inc., 
 *   59 Temple Place, Suite 330, 
 *   Boston, MA 02111-1307 USA
 */
package org.pscafepos.backends.domain.pos;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.pos.IPosSettings;
import org.pscafepos.configuration.Constants;
import org.pscafepos.util.Utils;
import static org.pscafepos.util.Utils.getIPAddress;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PosRegistrationAuditImpl extends BasePOSDao implements PosRegistrationAudit {
    private static final Logger logger = Logger.getLogger(PosRegistrationAuditImpl.class.getName());

    private String buildingNumber;

    public PosRegistrationAuditImpl(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public PosRegistrationAuditImpl(Connection connection, IPosSettings settings, String buildingNumber) {
        super(connection, settings);
        this.buildingNumber = buildingNumber;
    }

    private String getOS() {
        return System.getProperty("os.name");
    }

    public int getPOSRegistrationID() throws DAOException {
        String sql = "select pr_id from " + posTablesPrefix + "pos_registration where " +
                "pr_hostname = '" + Utils.getHostName() + "' " +
                "and pr_building = '" + buildingNumber + "'";
        Integer regId = executeSingleResultQuery(sql, new ResultsetProcessor<Integer>() {
            public Integer processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt("pr_id");
            }
        });
        return regId != null ? regId : NOTFOUND;
    }

    private boolean registrationRecordExists() throws DAOException {
        String sql = "select count(*) as cnt from " + posTablesPrefix + "pos_registration where " +
                "pr_hostname = '" + Utils.getHostName() + "' and " +
                "pr_building = '" + buildingNumber + "'";
        Boolean recordExist = executeSingleResultQuery(sql, new ResultsetProcessor<Boolean>() {
            public Boolean processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt("cnt") > 0;
            }
        });
        return recordExist != null ? recordExist : false;
    }
    
    private void createRegistrationRecord(String userName) throws DAOException {
        String timeStamp = currentTimeStamp();
        String sql = "insert into " + posTablesPrefix + "pos_registration" +
                "(pr_hostname, pr_ipaddress, pr_lastupdate, pr_createdate, pr_enabled, pr_version, pr_os, pr_building, pr_lastuser) values" +
                "('" + Utils.getHostName() + "', '" + getIPAddress() + "', '"+timeStamp+"', '"+timeStamp+"', '1', '" + Constants.VERSION + "', '" + getOS() + "', '" + buildingNumber + "', '" + userName + "')";
        executeUpdateSql(sql);
    }

    private void updateRegistrationRecord(String userName) throws DAOException {
        String sql = "update " + posTablesPrefix + "pos_registration set " +
                "pr_ipaddress = '" + getIPAddress() + "', " +
                "pr_lastupdate = '"+currentTimeStamp()+"', " +
                "pr_version = '" + Constants.VERSION + "', " +
                "pr_os = '" + getOS() + "', " +
                "pr_building = '" + buildingNumber + "', " +
                "pr_lastuser = '" + userName + "' where " +
                    "pr_hostname = '" + Utils.getHostName() + "' and " +
                    "pr_building = '" + buildingNumber + "'";
        executeUpdateSql(sql);
    }

    public boolean logRegisterEvent(String userName) {
        try {
            if(registrationRecordExists()){
                updateRegistrationRecord(userName);
            } else {
                createRegistrationRecord(userName);
            }
            return true;
        } catch (DAOException e) {
            logger.log(Level.SEVERE, "couldn't update/insert registration record ", e);
            return false;
        }
    }
}