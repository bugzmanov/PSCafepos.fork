package org.pscafepos.backends.domain.category;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.DAOException;

import java.util.List;

/**
 * @author bagmanov
 */
public interface CategoryDAO extends TransactionalDAO {
    List<String> listVisibleCategories() throws DAOException;

    List<String> listVisibleBatchCategories() throws DAOException;
}
