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
package org.pscafepos.settings;

import java.io.*;
import java.util.zip.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DBSettingsWriter {

    private static final Logger logger = Logger.getLogger(DBSettingsWriter.class.getName());

    public DBSettingsWriter() {
    }

    public boolean writeFile(Serializable s, File f) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
            out.writeObject(s);
            out.close();
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public Object loadSettingsDB(File f) throws ClassNotFoundException, IOException {
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
        return in.readObject();
    }
}