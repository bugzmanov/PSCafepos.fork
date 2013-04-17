package org.pscafepos.gui.swing.components;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import javax.swing.*;
import java.awt.*;

public class POSButton extends JButton {

    private static final Font fnt = new Font("Arial", Font.PLAIN, 10);
    private Object objectRef;

    public POSButton(String name, int x, int y) {
        super(name);
        Dimension d = new Dimension(x, y);
        setMaximumSize(d);
        setMinimumSize(d);
        setPreferredSize(d);
        setFont(fnt);
        objectRef = null;
    }


    public POSButton(String name, int x, int y, byte[] icon) {
        super(name);
        if (icon != null) {
            ImageIcon ic = new ImageIcon(icon, name);
            setIcon(ic);
            setVerticalTextPosition(AbstractButton.BOTTOM);
            setHorizontalTextPosition(AbstractButton.CENTER);
        }
        Dimension d = new Dimension(x, y + 26);
        setMaximumSize(d);
        setMinimumSize(d);

        setPreferredSize(d);
        setFont(fnt);
        objectRef = null;

    }


    public void setObjectRef(Object o) {
        objectRef = o;
    }

    public Object getObjectRef() {
        return objectRef;
    }

}