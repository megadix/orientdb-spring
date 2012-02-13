package it.megadix.orientdb.spring;

import org.springframework.transaction.support.ResourceHolderSupport;

import com.orientechnologies.orient.core.db.ODatabaseComplex;

public class OrientDbConnectionHolder extends ResourceHolderSupport {
    
    private final ODatabaseComplex database;

    public OrientDbConnectionHolder(ODatabaseComplex database) {
        super();
        this.database = database;
    }

    public ODatabaseComplex getDatabase() {
        return database;
    }
}
