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

public class PSSplashScreen {
    private JWindow win;
    private Image bgImage;
    private JLabel lblMsg;

    public PSSplashScreen(String image) {
        win = new JWindow();
        win.setSize(400, 250);
        bgImage = new ImageIcon(image).getImage();
        JPanel pan = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.drawImage(bgImage, 0, 0, null);
                super.paintComponent(g);
            }
        };

        lblMsg = new JLabel();
        lblMsg.setText("Please Wait...");
        lblMsg.setOpaque(false);
        pan.add(lblMsg, BorderLayout.SOUTH);
        pan.repaint();
        win.getContentPane().add(pan, "Center");

        pan.setLayout(new BorderLayout());
        pan.setOpaque(false);


        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = win.getBounds();
        win.setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);


    }

    public void display() {
        win.setVisible(true);
        win.repaint();
    }

    public void kill() {
        win.setVisible(false);
        win.dispose();
    }
}