package org.pscafepos.backends.domain.pos;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 */
public interface GlobalPOSProperties extends TransactionalDAO {
    String getMessage() throws DAOException;

    boolean hasMessage() throws DAOException;

    String getGeneralSettings(String key) throws DAOException;
}
