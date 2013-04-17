package org.pscafepos.gui.util;

import org.pscafepos.gui.POSGUI;
import org.pscafepos.backends.database.jdbc.JdbcConnector;

/**
 * @author bagmanov
 */
public class OrderItemsSizeResolver implements Runnable {
    private POSGUI posgui;
    JdbcConnector connector;

    public OrderItemsSizeResolver(POSGUI posgui) {
        this.posgui = posgui;
    }

    public void run() {

    }
}
