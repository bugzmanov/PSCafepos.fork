package org.pscafepos.gui;

import org.pscafepos.event.POSEventListener;
import org.pscafepos.event.POSEvent;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * @author Bagmanov
 *         Date: Sep 9, 2009
 */
public class PSOrderSummary implements MouseListener {
    private PSWindow win;
    private POSEventListener posEventListener;

    public void mouseClicked(MouseEvent e) {
        firePOSEvent(POSEvent.CHECKOUT_SUMMARYSCREENCLOSED);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public PSOrderSummary(Frame parent) {
        win = new PSWindow(parent);

        JPanel pan = new JPanel();

        win.getContentPane().add(pan, "Center");
        win.addMouseListener(this);
        pan.setLayout(new FlowLayout());
        pan.setBackground(Color.WHITE);
        pan.setOpaque(true);

    }

    private void firePOSEvent(int eventType) {
        if (posEventListener != null) {
            POSEvent posEvent = new POSEvent(this, eventType);

            posEventListener.onPosEvent(posEvent);
        }

    }

    public void setPOSEventListener(POSEventListener listener) {
        posEventListener = listener;
    }

    public void display(String total, String cashPaid, String creditPaid, String change, String remainingCredit) {
        win.setValues(total, cashPaid, creditPaid, change, remainingCredit);
        win.setVisible(true);
        win.repaint();
    }

    public void kill() {
        win.setVisible(false);
        win.dispose();
    }
}

class PSWindow extends JWindow {

    String t = "";
    String cp = "";
    String crp = "";
    String ch = "";
    String rmcr = "";

    public PSWindow(Frame parent) {
        super(parent);
        setSize(400, 250);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);
    }

    public void setValues(String total, String cashPaid, String creditPaid, String change, String remainingCredit) {
        t = total;
        cp = cashPaid;
        crp = creditPaid;
        ch = change;
        rmcr = remainingCredit;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint bg = new GradientPaint(0, 0, new Color(229, 229, 229), 200, 250, Color.GRAY.brighter());
        Font title = new Font("Arial", Font.BOLD, 14);
        Font bld = new Font("Arial", Font.BOLD, 12);
        Font nrm = new Font("Arial", Font.PLAIN, 12);
        Font chg = new Font("Arial", Font.BOLD, 14);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.clipRect(5, 5, getWidth() - 8, getHeight() - 8);

        g2d.setPaint(bg);
        g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);

        g2d.setPaint(Color.BLACK);
        g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);

        g2d.setFont(title);
        g2d.drawString("Order Summary", getWidth() / 2 - 50, 25);

        g2d.setFont(nrm);
        g2d.drawString("Order Total:", 35, 50);
        g2d.drawString("Cash Paid:", 35, 70);
        g2d.drawString("Credit Paid:", 35, 90);
        g2d.drawString("Remaining Credit:", 35, 110);

        g2d.drawString("Change Due:", 35, 150);

        g2d.setFont(bld);
        g2d.drawString(t, 200, 50);
        g2d.drawString(cp, 200, 70);
        g2d.drawString(crp, 200, 90);
        g2d.drawString(rmcr, 200, 110);

        g2d.setPaint(Color.GREEN.darker());
        g2d.setFont(chg);
        g2d.drawString(ch, 200, 150);
    }
}