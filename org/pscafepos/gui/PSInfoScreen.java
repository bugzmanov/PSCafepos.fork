package org.pscafepos.gui;

import org.pscafepos.event.POSEventListener;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.*;

import org.pscafepos.gui.swing.components.JWrapLabel;

/**
 * @author Bagmanov
 *         Date: Sep 9, 2009
 */
public class PSInfoScreen implements MouseListener {
    private PSInfoWindow win;
    private POSEventListener posEventListener;

    public void mouseClicked(MouseEvent e) {
        kill();
    }

    public void mousePressed(MouseEvent e) {
        kill();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public PSInfoScreen(Frame parent) {
        win = new PSInfoWindow(parent);

        JPanel pan = new JPanel();
        pan.setOpaque(false);

        win.getContentPane().add(pan, "Center");
        win.addMouseListener(this);

        pan.setLayout(new FlowLayout());

        //pan.setBackground( Color.WHITE );


    }

    public void display(String msg) {
        JPanel b = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint redtowhite = new GradientPaint(0, 0, new Color(229, 229, 229), 200, 250, Color.GRAY.brighter());
                g2d.setPaint(redtowhite);
                g2d.fill(g.getClipBounds());
            }
        };

        JWrapLabel wrap = new JWrapLabel(msg);
        wrap.setOpaque(false);
        wrap.addMouseListener(this);

        JLabel lblNorth = new JLabel("PSCafePOS Message", SwingConstants.CENTER);
        lblNorth.setOpaque(true);
        lblNorth.setBackground(Color.WHITE);
        b.add(lblNorth, BorderLayout.NORTH);

        JLabel lblSouth = new JLabel("Touch or Click to Dispose", SwingConstants.CENTER);
        lblSouth.setOpaque(true);
        lblSouth.setBackground(Color.WHITE);
        b.add(lblSouth, BorderLayout.SOUTH);
        b.add(wrap);

        b.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        win.add(b);
        win.setVisible(true);
        win.repaint();
    }

    public void kill() {
        win.setVisible(false);
        win.dispose();
    }
}

class PSInfoWindow extends JWindow {
    public PSInfoWindow(Frame parent) {
        super(parent);
        setSize(400, 250);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);
    }
}