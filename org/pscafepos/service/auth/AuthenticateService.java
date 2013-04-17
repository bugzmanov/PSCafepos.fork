package org.pscafepos.service.auth;

import org.pscafepos.configuration.SessionSettings;

/**
 * @author bagmanov
 *         Date: 31.08.2009
 */
public interface AuthenticateService {
    SessionSettings auth(String userName, String password) throws AuthFailedException;

    public static enum AuthenticateResult {
        REMOTE, LOCAL, FAIlED
    }
}
