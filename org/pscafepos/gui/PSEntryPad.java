package org.pscafepos.gui;

import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.pscafepos.gui.swing.components.POSButton;
import org.pscafepos.gui.swing.components.POSKeyPad;
import org.pscafepos.gui.swing.components.JWrapLabel;

/**
 * @author Bagmanov
 *         Date: Sep 9, 2009
 */
public class PSEntryPad {
    private PSKeyEntryWindow win;
    private POSEventListener posEventListener;
    private JWrapLabel wrap;
    private POSKeyPad keyPad;
    private POSButton bRef;
    private JPanel pan;

    private POSEventListener posEventListner;

    public PSEntryPad(Frame parent, POSButton prototype) {
        win = new PSKeyEntryWindow(parent);

        pan = new JPanel();
        pan.setOpaque(false);

        win.getContentPane().add(pan, "Center");

        pan.setLayout(new FlowLayout());
        pan.setOpaque(true);
        pan.setBackground(Color.WHITE);

        bRef = prototype;

        keyPad = new POSKeyPad(prototype, POSKeyPad.NUMBERMODE);

    }

    public void addPOSEventListener(POSEventListener listener) {
        posEventListner = listener;
    }

    public POSKeyPad getKeyPad() {
        return keyPad;
    }

    private void firePOSEvent(int eventType) {
        POSEvent posEvent = new POSEvent(this, eventType);
        if (posEventListner != null)
            posEventListner.onPosEvent(posEvent);

    }

    public void display() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(keyPad);
        Box buttonsBox = new Box(BoxLayout.X_AXIS);
        buttonsBox.setOpaque(false);
        buttonsBox.add(Box.createRigidArea(new Dimension(30, 80)));

        POSButton submitButton = getButton();
        submitButton.setText("OK");
        ActionListener addLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firePOSEvent(POSEvent.ITEMS_MANUALENTRYSUBMIT);
            }
        };
        submitButton.addActionListener(addLis);
        buttonsBox.add(submitButton);

        buttonsBox.add(Box.createHorizontalStrut(20));

        POSButton cancelButton = getButton();
        cancelButton.setText("Cancel");
        ActionListener killLis = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kill();
            }
        };
        cancelButton.addActionListener(killLis);
        buttonsBox.add(cancelButton);

        panel.add(buttonsBox, BorderLayout.SOUTH);

        pan.add(panel);
        win.setVisible(true);
        win.repaint();
    }

    public void kill() {
        win.setVisible(false);
        win.dispose();
    }

    private POSButton getButton() {
        POSButton org = bRef;
        Dimension dim = org.getPreferredSize();
        POSButton b = new POSButton("", (int) dim.getWidth(), (int) dim.getHeight());
        b.setPreferredSize(org.getPreferredSize());
        b.setFont(org.getFont());
        return b;
    }
}

class PSKeyEntryWindow extends JWindow {

    public PSKeyEntryWindow(Frame parent) {
        super(parent);
        setSize(300, 340);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);
    }
}