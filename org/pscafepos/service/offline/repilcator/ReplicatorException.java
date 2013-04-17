package org.pscafepos.service.offline.repilcator;

/**
 * @author bagmanov
 */
public class ReplicatorException extends Exception{
    public ReplicatorException(String message) {
        super(message);
    }

    public ReplicatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
