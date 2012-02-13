package it.megadix.orientdb.spring;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import com.orientechnologies.orient.core.db.ODatabaseComplex;

public class OrientDbTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean,
        DisposableBean {

    private ODatabaseComplex database;
    private String username;
    private String password;

    public void setDatabase(ODatabaseComplex database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void afterPropertiesSet() throws Exception {
        this.database.open(username, password);
    }
    
    public void destroy() throws Exception {
        this.database.close();
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        OrientDbTransactionObject txObject = new OrientDbTransactionObject();
        OrientDbConnectionHolder conHolder = (OrientDbConnectionHolder) TransactionSynchronizationManager
                .getResource(database);
        txObject.setConnectionHolder(conHolder);
        return txObject;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        OrientDbTransactionObject txObject = (OrientDbTransactionObject) transaction;
        ODatabaseComplex database = null;

        if (txObject.getConnectionHolder() == null) {
            // open a new session
            database = this.database.begin();
            txObject.setConnectionHolder(new OrientDbConnectionHolder(database));
        }

        TransactionSynchronizationManager.bindResource(database, txObject.getConnectionHolder());
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        OrientDbTransactionObject txObject = (OrientDbTransactionObject) status.getTransaction();
        ODatabaseComplex database = txObject.getConnectionHolder().getDatabase();
        try {
            database.commit();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not commit OrientDB transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        OrientDbTransactionObject txObject = (OrientDbTransactionObject) status.getTransaction();
        ODatabaseComplex database = txObject.getConnectionHolder().getDatabase();
        try {
            database.rollback();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }
}
