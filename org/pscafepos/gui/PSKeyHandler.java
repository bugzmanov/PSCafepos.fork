/*   PSCafePOS is an Open Source Point of Sale System for Schools
 *   Copyright (C) 2007 Charles Syperski
 *
 *   This program is free software; you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation; either version 2 of the License, or 
 *   any later version.
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
package org.pscafepos.gui;

import org.pscafepos.event.POSStudentListener;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PSKeyHandler implements KeyEventDispatcher {
    private static final Logger logger = Logger.getLogger(PSKeyHandler.class.getName());

    private POSStudentListener obj;
    private StringBuffer input;

    public PSKeyHandler(POSStudentListener o) {
        obj = o;
        input = new StringBuffer();
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        try {
            if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED && e.getKeyChar() != KeyEvent.VK_ENTER) {

                if (!e.isActionKey()) {
                    if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        if (input.length() > 0) {
                            input = input.deleteCharAt(input.length() - 1);
                        }
                    } else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        input = new StringBuffer();
                    } else {
                        input.append(e.getKeyChar());
                    }
                    return true;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED) {
                if (input.length() > 0) {
                    obj.processStudentScan(input.toString());
                    input = new StringBuffer();
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
        return false;
    }
}
