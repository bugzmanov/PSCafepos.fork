package org.pscafepos.service.auth;

import org.pscafepos.configuration.SessionSettings;
import org.pscafepos.util.StringUtils;

import static org.pscafepos.util.Utils.SHA1;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.FileReader;

import org.pscafepos.backends.database.jdbc.JdbcConnectorImpl;
import org.pscafepos.backends.database.jdbc.JdbcUtils;
import org.pscafepos.backends.database.jdbc.exception.JdbcConnectorException;
import org.pscafepos.backends.database.jdbc.exception.JdbcAuthException;

/**
 * @author bagmanov
 */
public class AuthenticateServiceImpl implements AuthenticateService {

    private static final Logger logger = Logger.getLogger(AuthenticateServiceImpl.class.getName());
    private static final String LOCAL_CREDENTIALS_FILE = "etc/local.cred";

    private Properties localCredentials;
    private SessionSettings settings;
    private static final String AUTHENTICATION_FAILED_ERROR = "2800";

    public AuthenticateServiceImpl(SessionSettings settings) {
        this.settings = settings;
        localCredentials = loadCredentials();
    }

    public SessionSettings auth(String userName, String password) throws AuthFailedException {
        Connection connection = null;
        try {
            settings.getPosSettings().setUserName(userName);
            settings.getPosSettings().setPassword(password);
            JdbcConnectorImpl connector = new JdbcConnectorImpl("", settings.getPosSettings());
            connection = connector.getNewConnection();
            udpateLocalCredentials(userName, password);
            return settings;
        } catch (JdbcAuthException e) {
            throw new AuthFailedException(e);
        } catch (JdbcConnectorException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (authLocal(userName, password)) {
                return settings;
            } else {
                throw new AuthFailedException("Local Auth failed either");
            }
        } finally {
            JdbcUtils.closeIfNeededSilently(connection);
        }
    }

    private boolean authLocal(String userName, String password) {
        String encryptedPassword = localCredentials.getProperty(userName);
        return StringUtils.isNotEmpty(encryptedPassword) && encryptedPassword.equals(SHA1(password));
    }

    private void udpateLocalCredentials(String userName, String password) {
        if (StringUtils.isEmpty(localCredentials.getProperty(userName))) {
            localCredentials.setProperty(userName, SHA1(password));
            saveCredentials(localCredentials);
        }

    }

    private Properties loadCredentials() {
        Properties properties = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(LOCAL_CREDENTIALS_FILE);
            properties.load(reader);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't load local credentials", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Couldn't close credentials file", e);
                }
            }
        }
        return properties;
    }

    private void saveCredentials(Properties credentials) {
        Writer fileWriter = null;
        try {
            fileWriter = new FileWriter(LOCAL_CREDENTIALS_FILE);
            credentials.store(fileWriter, "");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't update credentials file", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Couldn't close credentials file after update", e);
                }
            }
        }
    }
}
