package org.pscafepos.backends.domain.sis;

import org.pscafepos.backends.database.dao.BaseJDBCDao;
import org.pscafepos.backends.database.dao.ResultsetProcessor;
import org.pscafepos.model.Student;
import org.pscafepos.configuration.SisSettings;
import org.pscafepos.backends.sis.SchoolStudent;
import static org.pscafepos.util.StringUtils.isNotEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author bagmanov
 */
public class StudentInformationDAOImpl extends BaseJDBCDao implements StudentInformationDAO {
    private SisSettings settings;

    public void setSettings(SisSettings settings) {
        this.settings = settings;
    }

    public StudentInformationDAOImpl() {
    }

    public StudentInformationDAOImpl(Connection connector, SisSettings sisSettings) {
        super(connector);
        this.settings = sisSettings;
    }

    public Student getStudent(final String studentId) {
        String quote = "";
        if (settings.isStudentIDFieldString())
            quote = "'";

        final String nameFields = getNameFieldsAlias();
        final String activeField = getActiveFieldsAlias();

        String sql = "select " + settings.freeFieldName() + ", " + settings.reducedFieldName() + activeField + nameFields + " from " + settings.getStudentsTable() + " where " + settings.studentIDFieldName() + " = " + quote + studentId + quote;
        StudentResultSetProcessor processor = new StudentResultSetProcessor(!nameFields.isEmpty(), !activeField.isEmpty());
        SchoolStudent student = executeSingleResultQuerySilently(sql, processor);
        if(student != null) {
            student.setId(studentId);
        }
        return student;
    }

    private String getActiveFieldsAlias() {
        String activeField;
        activeField = isNotEmpty(settings.activeFieldName()) ? ", " + settings.activeFieldName() : "";
        return activeField;
    }

    private String getNameFieldsAlias() {
        String nameFields;
        if (isNotEmpty(settings.getLastNameField())) {
            nameFields = ", " + settings.getFirstNameField() + ", " + settings.getLastNameField() + " ";
        } else {
            nameFields = "";
        }
        return nameFields;
    }

    private class StudentResultSetProcessor implements ResultsetProcessor<SchoolStudent> {
        private boolean containsNameFields;
        private boolean containsIsActiveField;

        private StudentResultSetProcessor(boolean containsNameFields, boolean containsIsActiveField) {
            this.containsNameFields = containsNameFields;
            this.containsIsActiveField = containsIsActiveField;
        }

        public SchoolStudent processEntity(ResultSet resultSet) throws SQLException {
            SchoolStudent student = new SchoolStudent();
            if (containsNameFields) {
                student.setFirstName(resultSet.getString(settings.getFirstNameField()));
                student.setLastName(resultSet.getString(settings.getLastNameField()));
            } else {
                student.setFirstName("");
                student.setLastName("");
            }
            String freeFieldValue = resultSet.getString(settings.freeFieldName());
            boolean isFree = freeFieldValue != null && freeFieldValue.equalsIgnoreCase(settings.valueFree());
            student.setFree(isFree);
            String reducedFieldValue = resultSet.getString(settings.reducedFieldName());
            boolean isReduced = reducedFieldValue != null && reducedFieldValue.equalsIgnoreCase(settings.valueReduced());
            student.setReduced(isReduced);
            student.setExists(true);

            if (containsIsActiveField) {
                String activeFieldValue = resultSet.getString(settings.activeFieldName());
                boolean isActive = activeFieldValue != null &&
                        activeFieldValue.equalsIgnoreCase(settings.activeFieldValue());
                student.setActive(isActive);
            } else {
                student.setActive(true);
            }
            return student;
        }
    }
}
