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

import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;

public class GlobalPOSPropertiesImpl extends BasePOSDao implements GlobalPOSProperties {

    private int registrationID;
    private Map<String, String> propertiesCach;

    public GlobalPOSPropertiesImpl(int registrationID) {
        this.registrationID = registrationID;
        propertiesCach = new HashMap<String,String>();
    }

    public GlobalPOSPropertiesImpl(Connection connection, IPosSettings settings, int posRegistrationId) {
        super(connection, settings);
        this.registrationID = posRegistrationId;
    }

    public String getMessage() throws DAOException {
        String sql = "select m_text, m_id from " + posTablesPrefix + "pos_messages where m_posid = " + registrationID + " and m_viewed <> '1'";
        List<String> messages = executeQuery(sql, new ResultsetProcessor<String>() {
            public String processEntity(ResultSet resultSet) throws SQLException {
                String updateSql = "update " + posTablesPrefix + "pos_messages set m_viewed = '1' where m_id = '" + resultSet.getInt("m_id") + "'";
                executeUpdateSqlSilently(updateSql);
                return resultSet.getString("m_text");
            }
        });
        return messages.isEmpty() ? null : messages.get(0);
    }

    public boolean hasMessage() throws DAOException {
        String sql = "select count(*) as cnt from " + posTablesPrefix + "pos_messages where m_posid = '" + registrationID + "' and m_viewed <> '1'";
        Boolean hasMessage = executeSingleResultQuery(sql, new ResultsetProcessor<Boolean>() {
            public Boolean processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getInt("cnt") > 0;
            }
        });
        return hasMessage != null ? hasMessage : false;
    }

    public String getGeneralSettings(String key) throws DAOException {
        String value = propertiesCach.get(key);
        if(propertiesCach.containsKey(key)){
            return value;
        }
        String sql = "select set_value from " + posTablesPrefix + "pos_settings where set_key = '" + key + "' and set_posid = '" + registrationID + "'";
        value =  executeSingleResultQuery(sql, new ResultsetProcessor<String>() {
            public String processEntity(ResultSet resultSet) throws SQLException {
                return resultSet.getString("set_value");
            }
        });
        propertiesCach.put(key, value);
        return value;
    }
}
