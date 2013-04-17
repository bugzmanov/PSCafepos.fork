package org.pscafepos.backends.sis;

import org.pscafepos.model.Student;

/**
 * @author bagmanov
 */
public class SchoolOfficer implements Student {
    private String studentName;
    private String id;

    public SchoolOfficer(String studentName, String id) {
        this.studentName = studentName;
        this.id = id;
    }

    public boolean isStudentSet() {
        return true;
    }

    public boolean canGetFreeMeal() {
        return true;
    }

    public boolean canGetReducedMeal() {
        return false;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNumber() {
        return id;
    }

    public String getMealStatus() {
        return "Free";
    }

    public boolean getIsActiveStudent() {
        return true;
    }

    public boolean getExistsInDB() {
        return true;
    }

    public boolean isAnonStudent() {
        return false;
    }

    public String getType() {
        return "staff";
    }

  @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchoolOfficer that = (SchoolOfficer) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        return result;
    }
}
