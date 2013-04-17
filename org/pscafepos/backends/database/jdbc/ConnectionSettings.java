package org.pscafepos.backends.database.jdbc;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public interface ConnectionSettings {
    String getConnectionString();
    String getDriverName();
    String getTestConnectionQuery();
}
