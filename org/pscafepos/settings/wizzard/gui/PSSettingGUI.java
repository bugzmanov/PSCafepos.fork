package org.pscafepos.settings.wizzard.gui;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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
import org.pscafepos.settings.wizzard.gui.events.SettingEventListener;

public interface PSSettingGUI {
    void loadGUI();

    void setMessage(String msg);

    void setStatus(String stat);

    void addSettingEventListener(SettingEventListener listener);

    void removeSettingEventListener(SettingEventListener listener);

    void loadScreen(int screenNum);

    void nextScreen();

    void lastScreen();

    void pushData(DBSettings settings);

    void regenerateScreens(DBSettings settings);

    void popUpMessage(String msg);
}