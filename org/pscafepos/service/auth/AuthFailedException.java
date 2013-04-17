package org.pscafepos.service.auth;

/**
 * @author bagmanov
 *         Date: 07.09.2009
 */
public class AuthFailedException extends Exception {
    public AuthFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthFailedException(Throwable cause) {
        super(cause);
    }

    public AuthFailedException(String cause) {
        super(cause);
    }
}
