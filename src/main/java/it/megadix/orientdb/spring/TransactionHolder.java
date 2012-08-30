package it.megadix.orientdb.spring;

/**
 * Transaction holder, wrapping an ODatabaseRecord database and a OTransaction.
 */
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.tx.OTransaction;

public class TransactionHolder {
    private ODatabaseRecord database;

    public TransactionHolder() {
        super();
    }

    public TransactionHolder(ODatabaseRecord database) {
        super();
        this.database = database;
    }

    public ODatabaseRecord getDatabase() {
        return database;
    }

    public void setDatabase(ODatabaseRecord database) {
        this.database = database;
    }
}
