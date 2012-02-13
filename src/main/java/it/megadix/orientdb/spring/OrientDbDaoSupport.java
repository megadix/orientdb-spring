package it.megadix.orientdb.spring;

import org.springframework.beans.factory.annotation.Autowired;

import com.orientechnologies.orient.core.db.ODatabaseComplex;

public class OrientDbDaoSupport {
    
    protected ODatabaseComplex database;
    
    @Autowired
    public void setDatabase(ODatabaseComplex database) {
        this.database = database;
    }
    
    public ODatabaseComplex getDatabase() {
        return database;
    }
}
