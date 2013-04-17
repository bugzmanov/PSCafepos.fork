package org.pscafepos.drawer;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.event.POSEventListener;

import java.lang.Class;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CashDrawerManager {
    private static final Logger logger = Logger.getLogger(CashDrawerManager.class.getName());

    public static PSCashDrawer getDrawer(String cPath) {
        try {
            Class c = Class.forName(cPath);
            Object o = c.newInstance();
            return (PSCashDrawer) o;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Couldn't create PSCasheDrawer instance [type = " + cPath + "]", e);
            return null;
        }
    }

    public static PSCashDrawer getDrawer(String name, String clazz, POSEventListener listener) {
        PSCashDrawer drawer = getDrawer(clazz);
        if (drawer != null) {
            drawer.setDrawerName(name);
            drawer.addPOSEventListener(listener);
            if(!drawer.prepareDrawer()) {
                return null;
            }
        }
        return drawer;
    }
}