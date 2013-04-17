package org.pscafepos.backends.domain.pos;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 */
public interface PosRegistrationAudit extends TransactionalDAO {
    int NOTFOUND = -1;

    int getPOSRegistrationID() throws DAOException;

    boolean logRegisterEvent(String userName);
}
