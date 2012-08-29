package it.megadix.orientdb.spring;

import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

/**
 * Callback interface for OrientDb code. To be used with {@link OrientDbTemplate}.
 */
public interface OrientDbCallback<T> {
    T doInOrientDb(ODatabaseRecord database);
}
