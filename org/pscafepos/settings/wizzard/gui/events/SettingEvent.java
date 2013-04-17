package org.pscafepos.settings.wizzard.gui.events;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

public class SettingEvent extends EventObject {
    public static final int NEXT = 0;
    public static final int BACK = 1;
    public static final int FINISH = 2;

    private int eventType;
    private Object src;

    public SettingEvent(Object source, int eventType) {
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