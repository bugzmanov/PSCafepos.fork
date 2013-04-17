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
package org.pscafepos.drawer;
import jpos.*;
import jpos.events.*;
import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSEvent;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Drawer_APGT554 implements PSCashDrawer, StatusUpdateListener {
    private static final Logger logger = Logger.getLogger(Drawer_APGT554.class.getName());
    private String name;
    private CashDrawer cd;
    private POSEventListener posEventListener;

    public Drawer_APGT554() {
        cd = new CashDrawer();
        cd.addStatusUpdateListener(this);
    }

    public boolean prepareDrawer() {
        try {
            cd.open(name);
            cd.claim(1000);
            cd.setDeviceEnabled(true);
            return true;
        }
        catch (JposException e) {
            logger.log(Level.SEVERE, "Couldn't prepare cash drawer", e);
            return false;
        }
    }

    public void setDrawerName(String n) {
        name = n;
    }

    public String getDrawerName() {
        return name;
    }

    public boolean isDrawerReady() {
        switch (cd.getState()) {
            case JposConst.JPOS_S_CLOSED:
                return false;

            case JposConst.JPOS_S_IDLE:
                return true;

            case JposConst.JPOS_S_BUSY:
                return false;

            case JposConst.JPOS_S_ERROR:
                return false;

        }
        return false;
    }

    public boolean openDrawer() {
        try {
            cd.openDrawer();

            return cd.getDrawerOpened();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open drawer", e);
            return false;
        }
    }

    public boolean isDrawerClosed() {
        return true;
    }

    public void addPOSEventListener(POSEventListener listener) {
        posEventListener = listener;
    }

    private void firePOSEvent(int eventType) {
        POSEvent posEvent = new POSEvent(this, eventType);

        if (posEventListener != null)
            posEventListener.onPosEvent(posEvent);
    }

    public void statusUpdateOccurred(StatusUpdateEvent sue) {
        switch (sue.getStatus()) {
            case CashDrawerConst.CASH_SUE_DRAWEROPEN:
                firePOSEvent(POSEvent.DRAWER_OPENED);
                break;
            case CashDrawerConst.CASH_SUE_DRAWERCLOSED:
                firePOSEvent(POSEvent.DRAWER_CLOSED);
                break;
        }
    }

}