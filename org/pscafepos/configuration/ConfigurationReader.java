package org.pscafepos.configuration;

import org.pscafepos.configuration.PosSettings;
import org.pscafepos.util.StringUtils;
import static org.pscafepos.util.StringUtils.isEmpty;
import static org.pscafepos.util.StringUtils.isNotEmpty;

import java.math.BigDecimal;

import org.pscafepos.configuration.ConfigurationException;
import org.pscafepos.settings.DBSettings;

/**
 * @author bagmanov
 */
public class ConfigurationReader {

    private boolean debugMode = false;
    private int messageID = 0;

    public SessionSettings read(DBSettings settings) throws ConfigurationException {
        debugMode = settings.getBool(DBSettings.MAIN_DEBUG);
        messageID = 0;
        SessionSettings sessionSettings = loadSessionSettings(settings);
        sessionSettings.posSettings = loadPosSettings(settings);
        sessionSettings.sisSettings = loadSisSettings(settings);
        return sessionSettings;

    }

    private SessionSettings loadSessionSettings(DBSettings settings) throws ConfigurationException {
        SessionSettings sessionSettings = new SessionSettings();
        sessionSettings.allowOnlyActiveStudents = settings.getBool(DBSettings.SIS_ALLOWONLYACTIVE);
        sessionSettings.allowOnlyStudentsExistedInSIS = settings.getBool(DBSettings.SIS_ALLOWONLYSTUDENTSTHATEXIST);
        sessionSettings.anonymousTransactionsAllowed = settings.getBool(DBSettings.MAIN_ALLOWANONTRANS);
        sessionSettings.buildingName = settings.get(DBSettings.MAIN_BUILDINGNAME);
        sessionSettings.buildingNumber = settings.get(DBSettings.MAIN_BUILDING);
        if(isEmpty(sessionSettings.buildingNumber)) {
            throw new ConfigurationException ("Building number is not set in configuration");
        }
        try {
            sessionSettings.maxNegativeBalance = new BigDecimal(settings.get(DBSettings.MAIN_MAXNEGBALANCE));
            if(sessionSettings.maxNegativeBalance.compareTo(BigDecimal.ZERO) < 0) {
                sessionSettings.maxNegativeBalance = sessionSettings.maxNegativeBalance.negate();
            }
        } catch (Exception e) {
            debug("Max negative balance value is incorrect. '0.0' will be used as Max nagative balance for current session");
            sessionSettings.maxNegativeBalance = BigDecimal.ZERO;
        }
        sessionSettings.negativeBalanceAllowed = settings.getBool(DBSettings.MAIN_ALLOWNEGBALANCES);
        sessionSettings.showFreeReducedData = settings.getBool(DBSettings.SIS_FREEREDUCEDENABLED) &&
                !settings.getBool(DBSettings.SIS_HIDEFREEREDUCEDINFO);
        sessionSettings.isDrawerEnabled = settings.getBool(DBSettings.DRAWER_ENABLED);
        sessionSettings.drawerClass = settings.get(DBSettings.DRAWER_CLASS);
        sessionSettings.drawerName = settings.get(DBSettings.DRAWER_COMMONNAME);
        sessionSettings.imageBackGround = settings.get(DBSettings.IMAGES_MAINBG);
        sessionSettings.sellOnCreditOnlyMode = settings.getBool(DBSettings.MAIN_SELL_ON_CREDIT_ONLY);
        sessionSettings.autoCheckOutMode = settings.getBool(DBSettings.MAIN_AUTO_CHECKOUT);
        return sessionSettings;
    }

    private PosSettings loadPosSettings(DBSettings settings) throws ConfigurationException {
        debug ("Starting load pos settings");
        PosSettings posSettings = new PosSettings();
        String posDriver = settings.get(DBSettings.POS_JDBCDRIVER);
        posSettings.setPosDriver(posDriver);
        debug("Loading pos database.jdbc driver [" + posDriver + "]");
        try {
            Class.forName(posDriver).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Couldn't load POS database.jdbc driver [" + posDriver + "]",e);
        }
        debug("Pos driver has been loaded...");
        String hostName = settings.get(DBSettings.POS_HOSTNAME);
        if(isEmpty(hostName)){
            throw new ConfigurationException("The POS Database Setting: [hostname] is not set in the config file.");
        }
        posSettings.setHostName(hostName);
        String databaseName = settings.get(DBSettings.POS_DATABASE);
        if(isEmpty(databaseName)) {
            throw new ConfigurationException("The POS Database Setting: [database] is not set in the config file.");
        }
        posSettings.setDatabaseName(databaseName);

        posSettings.setDbType(settings.get(DBSettings.POS_JDBCDBTYPE));
        posSettings.setSslEnabled(settings.getBool(DBSettings.POS_SSLENABLED));
        posSettings.setDbPort(settings.get(DBSettings.POS_JDBCPORT));
        posSettings.setTablesPrefix(settings.get(DBSettings.POS_TABLEPREFIX));
        debug("POS settings have been loaded");
        return posSettings;

    }


