package it.megadix.orientdb.spring;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import org.springframework.util.StringUtils;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class OrientDbPoolDocumentTransactionManager extends AbstractPlatformTransactionManager implements
        InitializingBean, DisposableBean {

    private static final long serialVersionUID = -1837116040878560582L;

    private ODatabaseDocumentPool pool;
    private String databaseURL;
    private String username;
    private String password;
    private int minConnections = 1;
    private int maxConnections = 20;

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(databaseURL)) {
            throw new IllegalArgumentException("'databaseURL' is required");
        }
        if (minConnections > maxConnections) {
            throw new IllegalArgumentException("minConnections > maxConnections");
        }

        pool = new ODatabaseDocumentPool(databaseURL, username, password);
        pool.setup(minConnections, maxConnections);
    }

    public void destroy() throws Exception {
        this.pool.close();
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        TransactionHolder holder = (TransactionHolder) TransactionSynchronizationManager.getResource(ODatabase.class);
        if (holder == null) {
            // create and register new transaction
            ODatabaseDocumentTx database = pool.acquire();
            ODatabaseRecordThreadLocal.INSTANCE.set(database);
            holder = new TransactionHolder(database);
            TransactionSynchronizationManager.bindResource(ODatabase.class, holder);
        }

        return holder;
    }

    @Override
    protected void doBegin(Object transactionObject, TransactionDefinition definition) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) transactionObject;
        holder.getDatabase().getTransaction().begin();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) status.getTransaction();
        try {
            holder.getDatabase().getTransaction().commit();
            releaseConnecton(holder);
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) status.getTransaction();
        try {
            holder.getDatabase().getTransaction().rollback();
            releaseConnecton(holder);
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }

    protected void releaseConnecton(TransactionHolder holder) {
        holder.getDatabase().close();
        TransactionSynchronizationManager.unbindResource(ODatabase.class);
    }
}
