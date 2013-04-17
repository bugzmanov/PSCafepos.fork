package org.pscafepos.backends.domain.meal;

import org.pscafepos.model.Student;
import org.pscafepos.backends.database.TransactionalDAO;
import org.pscafepos.backends.database.dao.DAOException;

/**
 * @author bagmanov
 */
public interface MealManager extends TransactionalDAO {
    //TODO: very strange logic
    boolean canGetSpecialMeal(Student student, int mealType) throws DAOException;
}
