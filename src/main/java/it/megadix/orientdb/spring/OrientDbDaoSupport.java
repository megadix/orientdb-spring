package it.megadix.orientdb.spring;

import org.springframework.beans.factory.annotation.Autowired;

import com.orientechnologies.orient.core.db.ODatabaseComplex;

public abstract class OrientDbDaoSupport {

    protected ODatabaseComplex database;

    @Autowired
    public final void setDatabase(ODatabaseComplex database) {
        this.database = database;
    }

    protected final ODatabaseComplex getDatabase() {
        return database;
    }
}
