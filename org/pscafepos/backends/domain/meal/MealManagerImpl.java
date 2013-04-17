package org.pscafepos.backends.domain.meal;

import org.pscafepos.backends.database.dao.BasePOSDao;
import org.pscafepos.backends.database.dao.DAOException;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.model.Student;
import org.pscafepos.backends.pos.IPosSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author bagmanov
 *         Date: 26.08.2009
 */
public class MealManagerImpl extends BasePOSDao implements MealManager {

    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    public MealManagerImpl() {
    }

    public MealManagerImpl(Connection connection, IPosSettings settings) {
        super(connection, settings);
    }

    //TODO: this is really strange logic
    public boolean canGetSpecialMeal(Student student, int mealType) throws DAOException {
        if (student.isStudentSet()) {
            if (mealType == OrderItem.MEAL_TYPE_FREE_REDUCED_BREAKFAST ||
                    mealType == OrderItem.MEAL_TYPE_FREE_REDUCED_LUNCH) {
                String fld;
                if (student.canGetFreeMeal())
                    fld = "ti_isFree";
                else if (student.canGetReducedMeal())
                    fld = "ti_isReduced";
                else
                    return false;
                String today = SHORT_DATE_FORMAT.format(new Date());
                String sql = "select count(*) from (SELECT * FROM " + posTablesPrefix + "trans_item WHERE ti_studentid = '" + student.getStudentNumber() + "' AND ti_datetime >= '" + today + "'  AND " + fld + " = '1' ) " + posTablesPrefix + "trans_item inner join ( SELECT * FROM " + posTablesPrefix + "items WHERE item_fr_bl = " + mealType + ") " + posTablesPrefix + "items on (" + posTablesPrefix + "trans_item.ti_itemid = " + posTablesPrefix + "items.item_id)";
                List<Integer> something = executeQuery(sql, new ResultsetProcessor<Integer>() {
                    public Integer processEntity(ResultSet resultSet) throws SQLException {
                        return resultSet.getInt(1);
                    }
                });
                return something.contains(0);
            }
        }
        return false;
    }
}
