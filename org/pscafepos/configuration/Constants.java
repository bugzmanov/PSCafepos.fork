package org.pscafepos.configuration;

/**
 * @author bagmanov
 */
public class Constants {

    private Constants() {}

    public static final String VERSION = "1.1.4";

    public static final String LOCALDB_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String LOCALDB_USERNAME = "";
    public static final String LOCALDB_PASSWORD = "";
    public static final String LOCALDB_name = "localDB";
    public static final String LOCALDB_CONNECTION_STRING = "jdbc:derby:"+LOCALDB_name+";create=true";
    public static final String LOCALDB_UPDATEHOLDER_TABLE = "update_holder";

    public static final String STAFF_ID_PREFIX = "a";
}
