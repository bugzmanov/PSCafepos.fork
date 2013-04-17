package org.pscafepos.service.offline.repilcator;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author bagmanov
 */
public class BackendReplicationTimerTask extends TimerTask {

    private static final Logger logger = Logger.getLogger(BackendReplicationTimerTask.class.getName());
    private Replicator replicator;
    private String backendTitle;

    public BackendReplicationTimerTask(Replicator replicator, String backendTitle) {
        this.replicator = replicator;
        this.backendTitle = backendTitle;
    }

    public void run() {
        logger.log(Level.FINE, backendTitle + " replication task started");
        try {
            Date startTime = new Date();
            List<String> unsynchronizedTables = replicator.getUnsynchronizedTablesList();
            if (!unsynchronizedTables.isEmpty()) {
                replicator.replicate(unsynchronizedTables);
            }
            logger.log(Level.FINE, backendTitle + " synchronization finished [tables synchonized:" +
                    (unsynchronizedTables.isEmpty() ? "none (synchronization is not required)" : Arrays.toString(unsynchronizedTables.toArray())) + "" +
                    "]. Taken time: " + (System.currentTimeMillis() - startTime.getTime()) / 1000.0 + " sec");
        } catch (ReplicatorConnectionException ex) {
            logger.log(Level.FINEST, backendTitle + " replication timer task failed", ex);
        } catch (Exception e) {
            logger.log(Level.SEVERE, backendTitle + " replication timer task failed", e);
        }
    }
}
