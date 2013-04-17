package org.pscafepos.service.offline.ping;

/**
 * @author bagmanov
 *         Date: 04.09.2009
 */
public interface PingEventHandler {

    public void onConnectionEstablished();

    public void onConnectionLost();
}