    private SisSettings loadSisSettings(DBSettings settings) throws ConfigurationException {
        debug("Loading SIS configuration");
        if (!settings.getBool(DBSettings.SIS_ENABLED)) {
            throw new ConfigurationException("Using POS's internal Student Information System currently is not supported. Please configure external sis");
        }
        debug("Using external SIS system, requesting more information...");
        SisSettings sisSettings = new SisSettings();
        sisSettings.sisTitle = settings.get(DBSettings.SIS_SYSTEMNAME);
        debug("Loading configuration for " + sisSettings.sisTitle);
        initJDBCDriver(settings, sisSettings);
        sisSettings.connectionString = settings.get(DBSettings.SIS_JDBCCONNECTIONSTRING);
        if (StringUtils.isEmpty(sisSettings.connectionString)) {
            throw new ConfigurationException("JDBC Connection string is either Null or Zero characters in length.");
        }
        sisSettings.connectionUserName = settings.get(DBSettings.SIS_CONNUSERNAME);
        sisSettings.connectionPassword = settings.get(DBSettings.SIS_CONNPASSWORD);
        sisSettings.studentTable = settings.get(DBSettings.SIS_REGISTRATIONTABLE);
        if (isEmpty(sisSettings.studentTable)) {
            throw new ConfigurationException("Registration table is not set, this should be the table that holds the student id and meal status.");
        }
        debug("Registration Table: " + sisSettings.studentTable);
        initStudentIDField(settings, sisSettings);

        sisSettings.firstNameField = settings.get(DBSettings.SIS_STUDENTFNAMEFIELD);
        sisSettings.lastNameField = settings.get(DBSettings.SIS_STUDENTLNAMEFIELD);

        initFreeReducedTypes(settings, sisSettings);
        debug("Done loading mandatory fields");
        debug("Attempting to load optional fields...");

        initAllowOnlyActiveStudents(settings, sisSettings);

        initAllowOnlyStudentsThatExist(settings, sisSettings);
        initAllowSpacesInID(settings, sisSettings);
        initFixedIDWidth(settings, sisSettings);
        initImagePath(settings, sisSettings);
        sisSettings.blHideFreeRed = settings.getBool(DBSettings.SIS_HIDEFREEREDUCEDINFO);
        debug((sisSettings.blHideFreeRed ? "Hiding " : "Not hiding") + "Free and reduced information from cashiers.");
        debug("Student Information System Configuration has been successfully loaded.");
        return sisSettings;
    }

    private void initJDBCDriver(DBSettings settings, SisSettings sisSettings) throws ConfigurationException {
        sisSettings.jdbcDriver = settings.get(DBSettings.SIS_JDBCDRIVER);
        if (isNotEmpty(sisSettings.jdbcDriver)) {
            debug("Trying to load SIS database.jdbc driver");
            try {
                Class.forName(sisSettings.jdbcDriver).newInstance();
            } catch (Exception ex) {
                throw new ConfigurationException ("Couldn't load SIS database.jdbc driver", ex);
            }
            debug ("SIS database.jdbc driver was successfully loaded");
        } else {
            throw new ConfigurationException("Sis JDBC Driver setting is empty.");
        }
    }

    private void initAllowOnlyActiveStudents(DBSettings settings, SisSettings sisSettings) {
        sisSettings.allowOnlyActiveStudents = settings.getBool(DBSettings.SIS_ALLOWONLYACTIVE);
        sisSettings.activeField = settings.get(DBSettings.SIS_STATUSFIELD);
        sisSettings.blTypeActiveIsString = !settings.get(DBSettings.SIS_STATUSTYPE).equalsIgnoreCase("number");
        sisSettings.strValueActive = settings.get(DBSettings.SIS_STATUSVALUE);
        if (sisSettings.allowOnlyActiveStudents) {
            debug("Requiring only active students...");
            if (isNotEmpty(sisSettings.activeField) && isNotEmpty(sisSettings.strValueActive)) {
                debug("  ++ Student Status Field = " + sisSettings.activeField);
                debug("  ++ Student Status Value (indicating active) = " + sisSettings.strValueActive);
                if (sisSettings.blTypeActiveIsString) {
                    debug("  ++ Student Status Type = String");
                } else {
                    debug("  ++ Student Status Type = Number");
                }
            } else {
                debug("Incomplete information to require only active students...allowing inactive students!");
                sisSettings.allowOnlyActiveStudents = false;
            }
        } else {
            debug("NOT requiring only active students.");
        }
    }

