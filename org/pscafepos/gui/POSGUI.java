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

import org.pscafepos.backends.domain.items.OrderItem;
import org.pscafepos.backends.domain.items.ItemsPackage;
import org.pscafepos.event.POSEvent;
import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSStudentListener;
import org.pscafepos.model.Student;
import org.pscafepos.model.Order;
import org.pscafepos.model.MoneyBuffer;
import org.pscafepos.configuration.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Vector;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.pscafepos.gui.PSEntryPad;
import org.pscafepos.gui.swing.components.POSButton;
import org.pscafepos.gui.swing.components.POSKeyPad;
import org.pscafepos.gui.swing.components.OrderItemPanel;
import org.pscafepos.POSController;

public class POSGUI extends JFrame implements POSGUIInterface {

    private Vector posEventListenerList;

    private JLabel lblStatus;
    private JPanel pnlOrder;
    private JLabel cashBufferLabel, creditBufferLabel, totalPaymentBufferLabel, balanceLabel;
    private Box itemOrder;
    private JPanel pnlBackground;
    private JPanel pnlMain;
    private JPanel pnlNorth;
    private JPanel pnlLeft;
    private JPanel pnlStudentInfo;
    private JPanel pnlItems;
    private JPanel pnlLastOrder;
    private Box pnlImg;
    private JPanel pnlImgHolder;
    private Container root;
    private NumberFormat money;
    private int intMode;
    private POSButton toggleButton;
    private PSInfoScreen info;

    private JScrollPane scrollOrder;

    private Box pnlCats;
    private Box northBox;
    private Box hotBar;

    private JTextField txtUser;
    private JPasswordField txtPass;

    private static final Color pnlBGClr = new Color(182, 205, 235);

    private static final Font ARIAL = new Font("Arial", Font.PLAIN, 12);
    private static final Font ARIALBOLD = new Font("Arial", Font.BOLD, 13);

    private static final int BUTTONWIDTH = 90;
    private static final int BUTTONHEIGHT = 40;

    private static final String BMSG_CHECKOUT = "<html><b>Load<br>Checkout</b></html>";
    private static final String BMSG_RETURNITEMS = "<html><b>Load<br>Items</b></html>";
    private static final Logger logger = Logger.getLogger(POSGUI.class.getName());

    private Dimension orderItemIcoDimenstion;

    public void setOrderItemIcoDimenstion(Dimension orderItemIcoDimenstion) {
        this.orderItemIcoDimenstion = orderItemIcoDimenstion;
    }

    public POSGUI() {
        super("PS POS :: Cafe Register");
        posEventListenerList = new Vector();
        money = NumberFormat.getCurrencyInstance();
    }

    public void loadGUI(ImagePackage iPackage) {
        ImagePackage images = iPackage;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        root = getContentPane();
        setLayout(new BorderLayout());
        setUndecorated(true);

        setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());

        final Image bg = images.getImage(ImagePackage.IMG_BACKGROUND);

        if (bg != null) {
            pnlBackground = new JPanel(new BorderLayout()) {
                public void paintComponent(Graphics g) {
                    int x = 0, y = 0;

                    while (y < getHeight()) {
                        x = 0;
                        while (x < getWidth()) {
                            g.drawImage(bg, x, y, this);
                            x = x + bg.getWidth(null);
                        }
                        y = y + bg.getHeight(null);
                    }
                }
            };
        } else {
            pnlBackground = new JPanel(new BorderLayout());
        }

        add(pnlBackground);

        lblStatus = new JLabel(" Welcome to PS POS :: Please Sign in :: written by Chuck Syperski", SwingConstants.CENTER);
        lblStatus.setFont(ARIALBOLD);
        lblStatus.setOpaque(false);
        //lblStatus.setBorder( BorderFactory.createLineBorder(Color.BLACK, 1) );

        pnlBackground.add(lblStatus, BorderLayout.SOUTH);

        pnlMain = new JPanel(new BorderLayout());

        pnlBackground.add(pnlMain);
        pnlMain.setOpaque(false);


        setVisible(true);

