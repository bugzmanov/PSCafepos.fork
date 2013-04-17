package org.pscafepos.backends.domain.items;

import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.DAOException;

import java.util.List;

/**
 * @author bagmanov
 */
public interface ItemsPackageDAO extends TransactionalDAO {
    List<ItemsPackage> findByCategory(String category) throws DAOException;
}
