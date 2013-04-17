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
package org.pscafepos.configuration;

import org.pscafepos.backends.database.jdbc.ConnectionSettings;
import static org.pscafepos.util.StringUtils.isEmpty;

public class SisSettings implements ConnectionSettings {

    boolean allowOnlyActiveStudents, allowOnlyStudentsThatExist;
    boolean fixedStudentIDWidth, allowSpaces;
    boolean blStudentIDIsString, blIsFreeReducedEnabled, blHideFreeRed;
    String sisTitle, jdbcDriver, connectionString, studentTable;
    String studentIDField, freeField, reducedField, activeField;
    String strValueFree, strValueReduced, strValueActive;
    String firstNameField, lastNameField;
    boolean blTypeFreeIsString, blTypeReducedIsString, blTypeActiveIsString;
    String imagePath;
    int studentIDValueLength;


    String connectionUserName;
    String connectionPassword;
    private String fullConnectionString;

    public SisSettings() {
        this.sisTitle = "";
    }

    public String getDriverName() {
        return jdbcDriver;
    }

    public String getConnectionString() {
        if(isEmpty(connectionUserName)){
            return connectionString;
        } else {
            if(isEmpty(fullConnectionString)) {
                fullConnectionString = connectionString + "?user=" + connectionUserName + "&password=" + connectionPassword;
            }
            return fullConnectionString;
        }
    }

    public String getConnectionUserName() {
        return connectionUserName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public String getSISName() {
        return sisTitle;
    }

    public boolean allowOnlyActiveStudents() {
        return allowOnlyActiveStudents;
    }

    public boolean allowOnlyStudentsThatExist() {
        return allowOnlyStudentsThatExist;
    }

    public String activeFieldName() {
        return activeField;
    }

    public boolean isActiveFieldString() {
        return blTypeActiveIsString;
    }

    public String activeFieldValue() {
        return strValueActive;
    }

    public boolean idFixedWidth() {
        return fixedStudentIDWidth;
    }

    public int idFixedWidthLength() {
        return studentIDValueLength;
    }

    public boolean allowSpacesInID() {
        return allowSpaces;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean useFreeReduced() {
        return blIsFreeReducedEnabled;
    }

    public boolean hideFreeReduced() {
        return blHideFreeRed;
    }

    public String getStudentsTable() {
        return studentTable;
    }

    public String studentIDFieldName() {
        return studentIDField;
    }

    public boolean isStudentIDFieldString() {
        return blStudentIDIsString;
    }

    public String freeFieldName() {
        return freeField;
    }

    public String reducedFieldName() {
        return reducedField;
    }

    public String getFirstNameField() {
        return firstNameField;
    }

    public String getLastNameField() {
        return lastNameField;
    }

    public boolean isFreeString() {
        return blTypeFreeIsString;
    }

    public boolean isReducedString() {
        return blTypeReducedIsString;
    }

    public String valueFree() {
        return strValueFree;
    }

    public String valueReduced() {
        return strValueReduced;
    }

    public String getTestConnectionQuery() {
        return "select * from "+getStudentsTable();
    }
}