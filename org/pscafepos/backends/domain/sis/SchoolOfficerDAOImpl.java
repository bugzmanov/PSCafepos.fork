package org.pscafepos.backends.domain.sis;

import org.pscafepos.backends.database.dao.BaseJDBCDao;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.backends.sis.SchoolOfficer;
import org.pscafepos.model.Student;
import org.pscafepos.configuration.SisSettings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author bagmanov
 */
public class SchoolOfficerDAOImpl extends BaseJDBCDao implements SchoolOfficerDAO{
    private static final SchoolOfficerResultProcessor RESULT_PROCESSOR = new SchoolOfficerResultProcessor(); 
    private SisSettings settings;

     public void setSettings(SisSettings settings) {
         this.settings = settings;
     }

     public SchoolOfficerDAOImpl() {
     }

     public SchoolOfficerDAOImpl(Connection connector, SisSettings sisSettings) {
         super(connector);
         this.settings = sisSettings;
     }


    public Student loadOfficer(String id) {
        String sql = "select * from users where staff_id = "+id;
        return executeSingleResultQuerySilently(sql, RESULT_PROCESSOR);
    }

    private static class SchoolOfficerResultProcessor implements ResultsetProcessor<SchoolOfficer> {

        public SchoolOfficer processEntity(ResultSet resultSet) throws SQLException {
            String name = resultSet.getString("first_name") + resultSet.getString("last_name");
            String id = resultSet.getString("staff_id");
            SchoolOfficer officer = new SchoolOfficer(name, id);
            return officer;
        }
    }
}
