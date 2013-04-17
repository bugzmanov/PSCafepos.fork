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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class ImageManager {
    private String strFile;
    private URL urlFile;

    public ImageManager() {

    }

    public ImageManager(String fn) {
        setImage(fn);
    }

    public boolean isURL() {
        return urlFile != null;
    }

    public boolean isFile() {
        return strFile != null;
    }

    public boolean setImage(String strFileName) {
        if (strFileName != null) {
            try {
                if (strFileName.indexOf("http:/") >= 0) {
                    // url
                    urlFile = new URL(strFileName);
                    strFile = null;
                    return true;
                } else {
                    File flTest = new File(strFileName);
                    if (flTest.canRead() && flTest.isFile() && flTest.exists()) {
                        strFile = strFileName;
                        urlFile = null;
                        return true;
                    }
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public Image getImage() {
        if (isFile()) {
            return new ImageIcon(strFile).getImage();
        } else if (isURL()) {
            return new ImageIcon(urlFile).getImage();
        } else {
            return null;
        }
    }

    public Image scaleToStaticWidth(int width) {
        if (width > 0 && getStatus()) {
            int curX, curY, newHeight;
            curX = getImageWidth();
            curY = getImageHeight();
            newHeight = (width * curY) / curX;
            return resizeImage(width, newHeight);
        }
        return null;
    }

    public Image scaleToStaticHeight(int height) {
        if (height > 0 && getStatus()) {
            int curX, curY, newWidth;
            curX = getImageWidth();
            curY = getImageHeight();
            newWidth = (height * curX) / curY;
            return resizeImage(newWidth, height);
        }
        return null;
    }

    public Image resizeImage(int intX, int intY) {
        if (isFile()) {
            if (intX >= 0 && intY >= 0) {
                File flTmp = new File(strFile);
                if (flTmp.canRead() && flTmp.isFile() && flTmp.exists()) {
                    Image imgOrig;                                                                                                            // Image object that will hold the original image
                    Image imgMain;
                    imgOrig = new ImageIcon(strFile).getImage();
                    imgMain = imgOrig.getScaledInstance(intX, intY, 0);
                    return new ImageIcon(imgMain).getImage();
                }
            }
            return null;
        } else if (isURL()) {
            if (intX >= 0 && intY >= 0) {
                Image imgOrig;                                                                                                            // Image object that will hold the original image
                Image imgMain;
                imgOrig = new ImageIcon(urlFile).getImage();
                imgMain = imgOrig.getScaledInstance(intX, intY, 0);
                return new ImageIcon(imgMain).getImage();
            }
        }
        return null;
    }

    private boolean getStatus() {
        if (isFile()) {
            File tmp = new File(strFile);
            if (tmp.canRead() && tmp.exists() && tmp.isFile()) {
                return true;
            }
        } else if (isURL()) {
            return true;
        }
        return false;
    }

    public int getImageWidth() {
        if (getStatus()) {
            if (isFile()) {
                return new ImageIcon(strFile).getIconWidth();
            } else if (isURL()) {
                return new ImageIcon(urlFile).getIconWidth();
            }
        }
        return 0;
    }

    public int getImageHeight() {
        if (getStatus()) {
            if (isFile()) {
                return new ImageIcon(strFile).getIconHeight();
            } else if (isURL()) {
                return new ImageIcon(urlFile).getIconHeight();
            }
        }
        return 0;
    }

}
