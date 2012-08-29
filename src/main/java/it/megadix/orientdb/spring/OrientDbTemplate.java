package it.megadix.orientdb.spring;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

/**
 * Helper class that simplifies OrientDb data access code.
 */
public class OrientDbTemplate {

    public OrientDbTemplate() {
    }

    public ODatabaseRecord getDatabase() {
        return ODatabaseRecordThreadLocal.INSTANCE.get();
    }

    public <T> T execute(OrientDbCallback<T> action) {
        return action.doInOrientDb(getDatabase());
    }

}
