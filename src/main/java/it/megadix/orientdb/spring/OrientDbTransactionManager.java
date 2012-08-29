package it.megadix.orientdb.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.object.ODatabaseObject;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.entity.OEntityManager;

public class OrientDbTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean,
        DisposableBean {

    private static final long serialVersionUID = -1837116040878560582L;

    private ODatabaseRecord database;
    private String username;
    private String password;
    private List<String> entityClassesPackages = new ArrayList<String>();
    private boolean createIfNotExists;

    @Autowired
    public void setDatabase(ODatabaseRecord database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreateIfNotExists(boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    public boolean isCreateIfNotExists() {
        return createIfNotExists;
    }

    public void afterPropertiesSet() throws Exception {
        if (!database.exists()) {
            if (createIfNotExists) {
                database.create();
            } else {
                throw new IllegalStateException("Database does not exist");
            }
        } else {
            if (database.isClosed()) {
                database.open(username, password);
            }
        }

        if (!entityClassesPackages.isEmpty() && ODatabaseObject.class.isAssignableFrom(database.getClass())) {
            throw new UnsupportedOperationException("object database support not implemented!");
        }
    }

    public void destroy() throws Exception {
        this.database.close();
    }

    public List<String> getEntityClassesPackages() {
        return entityClassesPackages;
    }

    public void setEntityClassesPackages(List<String> entityClassesPackages) {
        this.entityClassesPackages = entityClassesPackages;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        TransactionHolder holder = (TransactionHolder) TransactionSynchronizationManager.getResource(database);

        if (holder == null) {
            // create and register new transaction
            holder = new TransactionHolder(database);
            ODatabaseRecordThreadLocal.INSTANCE.set(database);
        }

        holder.setTransaction(ODatabaseRecordThreadLocal.INSTANCE.get().getTransaction());

        return holder;
    }

    @Override
    protected void doBegin(Object transactionObject, TransactionDefinition definition) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) transactionObject;
        holder.getTransaction().begin();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) status.getTransaction();
        try {
            holder.getTransaction().commit();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        TransactionHolder holder = (TransactionHolder) status.getTransaction();
        try {
            holder.getTransaction().rollback();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }
}
