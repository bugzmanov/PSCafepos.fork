package org.pscafepos.gui;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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
import java.io.File;
import java.net.URI;

public class ImagePackage {
    public static final int IMG_BACKGROUND = 0;

    private String images[];

    public ImagePackage() {
        images = new String[1];
    }

    public void setImage(int img_type, String image) {
        if (img_type >= 0 && img_type < images.length)
            images[img_type] = image;
    }

    public Image getImage(int img_type) {
        try {
            if (imageExists(img_type)) {
                return new ImageIcon(images[img_type]).getImage();
            }
        } catch (Exception e) {

            return null;
        }
        return null;
    }

    public boolean imageExists(int img_type) {
        try {
            if (img_type >= 0 && img_type < images.length && images[img_type] != null) {
                File im = new File(images[img_type]);
                return (im.exists() && im.canRead() && im.isFile());
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public URI getImageURI(int img_type) {
        try {
            if (img_type >= 0 && img_type < images.length && images[img_type] != null) {

                return new URI(images[img_type]);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}