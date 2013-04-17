package org.pscafepos.configuration.local;

import org.pscafepos.configuration.Constants;
import org.pscafepos.configuration.SisSettings;
import static org.pscafepos.util.StringUtils.isNotEmpty;

/**
 * @author bagmanov
 */
public class LocalSisSettings extends SisSettings {
    private SisSettings settings;


    public LocalSisSettings(SisSettings settings) {
        this.settings = settings;
    }

    public String valueReduced() {
        return settings.valueReduced();
    }

    public boolean allowOnlyActiveStudents() {
        return settings.allowOnlyActiveStudents();
    }

    public boolean allowOnlyStudentsThatExist() {
        return settings.allowOnlyStudentsThatExist();
    }


    public String activeFieldValue() {
        return settings.activeFieldValue();
    }

    public boolean idFixedWidth() {
        return settings.idFixedWidth();
    }

    public int idFixedWidthLength() {
        return settings.idFixedWidthLength();
    }

    public boolean allowSpacesInID() {
        return settings.allowSpacesInID();
    }

    public String getImagePath() {
        return settings.getImagePath();
    }

    public boolean useFreeReduced() {
        return settings.useFreeReduced();
    }

    public boolean hideFreeReduced() {
        return settings.hideFreeReduced();
    }

    public String valueFree() {
        return settings.valueFree();
    }


//
    @Override
    public String getSISName() {
      return "Local SIS";
    }

    @Override
    public boolean isActiveFieldString() {
        return true;
    }

    @Override
    public String getStudentsTable() {
        return "sis_students";
    }

    @Override
    public String studentIDFieldName() {
        return "student_id";
    }

    @Override
    public boolean isStudentIDFieldString() {
        return true;
    }

    /**
     * As empty value of parent indicates whether remote SIS has active field, local SIS should mimic this behaviour
     * @return db column name
     */
    @Override
    public String activeFieldName() {
        return isNotEmpty(settings.activeFieldName()) ? "active" : settings.activeFieldName();
    }

    @Override
    public String freeFieldName() {
        return isNotEmpty(settings.freeFieldName()) ? "free" : settings.freeFieldName();
    }

    @Override
    public String reducedFieldName() {
        return isNotEmpty(settings.reducedFieldName()) ? "reduced" : settings.reducedFieldName();
    }

    @Override
    public String getFirstNameField() {
        return isNotEmpty(settings.getFirstNameField()) ? "first_name" : settings.getFirstNameField();
    }

    @Override
    public String getLastNameField() {
        return isNotEmpty(settings.getLastNameField()) ? "last_name" : settings.getLastNameField();
    }

    @Override
    public boolean isFreeString() {
        return true;
    }

    @Override
    public boolean isReducedString() {
        return true;
    }

    @Override
    public String getDriverName() {
        return Constants.LOCALDB_JDBC_DRIVER;
    }

    @Override
    public String getConnectionString() {
        return Constants.LOCALDB_CONNECTION_STRING;
    }

    @Override
    public String getConnectionUserName() {
        return Constants.LOCALDB_USERNAME;
    }

    @Override
    public String getConnectionPassword() {
        return Constants.LOCALDB_PASSWORD;
    }
}
