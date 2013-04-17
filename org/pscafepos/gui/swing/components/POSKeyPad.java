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
package org.pscafepos.gui.swing.components;

import org.pscafepos.gui.swing.components.POSButton;

import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.math.BigDecimal;

public class POSKeyPad extends JPanel implements ActionListener {
    public static final int MONEYMODE = 0;
    public static final int NUMBERMODE = 1;

    private static final int KEY_0 = 0;
    private static final int KEY_1 = 1;
    private static final int KEY_2 = 2;
    private static final int KEY_3 = 3;
    private static final int KEY_4 = 4;
    private static final int KEY_5 = 5;
    private static final int KEY_6 = 6;
    private static final int KEY_7 = 7;
    private static final int KEY_8 = 8;
    private static final int KEY_9 = 9;
    private static final int KEY_CLR = 10;
    private static final int KEY_BS = 11;
    private static final int KEY_M1 = 12;
    private static final int KEY_M5 = 13;
    private static final int KEY_M10 = 14;
    private static final int KEY_M20 = 15;
    private static final int KEY_M50 = 16;
    private static final int KEY_M100 = 17;

    private POSButton prototype;
    private int intMode;
    private StringBuffer strTotal;
    private JLabel lblTotal;
    private static final NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance();

    public POSKeyPad(POSButton prototype, int mode) {
        this.prototype = prototype;
        intMode = mode;
        if (intMode == MONEYMODE)
            strTotal = new StringBuffer("0");
        else
            strTotal = new StringBuffer("");
        buildKeyPad();
        setOpaque(false);
        setVisible(true);
    }

