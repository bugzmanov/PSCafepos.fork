package org.pscafepos.settings.wizzard.gui;/*   PSCafePOS is an Open Source Point of Sale System for Schools
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

import org.pscafepos.settings.DBSettings;
import org.pscafepos.settings.wizzard.gui.events.SettingEvent;
import org.pscafepos.settings.wizzard.gui.events.SettingEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PSSettingWizardGUI extends JFrame implements PSSettingGUI {
    private Container root;
    private Vector posEventListenerList;

    private JLabel lblTitle;
    private JPanel pnlTitle;

    private JLabel lblStatus;
    private JPanel pnlStatus;

    private JPanel pnlMiddle;
    private JPanel pnlButtons;

    private JPanel[] pnls;
    private JScrollPane scroll;

    private Vector[] comRefs;

    private JButton btnNext;
    private JButton btnBack;

    private int intCurrentScreen;
    private String[] strMessages = {" Welcome", " General Info", " POS Config", " Student Infomation System Config", " Cash Drawer Config", " Finish Config"};
    private String[] strStatus = {" Thanks for choosing PSCafePOS :: Open Source Software", " Configure general system settings", " Configure POS database connection information", " Configure Student Information System connection details", " Configure Cash Drawer Support", " Finish and save your configuration"};

    private static final String MESSAGEPREFIX = "PSCafePOS :: Configuration Wizard : ";

    public PSSettingWizardGUI() {
        super("PSCafePOS :: Configuration Wizard");
        pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint redtowhite = new GradientPaint(0, 0, new Color(229, 229, 229), 400, 300, Color.GRAY.brighter());
                g2d.setPaint(redtowhite);
                g2d.fill(g.getClipBounds());
            }
        };
        pnlTitle.setOpaque(true);
        pnlTitle.setBorder(BorderFactory.createLineBorder(new Color(109, 109, 109), 1));

        lblTitle = new JLabel("PSCafePOS :: Configuration Wizard : Welcome");
        lblTitle.setOpaque(false);

        pnlTitle.add(lblTitle);

        pnlStatus = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint redtowhite = new GradientPaint(0, 0, new Color(229, 229, 229), 400, 300, Color.GRAY.brighter());
                g2d.setPaint(redtowhite);
                g2d.fill(g.getClipBounds());
            }
        };
        pnlStatus.setOpaque(true);
        pnlStatus.setBorder(BorderFactory.createLineBorder(new Color(109, 109, 109), 1));

        lblStatus = new JLabel("Loading...");
        lblStatus.setOpaque(false);
        pnlStatus.add(lblStatus);

        pnlMiddle = new JPanel(new BorderLayout());

        pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));

        posEventListenerList = new Vector();

    }

    public void regenerateScreens(DBSettings settings) {
        pnls = null;
        pnls = new JPanel[6];
        comRefs = new Vector[pnls.length];

        boolean blSet = false;

        if (settings != null)
            blSet = true;

        for (int i = 0; i < pnls.length; i++) {
            pnls[i] = new JPanel(new GridBagLayout());
            comRefs[i] = new Vector();

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(5, 5, 5, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = c.weighty = 0;
            int y = 0;
            switch (i) {
                case 0:
                    pnls[i].setLayout(new FlowLayout(FlowLayout.CENTER));
                    pnls[i].add(new JLabel("<html><br><br><b><i>Welcome to PSCafePOS</i></b><br><br>The next few screens will allow you to configure PSCafePOS to work with your Student Information System.<br>" +
                            "For more information or if you have any questions regarding setup of PSCafePOS please check the online <br>" +
                            "documentation.  If you are still in need of more help please check the Sourceforge forums." +
                            "<br><br><br>" +
                            "Please check the PSCafePOS website for preconfigured database files for your school's SIS." +
                            "<br><br><br>" +
                            "Thanks for your interest in PSCafePOS.  This is a community driven product and as such, the roadmap is<br>" +
                            "is directed by the users of PSCafePOS.  If there is something that you would like to see in PSCafePOS, please<br>" +
                            "let us know." +
                            "<br><br><br>" +
                            "PSCafePOS is Open Source and free to use, released under GPL.<br><br><br>" +
                            "http://pscafe.sourceforge.net<br>http://sourceforge.net/projects/pscafe</html>"));
                    break;
                case 1:
                    pnls[i].setLayout(new GridBagLayout());

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Building Name:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtBuildingName = new PSSettingTextField(25);
                    txtBuildingName.setSettingID(DBSettings.MAIN_BUILDINGNAME);
                    if (blSet) txtBuildingName.setText(settings.get(DBSettings.MAIN_BUILDINGNAME));
                    pnls[i].add(txtBuildingName, c);
                    comRefs[i].add(txtBuildingName);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Building ID:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtBuildingID = new PSSettingTextField(15);
                    txtBuildingID.setSettingID(DBSettings.MAIN_BUILDING);
                    if (blSet) txtBuildingID.setText(settings.get(DBSettings.MAIN_BUILDING));
                    pnls[i].add(txtBuildingID, c);
                    comRefs[i].add(txtBuildingID);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Allow Anonymous Transactions:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkAnon = new PSSettingCheckbox();
                    chkAnon.setSettingID(DBSettings.MAIN_ALLOWANONTRANS);
                    if (blSet) chkAnon.setSelected(settings.get(DBSettings.MAIN_ALLOWANONTRANS).compareTo("1") == 0);
                    pnls[i].add(chkAnon, c);
                    comRefs[i].add(chkAnon);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Allow Negative Balances:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkNeg = new PSSettingCheckbox();
                    chkNeg.setSettingID(DBSettings.MAIN_ALLOWNEGBALANCES);
                    if (blSet) chkNeg.setSelected(settings.get(DBSettings.MAIN_ALLOWNEGBALANCES).compareTo("1") == 0);
                    pnls[i].add(chkNeg, c);
                    comRefs[i].add(chkNeg);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Negative Balance Max:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtNegAmnt = new PSSettingTextField(15);
                    txtNegAmnt.setSettingID(DBSettings.MAIN_MAXNEGBALANCE);
                    if (blSet) txtNegAmnt.setText(settings.get(DBSettings.MAIN_MAXNEGBALANCE));
                    pnls[i].add(txtNegAmnt, c);
                    comRefs[i].add(txtNegAmnt);

//---------
                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Sell on credit only:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox creditModeCheckBox = new PSSettingCheckbox();
                    creditModeCheckBox.setSettingID(DBSettings.MAIN_SELL_ON_CREDIT_ONLY);
                    if (blSet)
                        creditModeCheckBox.setSelected(settings.get(DBSettings.MAIN_SELL_ON_CREDIT_ONLY).equals("1"));
                    pnls[i].add(creditModeCheckBox, c);
                    comRefs[i].add(creditModeCheckBox);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Auto checkout mode:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox autoCheckoutCheckBox = new PSSettingCheckbox();
                    autoCheckoutCheckBox.setSettingID(DBSettings.MAIN_AUTO_CHECKOUT);
                    if (blSet)
                        autoCheckoutCheckBox.setSelected(settings.get(DBSettings.MAIN_AUTO_CHECKOUT).equals("1"));
                    pnls[i].add(autoCheckoutCheckBox, c);
                    comRefs[i].add(autoCheckoutCheckBox);
//---------


                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Debug Mode:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkDebug = new PSSettingCheckbox();
                    chkDebug.setSettingID(DBSettings.MAIN_DEBUG);
                    if (blSet) chkDebug.setSelected(settings.get(DBSettings.MAIN_DEBUG).compareTo("1") == 0);
                    pnls[i].add(chkDebug, c);
                    comRefs[i].add(chkDebug);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Hotbar Enabled:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkHB = new PSSettingCheckbox();
                    chkHB.setSettingID(DBSettings.HOTBAR_ENABLED);
                    if (blSet) chkHB.setSelected(settings.get(DBSettings.HOTBAR_ENABLED).compareTo("1") == 0);
                    pnls[i].add(chkHB, c);
                    comRefs[i].add(chkHB);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Update URL:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtURL = new PSSettingTextField(15);
                    txtURL.setSettingID(DBSettings.UPDATEURL);
                    if (blSet) txtURL.setText(settings.get(DBSettings.UPDATEURL));
                    pnls[i].add(txtURL, c);
                    comRefs[i].add(txtURL);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Background Image:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtBG = new PSSettingTextField(15);

                    txtBG.setSettingID(DBSettings.IMAGES_MAINBG);
                    //fleBG.setSettingID( org.pscafepos.settings.DBSettings.IMAGES_MAINBG );
                    //if ( blSet ) fleBG.setSelectedFile(new File( org.pscafepos.settings.get( org.pscafepos.settings.DBSettings.IMAGES_MAINBG ) ) );
                    if (blSet) txtBG.setText(settings.get(DBSettings.IMAGES_MAINBG));
                    pnls[i].add(txtBG, c);
                    //pnls[i].add( fleBG, c );
                    comRefs[i].add(txtBG);
                    //comRefs[i].add( fleBG );
                    break;
                case 2:
                    pnls[i].setLayout(new GridBagLayout());

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS JDBC Driver:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSJDBC = new PSSettingTextField(25);
                    txtPOSJDBC.setSettingID(DBSettings.POS_JDBCDRIVER);
                    if (blSet) txtPOSJDBC.setText(settings.get(DBSettings.POS_JDBCDRIVER));
                    pnls[i].add(txtPOSJDBC, c);
                    comRefs[i].add(txtPOSJDBC);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS JDBC Database Type:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSJDBCType = new PSSettingTextField(25);
                    txtPOSJDBCType.setSettingID(DBSettings.POS_JDBCDBTYPE);
                    if (blSet) txtPOSJDBCType.setText(settings.get(DBSettings.POS_JDBCDBTYPE));
                    pnls[i].add(txtPOSJDBCType, c);
                    comRefs[i].add(txtPOSJDBCType);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS Database Port:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSPort = new PSSettingTextField(25);
                    txtPOSPort.setSettingID(DBSettings.POS_JDBCPORT);
                    if (blSet) txtPOSPort.setText(settings.get(DBSettings.POS_JDBCPORT));
                    pnls[i].add(txtPOSPort, c);
                    comRefs[i].add(txtPOSPort);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS Database Hostname or IP:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSHostname = new PSSettingTextField(25);
                    txtPOSHostname.setSettingID(DBSettings.POS_HOSTNAME);
                    if (blSet) txtPOSHostname.setText(settings.get(DBSettings.POS_HOSTNAME));
                    pnls[i].add(txtPOSHostname, c);
                    comRefs[i].add(txtPOSHostname);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS Database name:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSdb = new PSSettingTextField(25);
                    txtPOSdb.setSettingID(DBSettings.POS_DATABASE);
                    if (blSet) txtPOSdb.setText(settings.get(DBSettings.POS_DATABASE));
                    pnls[i].add(txtPOSdb, c);
                    comRefs[i].add(txtPOSdb);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS Table Prefix:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSpf = new PSSettingTextField(25);
                    txtPOSpf.setSettingID(DBSettings.POS_TABLEPREFIX);
                    if (blSet) txtPOSpf.setText(settings.get(DBSettings.POS_TABLEPREFIX));
                    pnls[i].add(txtPOSpf, c);
                    comRefs[i].add(txtPOSpf);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SSL Enabled:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSSL = new PSSettingCheckbox();
                    chkSSL.setSettingID(DBSettings.POS_SSLENABLED);
                    if (blSet) chkSSL.setSelected(settings.get(DBSettings.POS_SSLENABLED).compareTo("1") == 0);
                    pnls[i].add(chkSSL, c);
                    comRefs[i].add(chkSSL);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS SSL Trust Store"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSTrust = new PSSettingTextField(25);
                    txtPOSTrust.setSettingID(DBSettings.POS_SSLTRUSTSTORE);
                    if (blSet) txtPOSTrust.setText(settings.get(DBSettings.POS_SSLTRUSTSTORE));
                    pnls[i].add(txtPOSTrust, c);
                    comRefs[i].add(txtPOSTrust);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("POS SSL Key Store"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtPOSKey = new PSSettingTextField(25);
                    txtPOSKey.setSettingID(DBSettings.POS_SSLKEYSTORE);
                    if (blSet) txtPOSKey.setText(settings.get(DBSettings.POS_SSLKEYSTORE));
                    pnls[i].add(txtPOSKey, c);
                    comRefs[i].add(txtPOSKey);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SSL Debug:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSSLDebug = new PSSettingCheckbox();
                    chkSSLDebug.setSettingID(DBSettings.POS_SSLDEBUG);
                    if (blSet) chkSSLDebug.setSelected(settings.get(DBSettings.POS_SSLDEBUG).compareTo("1") == 0);
                    pnls[i].add(chkSSLDebug, c);
                    comRefs[i].add(chkSSLDebug);
                    break;
                case 3:
                    pnls[i].setLayout(new GridBagLayout());

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS enabled:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISEnabled = new PSSettingCheckbox();
                    chkSISEnabled.setSettingID(DBSettings.SIS_ENABLED);
                    if (blSet) chkSISEnabled.setSelected(settings.get(DBSettings.SIS_ENABLED).compareTo("1") == 0);
                    pnls[i].add(chkSISEnabled, c);
                    comRefs[i].add(chkSISEnabled);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Name:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISName = new PSSettingTextField(25);
                    txtSISName.setSettingID(DBSettings.SIS_SYSTEMNAME);
                    if (blSet) txtSISName.setText(settings.get(DBSettings.SIS_SYSTEMNAME));
                    pnls[i].add(txtSISName, c);
                    comRefs[i].add(txtSISName);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS JDBC Driver:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISJDBC = new PSSettingTextField(25);
                    txtSISJDBC.setSettingID(DBSettings.SIS_JDBCDRIVER);
                    if (blSet) txtSISJDBC.setText(settings.get(DBSettings.SIS_JDBCDRIVER));
                    pnls[i].add(txtSISJDBC, c);
                    comRefs[i].add(txtSISJDBC);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Connection String:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISConnString = new PSSettingTextField(25);
                    txtSISConnString.setSettingID(DBSettings.SIS_JDBCCONNECTIONSTRING);
                    if (blSet) txtSISConnString.setText(settings.get(DBSettings.SIS_JDBCCONNECTIONSTRING));
                    pnls[i].add(txtSISConnString, c);
                    comRefs[i].add(txtSISConnString);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Connection Username:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISConnUser = new PSSettingTextField(25);
                    txtSISConnUser.setSettingID(DBSettings.SIS_CONNUSERNAME);
                    if (blSet) txtSISConnUser.setText(settings.get(DBSettings.SIS_CONNUSERNAME));
                    pnls[i].add(txtSISConnUser, c);
                    comRefs[i].add(txtSISConnUser);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Connection Password:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISConnPass = new PSSettingTextField(25);
                    txtSISConnPass.setSettingID(DBSettings.SIS_CONNPASSWORD);
                    if (blSet) txtSISConnPass.setText(settings.get(DBSettings.SIS_CONNPASSWORD));
                    pnls[i].add(txtSISConnPass, c);
                    comRefs[i].add(txtSISConnPass);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration or Student Table:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISRegTable = new PSSettingTextField(25);
                    txtSISRegTable.setSettingID(DBSettings.SIS_REGISTRATIONTABLE);
                    if (blSet) txtSISRegTable.setText(settings.get(DBSettings.SIS_REGISTRATIONTABLE));
                    pnls[i].add(txtSISRegTable, c);
                    comRefs[i].add(txtSISRegTable);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student ID Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISStudentID = new PSSettingTextField(25);
                    txtSISStudentID.setSettingID(DBSettings.SIS_STUDENTIDFIELD);
                    if (blSet) txtSISStudentID.setText(settings.get(DBSettings.SIS_STUDENTIDFIELD));
                    pnls[i].add(txtSISStudentID, c);
                    comRefs[i].add(txtSISStudentID);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student ID Type:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingListBox lSIDType = new PSSettingListBox();
                    lSIDType.addItem("String");
                    lSIDType.addItem("Number");
                    lSIDType.setSettingID(DBSettings.SIS_STUDENTIDTYPE);
                    if (blSet) {
                        if (settings.get(DBSettings.SIS_STUDENTIDTYPE).compareTo("Number") == 0)
                            lSIDType.setSelectedIndex(1);
                    }
                    pnls[i].add(lSIDType, c);
                    comRefs[i].add(lSIDType);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student First Name Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISFName = new PSSettingTextField(25);
                    txtSISFName.setSettingID(DBSettings.SIS_STUDENTFNAMEFIELD);
                    if (blSet) txtSISFName.setText(settings.get(DBSettings.SIS_STUDENTFNAMEFIELD));
                    pnls[i].add(txtSISFName, c);
                    comRefs[i].add(txtSISFName);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student Last Name Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISLName = new PSSettingTextField(25);
                    txtSISLName.setSettingID(DBSettings.SIS_STUDENTLNAMEFIELD);
                    if (blSet) txtSISLName.setText(settings.get(DBSettings.SIS_STUDENTLNAMEFIELD));
                    pnls[i].add(txtSISLName, c);
                    comRefs[i].add(txtSISLName);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Use Free/Reduced Meal Status :"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISUseFR = new PSSettingCheckbox();
                    chkSISUseFR.setSettingID(DBSettings.SIS_FREEREDUCEDENABLED);
                    if (blSet)
                        chkSISUseFR.setSelected(settings.get(DBSettings.SIS_FREEREDUCEDENABLED).compareTo("1") == 0);
                    pnls[i].add(chkSISUseFR, c);
                    comRefs[i].add(chkSISUseFR);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Hide Meal Status :"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISHideMS = new PSSettingCheckbox();
                    chkSISHideMS.setSettingID(DBSettings.SIS_HIDEFREEREDUCEDINFO);
                    if (blSet)
                        chkSISHideMS.setSelected(settings.get(DBSettings.SIS_HIDEFREEREDUCEDINFO).compareTo("1") == 0);
                    pnls[i].add(chkSISHideMS, c);
                    comRefs[i].add(chkSISHideMS);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Free Meal Status Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISFMS = new PSSettingTextField(25);
                    txtSISFMS.setSettingID(DBSettings.SIS_FREEFIELD);
                    if (blSet) txtSISFMS.setText(settings.get(DBSettings.SIS_FREEFIELD));
                    pnls[i].add(txtSISFMS, c);
                    comRefs[i].add(txtSISFMS);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Value indicating free meal:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISMSF = new PSSettingTextField(25);
                    txtSISMSF.setSettingID(DBSettings.SIS_FREEVALUE);
                    if (blSet) txtSISMSF.setText(settings.get(DBSettings.SIS_FREEVALUE));
                    pnls[i].add(txtSISMSF, c);
                    comRefs[i].add(txtSISMSF);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Free Meal Status Field Type:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingListBox lFMSType = new PSSettingListBox();
                    lFMSType.addItem("String");
                    lFMSType.addItem("Number");
                    lFMSType.setSettingID(DBSettings.SIS_FREETYPE);
                    if (blSet) {
                        if (settings.get(DBSettings.SIS_FREETYPE).compareTo("Number") == 0)
                            lFMSType.setSelectedIndex(1);
                    }
                    pnls[i].add(lFMSType, c);
                    comRefs[i].add(lFMSType);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Reduced Meal Status Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISRMS = new PSSettingTextField(25);
                    txtSISRMS.setSettingID(DBSettings.SIS_REDUCEDFIELD);
                    if (blSet) txtSISRMS.setText(settings.get(DBSettings.SIS_REDUCEDFIELD));
                    pnls[i].add(txtSISRMS, c);
                    comRefs[i].add(txtSISRMS);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Value indicating reduced meal:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISMSR = new PSSettingTextField(25);
                    txtSISMSR.setSettingID(DBSettings.SIS_REDUCEDVALUE);
                    if (blSet) txtSISMSR.setText(settings.get(DBSettings.SIS_REDUCEDVALUE));
                    pnls[i].add(txtSISMSR, c);
                    comRefs[i].add(txtSISMSR);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Reduced Meal Status Field Type:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingListBox lRMSType = new PSSettingListBox();
                    lRMSType.addItem("String");
                    lRMSType.addItem("Number");
                    lRMSType.setSettingID(DBSettings.SIS_REDUCEDTYPE);
                    if (blSet) {
                        if (settings.get(DBSettings.SIS_REDUCEDTYPE).compareTo("Number") == 0)
                            lRMSType.setSelectedIndex(1);
                    }
                    pnls[i].add(lRMSType, c);
                    comRefs[i].add(lRMSType);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student Status Field:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISStatus = new PSSettingTextField(25);
                    txtSISStatus.setSettingID(DBSettings.SIS_STATUSFIELD);
                    if (blSet) txtSISStatus.setText(settings.get(DBSettings.SIS_STATUSFIELD));
                    pnls[i].add(txtSISStatus, c);
                    comRefs[i].add(txtSISStatus);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Status value indicating active:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISStatusActive = new PSSettingTextField(25);
                    txtSISStatusActive.setSettingID(DBSettings.SIS_STATUSVALUE);
                    if (blSet) txtSISStatusActive.setText(settings.get(DBSettings.SIS_STATUSVALUE));
                    pnls[i].add(txtSISStatusActive, c);
                    comRefs[i].add(txtSISStatusActive);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Status Field Type:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingListBox lStatType = new PSSettingListBox();
                    lStatType.addItem("String");
                    lStatType.addItem("Number");
                    lStatType.setSettingID(DBSettings.SIS_STATUSTYPE);
                    if (blSet) {
                        if (settings.get(DBSettings.SIS_STATUSTYPE).compareTo("Number") == 0)
                            lStatType.setSelectedIndex(1);
                    }
                    pnls[i].add(lStatType, c);
                    comRefs[i].add(lStatType);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Allow only active students:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISActiveStudents = new PSSettingCheckbox();
                    chkSISActiveStudents.setSettingID(DBSettings.SIS_ALLOWONLYACTIVE);
                    if (blSet)
                        chkSISActiveStudents.setSelected(settings.get(DBSettings.SIS_ALLOWONLYACTIVE).compareTo("1") == 0);
                    pnls[i].add(chkSISActiveStudents, c);
                    comRefs[i].add(chkSISActiveStudents);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Allow only students that exist:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISExistant = new PSSettingCheckbox();
                    chkSISExistant.setSettingID(DBSettings.SIS_ALLOWONLYSTUDENTSTHATEXIST);
                    if (blSet)
                        chkSISExistant.setSelected(settings.get(DBSettings.SIS_ALLOWONLYSTUDENTSTHATEXIST).compareTo("1") == 0);
                    pnls[i].add(chkSISExistant, c);
                    comRefs[i].add(chkSISExistant);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student ID's fixed width:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISFixed = new PSSettingCheckbox();
                    chkSISFixed.setSettingID(DBSettings.SIS_NUMBERFIXEDWIDTH);
                    if (blSet)
                        chkSISFixed.setSelected(settings.get(DBSettings.SIS_NUMBERFIXEDWIDTH).compareTo("1") == 0);
                    pnls[i].add(chkSISFixed, c);
                    comRefs[i].add(chkSISFixed);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student fixed ID length:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISFixedIDWidth = new PSSettingTextField(25);
                    txtSISFixedIDWidth.setSettingID(DBSettings.SIS_FIXEDWIDTHSIZE);
                    if (blSet) txtSISFixedIDWidth.setText(settings.get(DBSettings.SIS_FIXEDWIDTHSIZE));
                    pnls[i].add(txtSISFixedIDWidth, c);
                    comRefs[i].add(txtSISFixedIDWidth);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Student Image URL:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtSISImageURL = new PSSettingTextField(25);
                    txtSISImageURL.setSettingID(DBSettings.SIS_IMAGEPATH);
                    if (blSet) txtSISImageURL.setText(settings.get(DBSettings.SIS_IMAGEPATH));
                    pnls[i].add(txtSISImageURL, c);
                    comRefs[i].add(txtSISImageURL);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("SIS Registration - Allow spaces in Student ID:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkSISSpaces = new PSSettingCheckbox();
                    chkSISSpaces.setSettingID(DBSettings.SIS_ALLOWSPACES);
                    if (blSet) chkSISSpaces.setSelected(settings.get(DBSettings.SIS_ALLOWSPACES).compareTo("1") == 0);
                    pnls[i].add(chkSISSpaces, c);
                    comRefs[i].add(chkSISSpaces);
                    break;
                case 4:
                    pnls[i].setLayout(new GridBagLayout());

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Enable Cash Drawer Support:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingCheckbox chkCashEnabled = new PSSettingCheckbox();
                    chkCashEnabled.setSettingID(DBSettings.DRAWER_ENABLED);
                    if (blSet) chkCashEnabled.setSelected(settings.get(DBSettings.DRAWER_ENABLED).compareTo("1") == 0);
                    pnls[i].add(chkCashEnabled, c);
                    comRefs[i].add(chkCashEnabled);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Cash Drawer Class Name:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtCashClass = new PSSettingTextField(25);
                    txtCashClass.setSettingID(DBSettings.DRAWER_CLASS);
                    if (blSet) txtCashClass.setText(settings.get(DBSettings.DRAWER_CLASS));
                    pnls[i].add(txtCashClass, c);
                    comRefs[i].add(txtCashClass);

                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("Cash Drawer Common Name:"), c);

                    c.gridx = 1;
                    c.gridy = y++;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    PSSettingTextField txtCashCommon = new PSSettingTextField(25);
                    txtCashCommon.setSettingID(DBSettings.DRAWER_COMMONNAME);
                    if (blSet) txtCashCommon.setText(settings.get(DBSettings.DRAWER_COMMONNAME));
                    pnls[i].add(txtCashCommon, c);
                    comRefs[i].add(txtCashCommon);
                    break;
                case 5:
                    pnls[i].setLayout(new GridBagLayout());

                    c = new GridBagConstraints();

                    c.insets = new Insets(15, 15, 15, 15);
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    c.weightx = c.weighty = 0;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    pnls[i].add(new JLabel("<html>Your configuration is now complete!<br>To save your settings, click on the button below.<br><br>Note:  The configuration file can be copied to other POS to ease deployement.<br>For more information, documentation or support please go to <br>http://pscafe.sourceforge.net</html>"), c);

                    JButton btnFinish = new JButton("Finish Configuration");
                    ActionListener finLis = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            fireSettingEvent(SettingEvent.FINISH, comRefs);
                        }
                    };
                    btnFinish.addActionListener(finLis);

                    c.gridx = 0;
                    c.gridy = 1;
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    c.anchor = GridBagConstraints.SOUTH;
                    pnls[i].add(btnFinish);
                    break;
            }
        }
    }

    public void popUpMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "PSCafePOS Configuration Wizard", JOptionPane.INFORMATION_MESSAGE);
    }

    public void loadGUI() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        root = getContentPane();
        setLayout(new BorderLayout());

        scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(pnlTitle, BorderLayout.NORTH);
        add(pnlStatus, BorderLayout.SOUTH);
        add(pnlMiddle);

        btnNext = new JButton("Next");
        ActionListener nextLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireSettingEvent(SettingEvent.NEXT);
            }
        };
        btnNext.addActionListener(nextLis);

        btnBack = new JButton("Back");
        ActionListener backLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireSettingEvent(SettingEvent.BACK);
            }
        };
        btnBack.addActionListener(backLis);

        pnlButtons.add(btnBack);
        pnlButtons.add(btnNext);
        pnlMiddle.add(pnlButtons, BorderLayout.SOUTH);
        pnlMiddle.add(scroll);

        setSize(700, 600);

        setVisible(true);

        validate();
        regenerateScreens(null);
        loadScreen(0);
        setStatus("Setting Wizard Loaded.");
    }

    public void pushData(DBSettings settings) {
        if (settings != null) {
            setStatus("Loading settings file...");
            // do screen number 1

            regenerateScreens(settings);

            setStatus("Settings loaded. ");
        }
    }

    public void loadScreen(int screenNum) {
        if (screenNum >= 0 && screenNum < pnls.length) {
            scroll.setViewportView(pnls[screenNum]);
            setMessage(strMessages[screenNum]);
            setStatus(strStatus[screenNum]);
            intCurrentScreen = screenNum;
        }

        if (screenNum != 0)
            btnBack.setEnabled(true);

        if (screenNum != pnls.length - 1)
            btnNext.setEnabled(true);

    }

    public void nextScreen() {
        if (intCurrentScreen + 1 >= 0 && intCurrentScreen + 1 < pnls.length) {
            loadScreen(intCurrentScreen + 1);

            if (intCurrentScreen == (pnls.length - 1))
                btnNext.setEnabled(false);
        }
    }

    public void lastScreen() {
        if (intCurrentScreen - 1 >= 0 && intCurrentScreen - 1 < pnls.length) {
            loadScreen(intCurrentScreen - 1);

            if (intCurrentScreen == 0)
                btnBack.setEnabled(false);
        }
    }

    public void setMessage(String msg) {
        lblTitle.setText(MESSAGEPREFIX + msg);
    }

    public void setStatus(String stat) {
        lblStatus.setText(stat + "");
    }

    public void addSettingEventListener(SettingEventListener listener) {
        posEventListenerList.add(listener);
    }

    public void removeSettingEventListener(SettingEventListener listener) {
        posEventListenerList.remove(listener);
    }

    private void fireSettingEvent(int eventType) {
        SettingEvent posEvent = new SettingEvent(this, eventType);

        for (Object aPosEventListenerList : posEventListenerList) {
            ((SettingEventListener) aPosEventListenerList).settingEventOccurred(posEvent);
        }

    }

    private void fireSettingEvent(int eventType, Object sender) {
        SettingEvent posEvent = new SettingEvent(sender, eventType);

        for (Object aPosEventListenerList : posEventListenerList) {
            ((SettingEventListener) aPosEventListenerList).settingEventOccurred(posEvent);
        }

    }
}
