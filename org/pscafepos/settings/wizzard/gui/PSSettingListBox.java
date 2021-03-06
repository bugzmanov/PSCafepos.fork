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

import javax.swing.JComboBox;

public class PSSettingListBox extends JComboBox implements PSSettingInputField {
    private int intID;

    public PSSettingListBox() {
        super();
    }

    public String getValue() {
        return this.getSelectedItem().toString();
    }

    public void setSettingID(int id) {
        intID = id;
    }

    public int getSettingID() {
        return intID;
    }

    public boolean isValid() {
        return true;
    }

}