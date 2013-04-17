/*   PSCafePOS is an Open Source Point of Sale System for Schools
 *   Copyright (C) 2007 Charles Syperski
 *
 *   This program is free software; you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation; either version 2 of the License, or 
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License 
 *   along with this program; if not, write to the 
 *   Free Software Foundation, Inc., 
 *   59 Temple Place, Suite 330, 
 *   Boston, MA 02111-1307 USA
 */
package org.pscafepos.backends.sis;
import org.pscafepos.model.Student;

import java.io.Serializable;

public class SchoolStudent implements Student, Serializable {
    String id, firstName, lastName;
    boolean isFree, isReduced, isActive, exists;

    public SchoolStudent() {
        isFree = false;
        isReduced = false;
        firstName = "";
        lastName = "";
        isActive = false;
        exists = false;
    }
 
    public boolean getIsActiveStudent() {
        return isActive;
    }

    public boolean getExistsInDB() {
        return exists;
    }

    public boolean canGetReducedMeal() {
        return isReduced;
    }

    public boolean canGetFreeMeal() {
        return isFree;
    }

    public String getMealStatus() {
        if (canGetFreeMeal()) {
            return "Free";
        }

        if (canGetReducedMeal()) {
            return "Reduced";
        }

        return "Normal";

    }

    public String getStudentName() {
        return firstName + " " + lastName;
    }

    public String getStudentNumber() {
        return id;
    }

    public boolean isStudentSet() {
        return id != null && (!id.equals(Student.NOSTUDENT));

    }

    public boolean isAnonStudent() {
        return id != null && (id.equals(Student.NOSTUDENT));
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFree(boolean free) {
        this.isFree = free;
    }

    public void setReduced(boolean reduced) {
        this.isReduced = reduced;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getType() {
        return "student";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchoolStudent that = (SchoolStudent) o;

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
