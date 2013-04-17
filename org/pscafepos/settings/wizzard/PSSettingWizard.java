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
package org.pscafepos.settings.wizzard;

import org.pscafepos.settings.DBSettings;
import org.pscafepos.settings.DBSettingsWriter;
import org.pscafepos.settings.wizzard.gui.PSSettingWizardGUI;
import org.pscafepos.service.database.LocalDatabaseManager;

import java.io.*;

public class PSSettingWizard {
    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            boolean ok = true;
            DBSettings dbset = null;
            try {
                System.out.println("\n\nSystem Started ");
                File settings = new File(args[0]);
                if (settings.canWrite() && settings.canRead()) {
                    DBSettingsWriter writ = new DBSettingsWriter();
                    try {
                        dbset = (DBSettings) writ.loadSettingsDB(settings);
                    } catch (Exception e) {
                        dbset = new DBSettings();
                    }
                }

                if (dbset == null) {
                    System.out.println("   + Generating new database file.");
                    dbset = new DBSettings();
                }
            } catch (SecurityException sex) {
                System.err.println("  ***** Security Exception ***** ");
                System.err.println(sex.getMessage());
                ok = false;
            } catch (Exception ex) {
                System.err.println("  ***** Exception ***** ");
                System.err.println(ex.getMessage());
                ok = false;
            }

            if (ok) {
                if (dbset != null) {
                    System.out.println("\n\nLoading Wizard, please wait...");
                    System.out.print("   + Creating GUI...");
                    PSSettingWizardGUI theGUI = new PSSettingWizardGUI();
                    System.out.print("Done\n   + Creating Controller...");
                    PSSettingWizardController cont = new PSSettingWizardController(theGUI, dbset, args[0]);

                    System.out.print("Done\n");

                } else {
                    System.err.println(" - Setttings object is null, unable to proceed.");
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.err.println("\n  *****Error*****\n  Missing argument!\n  Usage:  java org.pscafepos.settings.wizzard.PSSettingWizard settingfile\n");
            System.exit(1);
        }
    }
}