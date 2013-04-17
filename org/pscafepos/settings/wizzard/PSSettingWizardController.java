package org.pscafepos.settings.wizzard;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.settings.DBSettings;
import org.pscafepos.settings.DBSettingsWriter;
import org.pscafepos.settings.wizzard.gui.PSSettingGUI;
import org.pscafepos.settings.wizzard.gui.PSSettingInputField;
import org.pscafepos.settings.wizzard.gui.events.SettingEvent;
import org.pscafepos.settings.wizzard.gui.events.SettingEventListener;
import org.pscafepos.service.database.LocalDatabaseManager;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;

public class PSSettingWizardController implements SettingEventListener {
    private static final int[] purgeLocalDBFields = {DBSettings.POS_DATABASE, DBSettings.POS_HOSTNAME,
            DBSettings.POS_JDBCDBTYPE, DBSettings.POS_TABLEPREFIX,
            DBSettings.POS_JDBCDRIVER, DBSettings.POS_JDBCPORT, DBSettings.SIS_JDBCCONNECTIONSTRING};

    private static final Logger logger = Logger.getLogger(PSSettingWizardController.class.getName());
    private PSSettingGUI g;
    private DBSettings settings;
    private String strPath;
    private DBSettings settingsBeforeChanges = null;

    public PSSettingWizardController(PSSettingGUI gui, DBSettings dbset, String path) {
        strPath = path;
        settings = dbset;
        g = gui;
        if (g != null) {
            g.addSettingEventListener(this);
            g.loadGUI();
            File f = new File(path);
            if (f.exists() && f.canWrite()) {
                DBSettingsWriter writ = new DBSettingsWriter();
                try {
                    DBSettings settings = (DBSettings) writ.loadSettingsDB(f);
                } catch (Exception e) {
//                    logger.log(Level.SEVERE, e.getMessage(), e);
                    settings = new DBSettings();
                }
                try {
                    settingsBeforeChanges = (DBSettings) writ.loadSettingsDB(f);
                } catch (Exception e) {
                    dbset = new DBSettings();
                }

                g.pushData(settings);

            }
        }
    }

    private void processSettings(Vector vct[]) {
        if (vct != null) {
            if (settings == null)
                settings = new DBSettings();

            for (Vector v : vct) {
                while (!v.isEmpty()) {
                    PSSettingInputField in = ((PSSettingInputField) v.get(0));
                    settings.set(in.getSettingID(), in.getValue());
                    v.remove(0);
                }
            }
            try {
                File out = new File(strPath);
                if (!out.exists()) {
                    out.createNewFile();
                }

                if (out.canWrite()) {
                    DBSettingsWriter writ = new DBSettingsWriter();
                    if (writ.writeFile(settings, new File(strPath))) {
                        for (int purgeLocalDBField : purgeLocalDBFields) {
                            if (settingsBeforeChanges.get(purgeLocalDBField) != null && !settingsBeforeChanges.get(purgeLocalDBField).equalsIgnoreCase(settings.get(purgeLocalDBField))) {
                                LocalDatabaseManager.dump();
                                System.out.println("Because of SIS or POS access changes, local DB copy was dumped and will be recreated.");
                                break;
                            }
                        }

                        g.popUpMessage("Settings Saved!\n You can now close this wizard and launch PSCafePOS.");
                    } else {
                        g.popUpMessage("Failed to write settings.");
                    }
                } else {
                    g.popUpMessage("Can not write to destination file.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void settingEventOccurred(SettingEvent event) {
        switch (event.getEventType()) {
            case SettingEvent.NEXT:
                g.nextScreen();
                break;
            case SettingEvent.BACK:
                g.lastScreen();
                break;
            case SettingEvent.FINISH:
                if (event.getSource() != null) {
                    try {
                        Vector[] v = (Vector[]) event.getSource();
                        if (v != null)
                            processSettings(v);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                break;
        }
    }
}