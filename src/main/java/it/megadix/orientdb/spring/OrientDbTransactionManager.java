package it.megadix.orientdb.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.orientechnologies.orient.core.db.*;
import com.orientechnologies.orient.core.db.object.ODatabaseObject;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.tx.OTransaction;

public class OrientDbTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean,
        DisposableBean {

    private static final long serialVersionUID = -1837116040878560582L;

    private ODatabaseComplex database;
    private String username;
    private String password;
    private List<String> entityClassesPackages = new ArrayList<String>();
    private boolean createIfNotExists;

    @Autowired
    public void setDatabase(ODatabaseComplex database) {
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
            ODatabaseObject objectDb = (ODatabaseObject) database;
            OEntityManager entMgr = objectDb.getEntityManager();
            for (String entityPackage : entityClassesPackages) {
                entMgr.registerEntityClasses(entityPackage);
            }
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
        return ODatabaseRecordThreadLocal.INSTANCE.get().getTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        OTransaction tx = (OTransaction) transaction;
        tx.begin();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        OTransaction tx = (OTransaction) status.getTransaction();
        try {
            tx.commit();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        OTransaction tx = (OTransaction) status.getTransaction();
        try {
            tx.rollback();
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback OrientDB transaction", ex);
        }
    }
}
