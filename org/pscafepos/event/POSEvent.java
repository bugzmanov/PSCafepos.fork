package org.pscafepos.event;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import java.util.*;

public class POSEvent extends EventObject {
    public static final int SYSTEM_EXIT = 0;
    public static final int SYSTEM_LOGIN = 1;

    public static final int NO_SALE = 2;

    public static final int ITEMS_LOADCAT = 3;
    public static final int ITEMS_LOADITEMS = 4;
    public static final int ITEMS_ADDITEM = 5;
    public static final int ITEMS_TOGGLECHECKOUT = 6;
    public static final int ITEMS_MANUALENTRY = 7;
    public static final int ITEMS_MANUALENTRYSUBMIT = 8;
    public static final int ITEMS_ACTIVATELASTORDER = 9;

    public static final int DRAWER_OPENED = 100;
    public static final int DRAWER_CLOSED = 101;

    public static final int ORDER_VOIDORDER = 200;
    public static final int ORDER_VOIDLAST = 201;
    public static final int ORDER_VOIDITEM = 202;

    public static final int STUDENT_RESET = 300;

    public static final int CHECKOUT_ADDCASH = 400;
    public static final int CHECKOUT_ADDCREDIT = 401;
    public static final int CHECKOUT_CLEARCASH = 402;
    public static final int CHECKOUT_CLEARCREDIT = 403;
    public static final int CHECKOUT_PROCESS = 404;
    public static final int CHECKOUT_SUMMARYSCREENCLOSED = 405;

    public static final int IMAGE_LOADED = 500;
    public static final int IMAGE_LOAD_FAILED = 501;

    private int eventType;
    private Object src;

    public POSEvent(Object source, int eventType) {
        super(source);
        src = source;
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public Object getSource() {
        return src;
    }
}