        validate();
    }

    public void toggleButtonText() {
        if (toggleButton != null) {
            if (intMode == POSController.MODE_CHECKOUT)
                toggleButton.setText(BMSG_RETURNITEMS);
            else if (intMode == POSController.MODE_ITEMS)
                toggleButton.setText(BMSG_CHECKOUT);
        }
    }

    public void disableUI() {
        root.setVisible(false);
    }

    public void enableUI() {
        root.setVisible(true);
    }

    public void loadCheckOutInfo(Order order) {
        if (intMode == POSController.MODE_CHECKOUT) {
            if (pnlMain != null) {
                if (order != null && order.getItemsCount() > 0) {
                    Font large = new Font("Arial", Font.BOLD, 14);
                    JPanel pnlCheckOutOverview = new JPanel(new BorderLayout());
                    pnlCheckOutOverview.setOpaque(false);
                    Box oPanel = new Box(BoxLayout.Y_AXIS);
                    oPanel.setOpaque(false);

                    JLabel lblOverview = new JLabel("Order Overview", SwingConstants.CENTER);
                    oPanel.add(new JLabel("   Num. of Items:"));
                    JLabel lblNum = new JLabel("            " + order.getItemsCount() + "");
                    lblNum.setFont(large);
                    oPanel.add(lblNum);

                    oPanel.add(new JLabel("   Order Total:"));
                    JLabel lblT = new JLabel("            " + order.getOrderTotalString());
                    lblT.setFont(large);
                    lblT.setForeground(Color.GREEN.darker());
                    oPanel.add(lblT);

                    pnlCheckOutOverview.add(lblOverview, BorderLayout.NORTH);
                    pnlCheckOutOverview.add(oPanel);

                    pnlMain.add(pnlCheckOutOverview, BorderLayout.WEST);
                }
            }
        }
    }

    public void setStatus(String status, boolean blVis) {
        if (blVis) {
            if (info != null) {
                try {
                    info.kill();
                } catch (Exception e) {
                }
            }

            info = new PSInfoScreen(this);
            info.display("\n\r\n\r     " + status);
        } else
            lblStatus.setText(" " + status);
    }

    public void setStatus(String status) {
        setStatus(status, false);
    }

    public void setMode(int mode) {
        switch (mode) {
            case POSController.MODE_LOGIN:
                intMode = POSController.MODE_LOGIN;
                setStatus("Loading Login Screen...");
                loadLoginScreen();
                setStatus("Please Login");
                break;
            case POSController.MODE_ITEMS:
                intMode = POSController.MODE_ITEMS;
                setStatus("Loading Menu Items...");
                loadItemScreen();
                setStatus("Ready and waiting for user input");
                break;
            case POSController.MODE_CHECKOUT:
                intMode = POSController.MODE_CHECKOUT;
                setStatus("Loading Checkout information...");
                loadCheckOutScreen();
                setStatus("Read and waiting for user input");
                break;
            default:
                setStatus("Unknown Mode, ignoring");
                break;
        }
    }


    private void loadCheckOutScreen() {
        if (pnlLeft != null)
            pnlLeft.removeAll();

        if (pnlMain != null) {
            pnlMain.remove(pnlCats);
            pnlMain.remove(pnlItems);
        }

        JPanel pnlOrderLayout = new JPanel(new FlowLayout());
        pnlOrderLayout.setOpaque(false);

        final POSKeyPad pad = new POSKeyPad(new POSButton("", BUTTONWIDTH, BUTTONHEIGHT), POSKeyPad.MONEYMODE);
        pnlOrderLayout.add(pad);

        JPanel pnlBuffers = new JPanel(new FlowLayout());
        pnlBuffers.setOpaque(false);

        Box pnlBuffersSub = new Box(BoxLayout.Y_AXIS);
        pnlBuffersSub.setOpaque(false);

        Box line1 = new Box(BoxLayout.X_AXIS);
        line1.setOpaque(false);

        Box line2 = new Box(BoxLayout.X_AXIS);
        line2.setOpaque(false);
        ////
        POSButton butAddCash = new POSButton("Cash", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener addCashLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.CHECKOUT_ADDCASH, pad);
            }
        };
        butAddCash.addActionListener(addCashLis);

        cashBufferLabel = new JLabel("Cash:   $0.00", SwingConstants.CENTER);

        POSButton butClrCash = new POSButton("Clear", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener clrCashLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.CHECKOUT_CLEARCASH, pad);
            }
        };
        butClrCash.addActionListener(clrCashLis);

        line1.add(butAddCash);
        line1.add(Box.createHorizontalStrut(10));
        line1.add(cashBufferLabel);
        line1.add(Box.createHorizontalStrut(10));
        line1.add(butClrCash);
        ///
        POSButton butAddCredit = new POSButton("Credit", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener addCreditLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.CHECKOUT_ADDCREDIT, pad);
            }
        };
        butAddCredit.addActionListener(addCreditLis);

        creditBufferLabel = new JLabel("Credit:   $0.00", SwingConstants.CENTER);

        POSButton butClrCredit = new POSButton("Clear", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener clrCreditLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.CHECKOUT_CLEARCREDIT, pad);
            }
        };
        butClrCredit.addActionListener(clrCreditLis);

        line2.add(butAddCredit);
        line2.add(Box.createHorizontalStrut(10));
        line2.add(creditBufferLabel);
        line2.add(Box.createHorizontalStrut(10));
        line2.add(butClrCredit);


        pnlBuffersSub.add(line1);
        pnlBuffersSub.add(Box.createVerticalStrut(10));
        pnlBuffersSub.add(line2);
        totalPaymentBufferLabel = new JLabel("Buffer Total:   $0.00", SwingConstants.CENTER);
        balanceLabel = new JLabel("Balance:   $0.00", SwingConstants.CENTER);

        JPanel checkLabels = new JPanel(new GridLayout(2, 1, 5, 5));
        checkLabels.setOpaque(false);
        checkLabels.add(totalPaymentBufferLabel);
        checkLabels.add(balanceLabel);

        pnlBuffersSub.add(Box.createVerticalStrut(10));

        pnlBuffersSub.add(checkLabels);

        pnlBuffersSub.add(Box.createVerticalStrut(20));

        JPanel pnlButs = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlButs.setOpaque(false);

        POSButton butFinish = new POSButton("<html>Finish<br>Order</html>", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener finishLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.CHECKOUT_PROCESS);
            }
        };
        butFinish.addActionListener(finishLis);

        pnlButs.add(butFinish);

        pnlBuffersSub.add(pnlButs);

        pnlBuffers.add(pnlBuffersSub);

        pnlMain.add(pnlOrderLayout, BorderLayout.CENTER);
        pnlMain.add(pnlBuffers, BorderLayout.EAST);
        repaint();
    }

    public void updateBuffer(MoneyBuffer payment, Order order) {
        if (cashBufferLabel != null && creditBufferLabel != null && totalPaymentBufferLabel != null && balanceLabel != null) {
            if (payment != null) {
                if (order != null) {
                    if (intMode == POSController.MODE_CHECKOUT) {
                        cashBufferLabel.setText("Cash:   " + money.format(payment.getCash()));
                        creditBufferLabel.setText("Credit:   " + money.format(payment.getCredit()));
                        totalPaymentBufferLabel.setText("Buffer Total:   " + money.format(payment.getBufferTotal()));
                        double balance = 0d;
                        if (order.getPrice().compareTo(payment.getBufferTotal()) <= 0) {
                            balance = 0d;
                            balanceLabel.setForeground(Color.GREEN.darker());
                        } else if (order.getPrice().compareTo(payment.getBufferTotal()) > 0) {
                            balance = order.getPrice().subtract(payment.getBufferTotal()).doubleValue();
                            balanceLabel.setForeground(Color.RED.darker());
                        }
                        balanceLabel.setText("Balance:   " + money.format(balance));
                        creditBufferLabel.repaint();
                        cashBufferLabel.repaint();
                        totalPaymentBufferLabel.repaint();
                    }
                }
            }
        }
    }

    private void loadItemScreen() {
        if (pnlOrder != null)
            pnlBackground.remove(pnlOrder);

        if (pnlNorth != null)
            pnlBackground.remove(pnlNorth);

        if (pnlLeft != null)
            pnlBackground.remove(pnlLeft);

        if (northBox != null)
            pnlBackground.remove(northBox);

        pnlMain.removeAll();
        pnlMain.setOpaque(false);

        pnlOrder = new JPanel(new BorderLayout(3, 3));
        pnlOrder.setOpaque(false);

        pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        pnlLeft = new JPanel();
        pnlLeft.setOpaque(false);

        // subpanels
        pnlCats = new Box(BoxLayout.X_AXIS) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.GRAY);
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 5, 5);
            }
        };
        pnlCats.setOpaque(false);

        pnlItems = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlItems.setOpaque(false);

        pnlOrder.setPreferredSize(new Dimension(275, 0));
        pnlOrder.setOpaque(false);
        pnlOrder.setVisible(true);

        POSButton butVoidOrder = new POSButton("Void Order", BUTTONWIDTH, BUTTONHEIGHT);
        POSButton butVoidLast = new POSButton("Void Last", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener voidOrderLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.ORDER_VOIDORDER);
            }
        };
        ActionListener voidLastLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.ORDER_VOIDLAST);
            }
        };
        butVoidOrder.addActionListener(voidOrderLis);
        butVoidLast.addActionListener(voidLastLis);
        Box bxVoids = new Box(BoxLayout.X_AXIS);
        bxVoids.setOpaque(false);
        bxVoids.add(Box.createHorizontalStrut(30));
        bxVoids.add(butVoidOrder);
        bxVoids.add(Box.createHorizontalStrut(30));
        bxVoids.add(butVoidLast);
        pnlOrder.add(bxVoids, BorderLayout.SOUTH);

        northBox = new Box(BoxLayout.X_AXIS);
        northBox.setOpaque(false);

        hotBar = new Box(BoxLayout.X_AXIS) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.GRAY);
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 5, 5);
            }
        };
        hotBar.setOpaque(false);

        POSButton butExit = new POSButton("Exit System", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener exitLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.SYSTEM_EXIT);
            }
        };

        butExit.addActionListener(exitLis);
        northBox.add(Box.createRigidArea(new Dimension(5, BUTTONHEIGHT + 10)));
        northBox.add(butExit);
        POSButton butNoSale = new POSButton("No Sale", BUTTONWIDTH, BUTTONHEIGHT);
        ActionListener noSaleLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.NO_SALE);
            }
        };
        northBox.add(Box.createRigidArea(new Dimension(BUTTONWIDTH, BUTTONHEIGHT)));
        butNoSale.addActionListener(noSaleLis);
        northBox.add(butNoSale);
        northBox.add(Box.createRigidArea(new Dimension(5, BUTTONHEIGHT + 10)));

        pnlNorth.add(northBox, BorderLayout.WEST);
        pnlNorth.add(hotBar, BorderLayout.CENTER);
        pnlMain.add(pnlCats, BorderLayout.NORTH);
        pnlMain.add(pnlItems);

        pnlItems.setOpaque(false);
        loadStudentPanel();
        pnlBackground.add(pnlOrder, BorderLayout.EAST);
        pnlBackground.add(pnlNorth, BorderLayout.NORTH);

        firePOSEvent(POSEvent.ITEMS_LOADCAT);

        pnlBackground.add(pnlMain);
        repaint();
        validate();

        loadStudentPanel();

    }

    public void addHotbarItems(java.util.List<OrderItem> items) {
        int spacer = 10;
        if (hotBar != null) {
            hotBar.removeAll();
            hotBar.add(Box.createHorizontalStrut(5));
            int loc = 5;
            for (OrderItem item : items) {
                if (item.completeItem()) {
                    if (loc + BUTTONWIDTH + spacer + 20 > hotBar.getWidth())
                        break;

                    final POSButton butItem = new POSButton("<html><b><u>" + item.getName() + "</u></b><br>" + item.getNormalPriceString() + "</html>", BUTTONWIDTH, BUTTONHEIGHT);
                    butItem.setObjectRef(Collections.singletonList(item));
                    ActionListener addItemLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.ITEMS_ADDITEM, butItem);
                        }
                    };
                    butItem.addActionListener(addItemLis);
                    hotBar.add(Box.createRigidArea(new Dimension(5, BUTTONHEIGHT + 10)));
                    hotBar.add(butItem);
                    hotBar.add(Box.createHorizontalStrut(spacer));
                    loc += BUTTONWIDTH + spacer;
                }
            }
            hotBar.repaint();
        }
    }

    public void loadStudentImage(ImageIcon i) {
        if (intMode == POSController.MODE_ITEMS) {
            if (pnlImg != null) {
                pnlImg.removeAll();
            }
            if (i != null) {
                if (i.getIconWidth() > 0 && i.getIconHeight() > 0) {
                    setStatus("Loading Image.");
                    pnlImg = null;
                    pnlImg = new Box(BoxLayout.Y_AXIS);
                    pnlImg.add(Box.createRigidArea(new Dimension(20, 10)));
                    pnlImg.setPreferredSize(new Dimension(i.getIconWidth(), 0));
                    JLabel lblImg = new JLabel(i);
                    lblImg.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                    pnlImg.add(lblImg);
                    pnlStudentInfo.add(pnlImg, BorderLayout.WEST);
                    setStatus("Image Loaded.");
                    pnlStudentInfo.repaint();
                    pnlImg.repaint();

                }
            }
        }
    }

    private void loadStudentPanel() {
        if (intMode == POSController.MODE_ITEMS) {
            if (pnlStudentInfo == null) {
                pnlStudentInfo = new JPanel(new BorderLayout(5, 5)) {
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(Color.GRAY);
                        g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 5, 5);
                    }
                };
                pnlStudentInfo.setOpaque(false);
                pnlStudentInfo.setPreferredSize(new Dimension(0, 200));
            }

            pnlMain.add(pnlStudentInfo, BorderLayout.SOUTH);
            pnlStudentInfo.removeAll();

            toggleButton = new POSButton(BMSG_CHECKOUT, BUTTONWIDTH, BUTTONHEIGHT);
            ActionListener coLis = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    firePOSEvent(POSEvent.ITEMS_TOGGLECHECKOUT);
                }
            };
            toggleButton.addActionListener(coLis);
            Box subMenu = new Box(BoxLayout.Y_AXIS);
            subMenu.setPreferredSize(new Dimension(BUTTONWIDTH * 2, 0));
            subMenu.add(Box.createRigidArea(new Dimension(BUTTONWIDTH + 25, 5)));
            subMenu.add(toggleButton);
            pnlStudentInfo.add(subMenu, BorderLayout.EAST);

            //pnlMain.add( pnlItems );
        }
    }

    public void loadStudent(Student stu, boolean showMealStatus, boolean gotSpecialBreakfastToday, boolean gotSpecialLunchToday, double creditAmount) {
        if (intMode == POSController.MODE_ITEMS) {
            loadStudentPanel();
            if (stu != null) {
                setStatus("Processing: " + stu.getStudentNumber() + ". Please wait...");
                pnlImg = new Box(BoxLayout.Y_AXIS);
                pnlImg.setPreferredSize(new Dimension(200, (int) (pnlStudentInfo.getHeight() * 0.75)));
                pnlImg.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                pnlImg.setOpaque(true);


                Box pnlMoreInfo = new Box(BoxLayout.Y_AXIS);
                pnlMoreInfo.setOpaque(false);

                JPanel pnlInfoGrid = new JPanel(new GridBagLayout());
                pnlInfoGrid.setOpaque(false);
                GridBagConstraints con = new GridBagConstraints();
                con.fill = GridBagConstraints.BOTH;
                con.insets = new Insets(0, 0, 0, 0);

                int row = 0;

                con.gridx = 0;
                con.gridy = row;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel("Student Name:"), con);

                con.gridx = 1;
                con.gridy = row++;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel(stu.getStudentName() + ""), con);

                con.gridx = 0;
                con.gridy = row;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel("Student Number:      "), con);

                con.gridx = 1;
                con.gridy = row++;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel(stu.getStudentNumber() + ""), con);

                if (showMealStatus) {
                    con.gridx = 0;
                    con.gridy = row;
                    con.gridwidth = 1;
                    con.gridheight = 1;
                    con.weightx = con.weighty = 0.0;
                    pnlInfoGrid.add(new JLabel("Meal Status:"), con);

                    con.gridx = 1;
                    con.gridy = row++;
                    con.gridwidth = 1;
                    con.gridheight = 1;
                    con.weightx = con.weighty = 0.0;
                    pnlInfoGrid.add(new JLabel(stu.getMealStatus() + ""), con);

                    if (stu.canGetFreeMeal() || stu.canGetReducedMeal()) {
                        String lnch, brk;
                        if (gotSpecialBreakfastToday)
                            brk = "YES";
                        else
                            brk = "NO";

                        if (gotSpecialLunchToday)
                            lnch = "YES";
                        else
                            lnch = "NO";

                        con.gridx = 0;
                        con.gridy = row;
                        con.gridwidth = 1;
                        con.gridheight = 1;
                        con.weightx = con.weighty = 0.0;
                        pnlInfoGrid.add(new JLabel("Got F/R Breakfast:  "), con);

                        con.gridx = 1;
                        con.gridy = row++;
                        con.gridwidth = 1;
                        con.gridheight = 1;
                        con.weightx = con.weighty = 0.0;
                        pnlInfoGrid.add(new JLabel(brk + ""), con);

                        con.gridx = 0;
                        con.gridy = row;
                        con.gridwidth = 1;
                        con.gridheight = 1;
                        con.weightx = con.weighty = 0.0;
                        pnlInfoGrid.add(new JLabel("Got F/R Lunch:  "), con);

                        con.gridx = 1;
                        con.gridy = row++;
                        con.gridwidth = 1;
                        con.gridheight = 1;
                        con.weightx = con.weighty = 0.0;
                        pnlInfoGrid.add(new JLabel(lnch + ""), con);
                    }

                }

                con.gridx = 0;
                con.gridy = row;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel("Credit:"), con);

                con.gridx = 1;
                con.gridy = row++;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel(money.format(creditAmount) + ""), con);

                String act = "Active";
                String exists = "";
                if (!stu.getIsActiveStudent())
                    act = "Inactive";

                if (!stu.getExistsInDB())
                    exists = " (Not found in SIS)";

                con.gridx = 0;
                con.gridy = row;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel("Status:"), con);

                con.gridx = 1;
                con.gridy = row++;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(new JLabel(act + exists + ""), con);

                POSButton butStudentToggle;
                if (stu.isStudentSet()) {
                    butStudentToggle = new POSButton("<html>Reset<br>Student</html>", BUTTONWIDTH, BUTTONHEIGHT);
                    ActionListener stuResetLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.STUDENT_RESET);
                        }
                    };
                    butStudentToggle.addActionListener(stuResetLis);
                } else {
                    butStudentToggle = new POSButton("<html>Manual<br>Entry</html>", BUTTONWIDTH, BUTTONHEIGHT);
                    ActionListener stuManLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.ITEMS_MANUALENTRY);
                        }
                    };
                    butStudentToggle.addActionListener(stuManLis);
                }

                con.gridx = 0;
                con.gridy = row;
                con.gridwidth = 1;
                con.gridheight = 1;
                con.weightx = con.weighty = 0.0;
                pnlInfoGrid.add(butStudentToggle, con);

                pnlMoreInfo.add(pnlInfoGrid);
                //pnlMoreInfo.add( butStudentToggle );
                pnlMoreInfo.add(Box.createRigidArea(new Dimension(20, 0)));
                pnlStudentInfo.add(pnlMoreInfo);

                setStatus("Student loaded.");
            }
            pnlStudentInfo.repaint();
        }
    }

    public void loadKeyPad(POSEventListener listener) {
        PSEntryPad keyP = new PSEntryPad(this, new POSButton(" ", BUTTONWIDTH, BUTTONHEIGHT));
        keyP.addPOSEventListener(listener);
        keyP.display();

    }

    public void loadCatgories(java.util.List<String> cats) {
        if (pnlCats != null) {
            pnlCats.removeAll();
            if (cats != null) {
                for (String cat : cats) {
                    final POSButton tmp = new POSButton(cat, BUTTONWIDTH, BUTTONHEIGHT);

                    ActionListener loadItemsLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.ITEMS_LOADITEMS, tmp);
                        }
                    };
                    tmp.addActionListener(loadItemsLis);
                    tmp.setObjectRef(cat);

                    pnlCats.add(Box.createRigidArea(new Dimension(5, BUTTONHEIGHT + 10)));
                    pnlCats.add(tmp);
                    pnlCats.add(Box.createHorizontalStrut(5));

                }
            }
            pnlCats.repaint();
        }
    }


    public void loadItems(java.util.List<OrderItem> items, java.util.List<ItemsPackage> itemsPackageList) {

        if (pnlItems != null) {
            pnlItems.removeAll();
            for (ItemsPackage itemsPackage : itemsPackageList) {
                if (itemsPackage != null) {
                    final POSButton butItem = new POSButton("<html><b><u>" + itemsPackage.getName() + "</u></b><br>" + itemsPackage.getTotalString() + "</html>", BUTTONWIDTH, BUTTONHEIGHT);
                    butItem.setObjectRef(itemsPackage.getItems());
                    ActionListener addItemLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.ITEMS_ADDITEM, butItem);
                        }
                    };
                    butItem.addActionListener(addItemLis);
                    pnlItems.add(butItem);
                }


            }
            for (OrderItem item1 : items) {
                if (item1.completeItem()) {
                    final POSButton butItem = new POSButton("<html><b><u>" + item1.getName() + "</u></b><br>" + item1.getNormalPriceString() + "</html>", BUTTONWIDTH, BUTTONHEIGHT, item1.getIco());
                    butItem.setObjectRef(java.util.Collections.singletonList(item1));
                    ActionListener addItemLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            firePOSEvent(POSEvent.ITEMS_ADDITEM, butItem);
                        }
                    };
                    butItem.addActionListener(addItemLis);
                    pnlItems.add(butItem);
                }


                //org.pscafepos.backends.domain.items.OrderItem item = (org.pscafepos.backends.domain.items.OrderItem)it.next();

            }
            pnlItems.repaint();
        }
    }

    public void removeLastOrderScreen() {
        if (pnlLastOrder != null) {
            pnlOrder.remove(pnlLastOrder);
            pnlOrder.repaint();
        }
    }

    public void loadStudentsLastOrder(Order o) {
        int hght = 50;
        int titlehght = 30;
        int totalheight = 250;

        if (pnlLastOrder != null)
            pnlOrder.remove(pnlLastOrder);

        if (o != null) {
            if (o.getItemsCount() > 0) {
                pnlLastOrder = new JPanel(new BorderLayout());
                pnlLastOrder.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, totalheight));
                pnlLastOrder.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, totalheight));
                pnlLastOrder.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, totalheight));
                pnlLastOrder.setOpaque(false);

                Box lastItemOrder = new Box(BoxLayout.Y_AXIS) {
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        GradientPaint grad = new GradientPaint(0, 0, new Color(229, 229, 229), 400, 300, Color.GRAY.brighter());
                        g2d.setPaint(grad);
                        g2d.fill(g.getClipBounds());
                    }
                };

                JScrollPane scrollLastOrder = new JScrollPane(lastItemOrder, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollLastOrder.setOpaque(false);
                pnlLastOrder.add(scrollLastOrder);

                JPanel pnlActivateOrder = new JPanel(new FlowLayout(FlowLayout.CENTER));
                pnlActivateOrder.setOpaque(false);

                POSButton butActivate = new POSButton("Activate", BUTTONWIDTH, BUTTONHEIGHT);
                ActionListener actLis = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        firePOSEvent(POSEvent.ITEMS_ACTIVATELASTORDER);
                    }
                };

                butActivate.addActionListener(actLis);
                pnlActivateOrder.add(butActivate);

                pnlLastOrder.add(pnlActivateOrder, BorderLayout.SOUTH);
                pnlOrder.add(pnlLastOrder, BorderLayout.NORTH);


                JLabel title = new JLabel(o.getTitle(), SwingConstants.LEFT);
                JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                fillerPanel.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
                fillerPanel.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
                fillerPanel.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
                fillerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                fillerPanel.add(title);
                fillerPanel.setOpaque(true);
                fillerPanel.setBackground(Color.WHITE);
                lastItemOrder.add(Box.createVerticalStrut(5));
                lastItemOrder.add(fillerPanel);


                OrderItem items[] = o.getOrderItems();
                if (items != null) {
                    int cnt = 1;
//     							Color[] colors = new Color[2];
//     							colors[0] = new Color( 222, 222, 222 );
//     							colors[1] = new Color( 191, 191, 191);

                    for (OrderItem item : items) {
                        if (item.completeItem()) {
                            OrderItemPanel p = new OrderItemPanel() {
                                protected void paintComponent(Graphics g) {
                                    Graphics2D g2d = (Graphics2D) g;
                                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                    GradientPaint grad;
                                    grad = new GradientPaint(0, 10, new Color(229, 229, 229), 0, 80, Color.GRAY.brighter());
                                    g2d.setPaint(grad);
                                    g2d.fill(g.getClipBounds());
                                }
                            };

                            p.setObjectRef(item);
                            p.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                            p.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                            p.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                            p.setOpaque(true);
                            p.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));

                            JLabel name = new JLabel("  " + cnt + ") " + item.getName());

                            JLabel price = new JLabel(item.getEffectivePriceString() + "   ", SwingConstants.RIGHT);
                            p.add(name, BorderLayout.CENTER);
                            p.add(price, BorderLayout.EAST);

                            lastItemOrder.add(p);
                            cnt++;
                        }
                    }
                }
                pnlOrder.repaint();
            }
        }
    }

    public void refreshOrder(Order order) {
        int hght = 50;
        int titlehght = 30;

        if (itemOrder != null && scrollOrder != null) {
            scrollOrder.remove(itemOrder);
            pnlOrder.remove(scrollOrder);
        }
        itemOrder = null;
        itemOrder = new Box(BoxLayout.Y_AXIS) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint redtowhite = new GradientPaint(0, 0, new Color(229, 229, 229), 400, 300, Color.GRAY.brighter());
                g2d.setPaint(redtowhite);
                g2d.fill(g.getClipBounds());
            }
        };
        itemOrder.setOpaque(false);
        scrollOrder = new JScrollPane(itemOrder, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollOrder.setOpaque(false);
        if (pnlOrder != null) {
            pnlOrder.add(scrollOrder);
            pnlOrder.setOpaque(false);
        }

        JLabel title = new JLabel("Current Order", SwingConstants.LEFT);
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fillerPanel.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
        fillerPanel.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
        fillerPanel.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
        fillerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        fillerPanel.add(title);
        fillerPanel.setOpaque(true);
        fillerPanel.setBackground(Color.WHITE);
        itemOrder.add(Box.createVerticalStrut(5));
        itemOrder.add(fillerPanel);
        if (order != null && order.getItemsCount() > 0) {
            OrderItem items[] = order.getOrderItems();
            if (items != null) {
                int cnt = 1;
//                    Color[] colors = new Color[2];
//                    colors[0] = new Color( 222, 222, 222 );
//                    colors[1] = new Color( 191, 191, 191);

                for (OrderItem item : items) {
                    if (item.completeItem()) {
                        OrderItemPanel p = new OrderItemPanel() {
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g;
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                GradientPaint redtowhite;
                                if (!isTapped())
                                    redtowhite = new GradientPaint(0, 10, new Color(229, 229, 229), 0, 80, Color.GRAY.brighter());
                                else
                                    redtowhite = new GradientPaint(0, 10, Color.RED, 0, 80, Color.GRAY.brighter());
                                g2d.setPaint(redtowhite);
                                g2d.fill(g.getClipBounds());
                            }
                        };

                        final OrderItemPanel sItem = p;
                        MouseListener orderItemTapLis = new MouseListener() {
                            public void mouseClicked(MouseEvent e) {
                                firePOSEvent(POSEvent.ORDER_VOIDITEM, sItem);
                            }

                            public void mouseReleased(MouseEvent e) {
                            }

                            public void mousePressed(MouseEvent e) {
                            }

                            public void mouseExited(MouseEvent e) {
                            }

                            public void mouseEntered(MouseEvent e) {
                            }
                        };

                        p.addMouseListener(orderItemTapLis);
                        p.setObjectRef(item);
                        p.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                        p.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                        p.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, hght));
                        //p.setBackground( colors[cnt % 2] );
                        p.setOpaque(true);
                        p.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));
                        JLabel name = new JLabel("  " + cnt + ") " + item.getName());

                        JLabel price = new JLabel(item.getEffectivePriceString() + "   ", SwingConstants.RIGHT);
                        p.add(name, BorderLayout.CENTER);
                        p.add(price, BorderLayout.EAST);

                        itemOrder.add(p);
                        cnt++;
                    }
                }
                JLabel total = new JLabel("Order Total:" + order.getOrderTotalString());
                total.setPreferredSize(new Dimension(0, hght));
                total.setMaximumSize(new Dimension(250, hght));

                itemOrder.add(total);
            }
        } else {
            JLabel noItems = new JLabel("No items in current order", SwingConstants.LEFT);
            noItems.setOpaque(false);

            JPanel nip = new JPanel(new FlowLayout(FlowLayout.CENTER));
            nip.setOpaque(false);
            nip.setPreferredSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
            nip.setMaximumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
            nip.setMinimumSize(new Dimension(pnlOrder.getWidth() - 25, titlehght));
            nip.add(noItems);
            itemOrder.add(nip);
            //itemOrder.setOpaque( false );
        }

        itemOrder.repaint();
        pnlOrder.repaint();
        pnlOrder.invalidate();
        pnlOrder.validate();
    }

    private void loadLoginScreen() {
        Font line1 = new Font("Arial", Font.BOLD, 15);
        Font line2 = new Font("Arial", Font.PLAIN, 11);
        FlowLayout lay = new FlowLayout();
        JPanel pnlLogin = new JPanel(lay);
        pnlLogin.setOpaque(false);
        lay.setVgap(150);

        JPanel subMainLogin = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint redtowhite = new GradientPaint(0, 0, new Color(229, 229, 229), 200, 250, Color.GRAY.brighter());
                g2d.setPaint(redtowhite);
                g2d.fill(g.getClipBounds());

                //g2d.setColor( Color.GRAY );
                //g2d.fillOval(-10, -10, 20, 20 );

                //g2d.drawRect( 0, 0, (int)g.getClipBounds().getWidth() -1, (int)g.getClipBounds().getHeight() -1);
            }
        };
        subMainLogin.setOpaque(false);
        subMainLogin.setPreferredSize(new Dimension(350, 150));
        //subMainLogin.setBackground( new Color( 249, 250, 220 ) );
        subMainLogin.setBorder(BorderFactory.createLineBorder(Color.BLACK));


        JPanel pnlTopMost = new JPanel(new GridLayout(2, 1, 4, 4));
        pnlTopMost.setOpaque(false);
        subMainLogin.add(pnlTopMost, BorderLayout.NORTH);
        JLabel lblTitle = new JLabel("PSCafePOS", SwingConstants.CENTER);
        lblTitle.setFont(line1);
        JLabel lblLine2 = new JLabel("Version " + Constants.VERSION, SwingConstants.CENTER);
        lblLine2.setFont(line2);
        lblLine2.setForeground(Color.GRAY);
        pnlTopMost.add(lblTitle);
        pnlTopMost.add(lblLine2);

        JPanel in = new JPanel(new GridLayout(2, 2, 5, 5));
        in.setOpaque(false);
        JLabel lblUser = new JLabel("    Username: ");
        JLabel lblPass = new JLabel("    Password: ");

        ActionListener passLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.SYSTEM_LOGIN);
            }
        };

        txtUser = new JTextField(12);
        txtPass = new JPasswordField(12);
        txtPass.addActionListener(passLis);

        in.add(lblUser);
        in.add(txtUser);
        in.add(lblPass);
        in.add(txtPass);

        subMainLogin.add(in);

        JPanel buts = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buts.setOpaque(false);
        POSButton butLogin = new POSButton("Login", BUTTONWIDTH, BUTTONHEIGHT);
        POSButton butCancel = new POSButton("Cancel", BUTTONWIDTH, BUTTONHEIGHT);
        buts.add(butLogin);
        buts.add(butCancel);

        ActionListener cancelLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                firePOSEvent(POSEvent.SYSTEM_EXIT);
            }
        };

        ActionListener loginLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.SYSTEM_LOGIN);
            }
        };

        butLogin.addActionListener(loginLis);
        butCancel.addActionListener(cancelLis);

        subMainLogin.add(buts, BorderLayout.SOUTH);

        pnlLogin.add(subMainLogin);
        pnlMain.add(pnlLogin);
        pnlMain.setVisible(true);
        validate();

        txtUser.grabFocus();
    }

    public void loadKeyManager(POSStudentListener p) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new PSKeyHandler(p));
    }

    public String getLoginUserName() {
        if (txtUser != null) {
            return txtUser.getText();
        }
        return "";
    }

    public String getLoginPassword() {
        if (txtPass != null) {
            return new String(txtPass.getPassword());
        }
        return "";
    }

    public void setCriticalMessage(String message) {
        logger.log(Level.SEVERE, message);
        JOptionPane.showMessageDialog(this, "Critical Error: " + message);
        System.out.println("Exiting System...");
        System.exit(-1);
    }

    // Event Handling Stuff
    public void addPOSEventListener(POSEventListener listener) {
        posEventListenerList.add(listener);
    }

    public void removePOSEventListener(POSEventListener listener) {
        posEventListenerList.remove(listener);
    }

    private void firePOSEvent(int eventType) {
        POSEvent posEvent = new POSEvent(this, eventType);

        for (Object aPosEventListenerList : posEventListenerList) {
            ((POSEventListener) aPosEventListenerList).onPosEvent(posEvent);
        }

    }

    private void firePOSEvent(int eventType, Object sender) {
        POSEvent posEvent = new POSEvent(sender, eventType);

        for (Object aPosEventListenerList : posEventListenerList) {
            ((POSEventListener) aPosEventListenerList).onPosEvent(posEvent);
        }

    }

    public void setEnabledGUI(boolean en) {
        setEnabled(en);
    }

    public boolean promt(String text) {
        if (JOptionPane.showConfirmDialog(this, text, "Are you sure?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }

    }
}
