package org.pscafepos.settings;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import java.io.*;

public class DBSettings implements Serializable {
    private static final long serialVersionUID = 1;

    private static final int VERSION = 1;
    private static final int NUMOFSETTINGS = 200;

    public static final int MAIN_BUILDING = 0;
    public static final int MAIN_DEBUG = 1;
    public static final int MAIN_ALLOWANONTRANS = 2;
    public static final int MAIN_ALLOWNEGBALANCES = 44;
    public static final int MAIN_MAXNEGBALANCE = 45;
    public static final int MAIN_BUILDINGNAME = 3;
    public static final int MAIN_SELL_ON_CREDIT_ONLY = 52;
    public static final int MAIN_AUTO_CHECKOUT = 53;

    public static final int POS_JDBCDRIVER = 48;
    public static final int POS_JDBCDBTYPE = 49;
    public static final int POS_JDBCPORT = 50;
    public static final int POS_TABLEPREFIX = 51;
    public static final int POS_HOSTNAME = 4;
    public static final int POS_DATABASE = 5;
    public static final int POS_SSLTRUSTSTORE = 42;
    public static final int POS_SSLKEYSTORE = 43;
    public static final int POS_SSLENABLED = 47;
    public static final int POS_SSLDEBUG = 46;

    public static final int SIS_ENABLED = 6;
    public static final int SIS_SYSTEMNAME = 7;
    public static final int SIS_JDBCDRIVER = 8;
    public static final int SIS_JDBCCONNECTIONSTRING = 9;
    public static final int SIS_CONNUSERNAME = 10;
    public static final int SIS_CONNPASSWORD = 11;
    public static final int SIS_REGISTRATIONTABLE = 12;
    public static final int SIS_STUDENTIDFIELD = 13;
    public static final int SIS_STUDENTIDTYPE = 14;
    public static final int SIS_STUDENTFNAMEFIELD = 15;
    public static final int SIS_STUDENTLNAMEFIELD = 16;
    public static final int SIS_FREEFIELD = 17;
    //public static final int SIS_FREEFIELD = 18;
    public static final int SIS_FREEVALUE = 19;
    public static final int SIS_FREETYPE = 20;
    public static final int SIS_REDUCEDFIELD = 21;
    public static final int SIS_REDUCEDVALUE = 22;
    public static final int SIS_REDUCEDTYPE = 23;
    public static final int SIS_ALLOWONLYACTIVE = 24;
    public static final int SIS_STATUSFIELD = 25;
    //public static final int SIS_STATUSFIELD = 26;
    public static final int SIS_STATUSVALUE = 27;
    public static final int SIS_STATUSTYPE = 28;
    public static final int SIS_ALLOWONLYSTUDENTSTHATEXIST = 29;
    public static final int SIS_NUMBERFIXEDWIDTH = 30;
    public static final int SIS_FIXEDWIDTHSIZE = 31;
    public static final int SIS_IMAGEPATH = 32;
    public static final int SIS_ALLOWSPACES = 33;
    public static final int SIS_FREEREDUCEDENABLED = 34;
    public static final int SIS_HIDEFREEREDUCEDINFO = 35;

    public static final int HOTBAR_ENABLED = 36;

    public static final int DRAWER_ENABLED = 37;
    public static final int DRAWER_CLASS = 38;
    public static final int DRAWER_COMMONNAME = 39;

    public static final int UPDATEURL = 40;

    public static final int IMAGES_MAINBG = 41;


    private String[] settings;

    public DBSettings() {
        settings = new String[NUMOFSETTINGS];
        for (int i = 0; i < NUMOFSETTINGS; i++)
            settings[i] = "0";
    }

    public void set(int key, String value) {
        settings[key] = value;
    }

    public String get(int key) {
        if (key >= 0 && key < settings.length) {
            return settings[key];
        }
        return null;
    }

    public boolean getBool(int key) {
        String value = get(key);
        return value != null && value.equals("1");
    }

    //todo: get rid of this?
    public int getVersion() {
        return VERSION;
    }
}

