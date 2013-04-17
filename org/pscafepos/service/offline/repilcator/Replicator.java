package org.pscafepos.service.offline.repilcator;

import java.util.List;

/**
 * @author bagmanov
 */
public interface Replicator {
    void replicate(List<String> tables) throws ReplicatorException, ReplicatorConnectionException;

    List<String> getUnsynchronizedTablesList() throws ReplicatorException, ReplicatorConnectionException;
}
