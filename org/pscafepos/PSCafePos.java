package org.pscafepos;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.configuration.ConfigurationException;
import org.pscafepos.configuration.ConfigurationReader;
import org.pscafepos.configuration.SessionSettings;
import org.pscafepos.configuration.Constants;
import org.pscafepos.settings.DBSettingsWriter;
import org.pscafepos.settings.DBSettings;

import java.io.File;

import org.pscafepos.backends.pos.PointOfSaleSystemImpl;
import org.pscafepos.backends.sis.StudentInformationSystem;
import org.pscafepos.backends.sis.StudentInformationSystemImpl;
import org.pscafepos.backends.sis.SisException;
import org.pscafepos.service.auth.AuthenticateService;
import org.pscafepos.service.auth.AuthenticateServiceImpl;
import org.pscafepos.backends.domain.sis.StudentInformationDAO;
import org.pscafepos.backends.domain.sis.SchoolOfficerDAO;
import org.pscafepos.model.Student;
import org.pscafepos.service.offline.DaoFactory;
import org.pscafepos.service.database.LocalDatabaseManager;
import org.pscafepos.gui.POSGUI;
import org.pscafepos.POSController;

public class PSCafePos {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println(" Missing Settings File Argument!");
            System.exit(-1);
        }
        File settingsFile = new File(args[0]);
        if (!settingsFile.isFile()) {
            System.out.println("Settings file " + settingsFile.getPath() + " is not found! Please run configuration wizzard first");
            System.exit(-1);
        }
        DBSettings settings = loadSettings(settingsFile);
        ConfigurationReader reader = new ConfigurationReader();
        if (settings.get(DBSettings.POS_SSLENABLED).equals("1")) {
            initSSL(settings);
        }
        try {
            SessionSettings sessionSettings = reader.read(settings);
            if (!new File(Constants.LOCALDB_name).exists()) {
                System.out.println("Creating local database. Please wait...");
                if (LocalDatabaseManager.create(settings.get(DBSettings.POS_TABLEPREFIX))) {
                    System.out.println("Local database created");
                }
            }
            POSGUI theGUI = new POSGUI();
            AuthenticateService authService = new AuthenticateServiceImpl(sessionSettings);
            final DaoFactory daoFactory = new DaoFactory();
            PointOfSaleSystemImpl saleSystem = new PointOfSaleSystemImpl(authService, daoFactory);
            StudentInformationDAO studentInformationDAO = createSISDao(daoFactory);
            StudentInformationSystem studentSystem = new StudentInformationSystemImpl(sessionSettings.getSisSettings(),
                    studentInformationDAO, createOfficerSISDao(daoFactory));
            POSController cont = new POSController(theGUI, sessionSettings, saleSystem, studentSystem);

        } catch (ConfigurationException e) {
            System.out.println("Configuration error: " + e.getMessage());
            System.out.println("System Halted, please set debug to 1 in the settings file to debug the problem.");
        }
    }


    private static DBSettings loadSettings(File settingsFile) {
        DBSettingsWriter writ = new DBSettingsWriter();
        DBSettings settings = null;
        try {
            settings = (DBSettings) writ.loadSettingsDB(settingsFile);
        } catch (Exception e) {
            System.out.print("Error: ");
            e.printStackTrace();
            System.out.println("System Halted, please set debug to 1 in the settings file to debug the problem.");
            System.exit(-1);
        }
        return settings;
    }

    //todo: this ugly hack assumes that daoFactory.init() will be called before dao.getStudent..not cool
    private static StudentInformationDAO createSISDao(final DaoFactory daoFactory) {
        StudentInformationDAO studentInformationDAO = new StudentInformationDAO() {
            private final Object daoSync = new Object();
            private StudentInformationDAO dao;

            public Student getStudent(String studentId) throws SisException {
                synchronized (daoSync) {
                    if (dao == null) {
                        dao = daoFactory.createStudentInformationDAO();
                    }
                }
                return dao.getStudent(studentId);
            }
        };
        return studentInformationDAO;
    }

    //todo: this ugly hack assumes that daoFactory.init() will be called before dao.getStudent..not cool
    private static SchoolOfficerDAO createOfficerSISDao(final DaoFactory daoFactory) {
        SchoolOfficerDAO studentInformationDAO = new SchoolOfficerDAO() {
            private final Object daoSync = new Object();
            private SchoolOfficerDAO dao;

            public Student loadOfficer(String id) {
                synchronized (daoSync) {
                    if (dao == null) {
                        dao = daoFactory.createScholOfficerDAO();
                    }
                }
                return dao.loadOfficer(id);
            }
        };
        return studentInformationDAO;
    }

    private static void initSSL(DBSettings settings) {
        System.setProperty("javax.net.ssl.keyStorePassword", "pscafe");
        System.setProperty("javax.net.ssl.trustStore", settings.get(DBSettings.POS_SSLTRUSTSTORE));
        System.setProperty("javax.net.ssl.trustStorePassword", "pscafe");
    }
} 
