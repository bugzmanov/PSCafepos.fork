package org.pscafepos.service.offline.repilcator;

/**
 * @author bagmanov
 */
public class ReplicatorConnectionException extends ReplicatorException{
    public ReplicatorConnectionException(String message) {
        super(message);
    }

    public ReplicatorConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
