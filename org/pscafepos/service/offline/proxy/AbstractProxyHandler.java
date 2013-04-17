package org.pscafepos.service.offline.proxy;

import org.pscafepos.service.offline.watchdog.connection.IWatchDog;
import org.pscafepos.backends.database.dao.BaseJDBCDao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

/**
 * @author bagmanov
 */
public abstract class AbstractProxyHandler<T extends BaseJDBCDao> implements InvocationHandler {
    protected IWatchDog dog;
    protected T dao;

    public AbstractProxyHandler(IWatchDog dog, T dao) {
        this.dog = dog;
        this.dao = dao;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Connection connection = dog.getCurrentConnection();
        dao.setConnection(connection);
        updateDao(dog.isOfflineMode(connection));
        try {
            return method.invoke(dao, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            dog.releaseConnection(connection);
        }
    }

    public abstract void updateDao(boolean isOfflineMode);
}