    private void buildKeyPad() {
        POSButton butSeq[];
        if (intMode == MONEYMODE) {
            butSeq = new POSButton[18];
            lblTotal = new JLabel("$0.00", SwingConstants.CENTER);
            strTotal = new StringBuffer("0");
        } else {
            lblTotal = new JLabel(" ", SwingConstants.CENTER);
            butSeq = new POSButton[12];
            strTotal = new StringBuffer("");
        }

        lblTotal.setBackground(Color.WHITE);
        lblTotal.setOpaque(true);
        lblTotal.setBorder(BorderFactory.createLineBorder(Color.BLACK));


        butSeq[0] = createButton();
        butSeq[0].setText("7");
        butSeq[0].setObjectRef(KEY_7);
        butSeq[0].addActionListener(this);

        butSeq[1] = createButton();
        butSeq[1].setText("8");
        butSeq[1].setObjectRef(KEY_8);
        butSeq[1].addActionListener(this);

        butSeq[2] = createButton();
        butSeq[2].setText("9");
        butSeq[2].setObjectRef(KEY_9);
        butSeq[2].addActionListener(this);

        butSeq[3] = createButton();
        butSeq[3].setText("4");
        butSeq[3].setObjectRef(KEY_4);
        butSeq[3].addActionListener(this);

        butSeq[4] = createButton();
        butSeq[4].setText("5");
        butSeq[4].setObjectRef(KEY_5);
        butSeq[4].addActionListener(this);

        butSeq[5] = createButton();
        butSeq[5].setText("6");
        butSeq[5].setObjectRef(KEY_6);
        butSeq[5].addActionListener(this);

        butSeq[6] = createButton();
        butSeq[6].setText("1");
        butSeq[6].setObjectRef(KEY_1);
        butSeq[6].addActionListener(this);

        butSeq[7] = createButton();
        butSeq[7].setText("2");
        butSeq[7].setObjectRef(KEY_2);
        butSeq[7].addActionListener(this);

        butSeq[8] = createButton();
        butSeq[8].setText("3");
        butSeq[8].setObjectRef(KEY_3);
        butSeq[8].addActionListener(this);

        butSeq[9] = createButton();
        butSeq[9].setText("BKSP");
        butSeq[9].setObjectRef(KEY_BS);
        butSeq[9].addActionListener(this);

        butSeq[10] = createButton();
        butSeq[10].setText("0");
        butSeq[10].setObjectRef(KEY_0);
        butSeq[10].addActionListener(this);

        butSeq[11] = createButton();
        butSeq[11].setText("CLR");
        butSeq[11].setObjectRef(KEY_CLR);
        butSeq[11].addActionListener(this);


        if (intMode == MONEYMODE) {
            butSeq[12] = createButton();
            butSeq[12].setText("+ $1");
            butSeq[12].setObjectRef(KEY_M1);
            butSeq[12].addActionListener(this);

            butSeq[13] = createButton();
            butSeq[13].setText("+ $5");
            butSeq[13].setObjectRef(KEY_M5);
            butSeq[13].addActionListener(this);

            butSeq[14] = createButton();
            butSeq[14].setText("+ $10");
            butSeq[14].setObjectRef(KEY_M10);
            butSeq[14].addActionListener(this);

            butSeq[15] = createButton();
            butSeq[15].setText("+ $20");
            butSeq[15].setObjectRef(KEY_M20);
            butSeq[15].addActionListener(this);

            butSeq[16] = createButton();
            butSeq[16].setText("+ $50");
            butSeq[16].setObjectRef(KEY_M50);
            butSeq[16].addActionListener(this);

            butSeq[17] = createButton();
            butSeq[17].setText("+ $100");
            butSeq[17].setObjectRef(KEY_M100);
            butSeq[17].addActionListener(this);
        }
        JPanel gridPanel = new JPanel(new GridLayout(butSeq.length % 3, 3, 7, 7));
        gridPanel.setOpaque(false);

        for (POSButton aButSeq : butSeq) {
            gridPanel.add(aButSeq);
        }

        setLayout(new BorderLayout());
        add(lblTotal, BorderLayout.NORTH);

        JToggleButton staffButton = new JToggleButton();
        staffButton.setPreferredSize(prototype.getPreferredSize());
        staffButton.setFont(prototype.getFont());
        staffButton.setText("Staff");
        staffButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (strTotal.indexOf("a") != 0) {
                    strTotal.insert(0, "a");
                } else {
                    strTotal.deleteCharAt(0);
                }
                updateTotal();
            }
        });
        if (intMode == NUMBERMODE) {

            JPanel buttonPannel = new JPanel(new BorderLayout());
            buttonPannel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
            buttonPannel.add(staffButton, BorderLayout.SOUTH);
            add(buttonPannel, BorderLayout.SOUTH);
        }
        add(gridPanel);

    }

    //TODO: change name, get rid of division by 100
    public BigDecimal getBigDecimalValue() {
        if (intMode == MONEYMODE) {
            try {
                if (strTotal.length() > 0)
                    return (new BigDecimal(strTotal.toString()).divide(new BigDecimal("100")));
            } catch (Exception e) {
                System.err.println(e);
            }

        }
        return BigDecimal.ZERO;

    }

    public int getValueInt() {
        if (intMode == NUMBERMODE) {
            try {
                if (strTotal.length() > 0)
                    return (Integer.parseInt(strTotal.toString()));
            } catch (Exception e) {
                System.err.println(e);
            }

        }
        return 0;
    }

    public String getValueString() {
        if (strTotal != null && strTotal.length() > 0)
            return strTotal.toString();

        return "";
    }

    public void reset() {
        if (intMode == MONEYMODE)
            strTotal = new StringBuffer("0");
        else {
            if (strTotal.indexOf("a") == 0) {
                strTotal = new StringBuffer("a");
            } else {
                strTotal = new StringBuffer("");
            }
        }
        updateTotal();
    }

    private void updateTotal() {
        if (intMode == MONEYMODE) {
            if (strTotal != null && strTotal.length() > 0) {
                lblTotal.setText(MONEY_FORMAT.format(Double.parseDouble(strTotal.toString()) / 100));
            } else {
                lblTotal.setText(MONEY_FORMAT.format(0d));
            }
        } else {
            if (strTotal != null && strTotal.length() > 0)
                lblTotal.setText(strTotal.toString());
            else
                lblTotal.setText(" ");
        }

        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        POSButton b = (POSButton) e.getSource();
        switch ((Integer) b.getObjectRef()) {
            case KEY_0:
                strTotal.append(KEY_0 + "");
                break;
            case KEY_1:
                strTotal.append(KEY_1 + "");
                break;
            case KEY_2:
                strTotal.append(KEY_2 + "");
                break;
            case KEY_3:
                strTotal.append(KEY_3 + "");
                break;
            case KEY_4:
                strTotal.append(KEY_4 + "");
                break;
            case KEY_5:
                strTotal.append(KEY_5 + "");
                break;
            case KEY_6:
                strTotal.append(KEY_6 + "");
                break;
            case KEY_7:
                strTotal.append(KEY_7 + "");
                break;
            case KEY_8:
                strTotal.append(KEY_8 + "");
                break;
            case KEY_9:
                strTotal.append(KEY_9 + "");
                break;
            case KEY_CLR:
                reset();
                break;
            case KEY_BS:
                if (strTotal.length() > 0 && strTotal.charAt(strTotal.length() - 1) != 'a')
                    strTotal.deleteCharAt(strTotal.length() - 1);
                break;
            case KEY_M1:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 100) + "");
                break;
            case KEY_M5:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 500) + "");
                break;
            case KEY_M10:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 1000) + "");
                break;
            case KEY_M20:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 2000) + "");
                break;
            case KEY_M50:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 5000) + "");
                break;
            case KEY_M100:
                strTotal = new StringBuffer((Double.parseDouble(strTotal.toString()) + 10000) + "");
                break;
        }
        updateTotal();
    }

    private POSButton createButton() {
        POSButton b = new POSButton("", 1, 1);
        b.setPreferredSize(prototype.getPreferredSize());
        b.setFont(prototype.getFont());
        return b;
    }

}