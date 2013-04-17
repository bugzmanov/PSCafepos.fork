package org.pscafepos.gui.swing;

import static org.pscafepos.util.StringUtils.isEmpty;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author bagmanov
 */
public class EventQueueProxy extends EventQueue {
    Logger logger = Logger.getLogger(EventQueueProxy.class.getName());

    protected void dispatchEvent(AWTEvent newEvent) {
        try {
            super.dispatchEvent(newEvent);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);

            StringBuilder message = new StringBuilder("Unexpected error:\n ");

            if (isEmpty(t.getMessage())) {
                message.append("caused by ").append(t.getClass());
            } else {
                message.append(t.getMessage());
            }
            message.append("\n\nPlease refer to log for clarifications");
            JOptionPane.showMessageDialog(null, message, "General Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
