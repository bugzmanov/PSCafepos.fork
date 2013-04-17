package org.pscafepos.service.offline.watchdog.connection;

/**
 * @author Bagmanov
 *         Date: 09.09.2009
 */
public interface WatchDogEventHandler {

    public void onOfflineMode();

    public void onOnlineMode();
}
