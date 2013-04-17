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
package org.pscafepos.backends.sis.util;
import org.pscafepos.event.POSEvent;
import org.pscafepos.event.POSEventListener;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ThreadedImageManager extends Thread {

    private static final Logger logger = Logger.getLogger(ThreadedImageManager.class.getName());

    private POSEventListener posEventListener;
    private ImageIcon imageIcon;
    private String strPath;
    private int imageWidth;

    public ThreadedImageManager(String path, int width) {
        strPath = path;
        imageWidth = width;
    }

    public void run() {
        try {
            ImageManager imMan = new ImageManager(strPath);
            //todo: wtf is this?
            Image t = imMan.scaleToStaticHeight(imageWidth);
            if (t != null) {
                imageIcon = new ImageIcon(t);
                firePOSEvent(POSEvent.IMAGE_LOADED);
            }
        }
        catch (Exception e) {
            firePOSEvent(POSEvent.IMAGE_LOAD_FAILED);
            logger.log(Level.WARNING, "sis.util.ThreadedImageManager: " + e.getMessage(), e);
        }
    }

    private void firePOSEvent(int eventType) {
        if (posEventListener != null) {
            POSEvent posEvent = new POSEvent(imageIcon, eventType);
            posEventListener.onPosEvent(posEvent);
        }

    }

    public void setPOSEventListener(POSEventListener listener) {
        posEventListener = listener;
    }

}