    private void initFreeReducedTypes(DBSettings settings, SisSettings sisSettings) {
        sisSettings.blIsFreeReducedEnabled = settings.getBool(DBSettings.SIS_FREEREDUCEDENABLED);
        if (sisSettings.blIsFreeReducedEnabled) {
            debug("Free and Reduced Subsystem is enabled.");
            // process org.pscafepos.settings that relate to free and reduced subsystem
            debug("Getting field names for free and reduced information.");
            sisSettings.freeField = settings.get(DBSettings.SIS_FREEFIELD);
            sisSettings.reducedField = settings.get(DBSettings.SIS_REDUCEDFIELD);

            if (isNotEmpty(sisSettings.freeField) && isNotEmpty(sisSettings.reducedField)) {
                debug("  ++ Free = " + sisSettings.freeField);
                debug("  ++ Reduced = " + sisSettings.reducedField);

                debug("Getting field types for free and reduced information.");
                sisSettings.blTypeFreeIsString = !settings.get(DBSettings.SIS_FREETYPE).equalsIgnoreCase("number");
                sisSettings.blTypeReducedIsString = !settings.get(DBSettings.SIS_REDUCEDTYPE).equalsIgnoreCase("number");

                debug("  ++ Free fieldtype = " + (sisSettings.blTypeFreeIsString ? "String" : "Number"));
                debug("  ++ Reduced fieldtype = " + (sisSettings.blTypeReducedIsString ? "String" : "Number"));

                debug("Getting values to flag fields as either free or reduced.");
                sisSettings.strValueFree = settings.get(DBSettings.SIS_FREEVALUE);
                sisSettings.strValueReduced = settings.get(DBSettings.SIS_REDUCEDVALUE);
                if (isNotEmpty(sisSettings.strValueFree) && isNotEmpty(sisSettings.strValueReduced)) {
                    debug("  ++ Free Value = " + sisSettings.strValueFree);
                    debug("  ++ Reduced Value = " + sisSettings.strValueReduced);
                } else {
                    debug("  -- Missing value: One of the free or reduced Value Flags is missing or blank.");
                }
            } else {
                debug("  -- Missing value: One of the free or reduced field names is missing or blank.");
            }
        } else {
            debug("Free and Reduced Subsystem is disabled.");
        }
    }

    private void initStudentIDField(DBSettings settings, SisSettings sisSettings) throws ConfigurationException {
        sisSettings.studentIDField = settings.get(DBSettings.SIS_STUDENTIDFIELD);
        if (isEmpty(sisSettings.studentIDField)) {
            throw new ConfigurationException("Student ID field is not set, this is the field that holds the studentid number in the registration table.");
        }
        debug("Student ID Field: " + sisSettings.studentIDField);
        String strTypeStudentID = settings.get(DBSettings.SIS_STUDENTIDTYPE);
        sisSettings.blStudentIDIsString = !strTypeStudentID.equalsIgnoreCase("number");
        debug("Student ID Field is a " + (sisSettings.blStudentIDIsString ? "string" : "number"));
    }

    private void initAllowOnlyStudentsThatExist(DBSettings settings, SisSettings sisSettings) {
        sisSettings.allowOnlyStudentsThatExist = settings.getBool(DBSettings.SIS_ALLOWONLYSTUDENTSTHATEXIST);
        if (sisSettings.allowOnlyStudentsThatExist) {
            debug("Allowing only students that exist in the database, but they might not be active.");
        } else {
            debug("Not restricting processing to students that exist.");
        }
    }

    private void initAllowSpacesInID(DBSettings settings, SisSettings sisSettings) {
        sisSettings.allowSpaces = settings.getBool(DBSettings.SIS_ALLOWSPACES);
        if (sisSettings.allowSpaces) {
            debug("Student ID's may have spaces.");
        } else {
            debug("Student ID's may not have spaces.");
        }
    }

    private void initImagePath(DBSettings settings, SisSettings sisSettings) {
        sisSettings.imagePath = settings.get(DBSettings.SIS_IMAGEPATH);
        if (sisSettings.imagePath != null && sisSettings.imagePath.contains("{studentid}")) {
            debug("Image path set to :" + sisSettings.imagePath);
        } else {
            debug("Image path does not contain the substring : {studentid} so it will not be used.");
            sisSettings.imagePath = null;
        }
    }

    private void initFixedIDWidth(DBSettings settings, SisSettings sisSettings) {
        sisSettings.fixedStudentIDWidth = settings.getBool(DBSettings.SIS_NUMBERFIXEDWIDTH);
        if (sisSettings.fixedStudentIDWidth) {
            try {
                sisSettings.studentIDValueLength = Integer.parseInt(settings.get(DBSettings.SIS_FIXEDWIDTHSIZE));
                if (sisSettings.studentIDValueLength > 0)
                    debug("Student ID is set to a fixed width of " + sisSettings.studentIDValueLength);
                else {
                    sisSettings.fixedStudentIDWidth = false;
                    debug("Invalid value for Student ID Width:: not requiring fixed width.");
                }
            }
            catch (NumberFormatException exp) {
                sisSettings.fixedStudentIDWidth = false;
            }
        } else {
            debug("Student ID is not a fixed width.");
        }
    }

    private void debug(String str) {
        if (debugMode){
            System.out.println("[" + (messageID++) + "] SIS Debug: " + str);
        }
    }
}
