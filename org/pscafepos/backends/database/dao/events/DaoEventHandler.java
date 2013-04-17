package org.pscafepos.backends.database.dao.events;

import java.sql.Connection;

/**
 * @author bagmanov
 */
public interface DaoEventHandler {
    public void onUpdateSql(Connection connection, String sql);
    public void onQuerySql (String sql);
}
