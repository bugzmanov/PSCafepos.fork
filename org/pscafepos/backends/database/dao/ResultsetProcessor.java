package org.pscafepos.backends.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author bagmanov
 *         Date: 25.08.2009
 */
public interface ResultsetProcessor <T>{
    public T processEntity(ResultSet resultSet) throws SQLException;
}
