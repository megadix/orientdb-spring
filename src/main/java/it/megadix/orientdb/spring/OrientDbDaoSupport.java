package it.megadix.orientdb.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;

import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

public abstract class OrientDbDaoSupport extends DaoSupport {

    private OrientDbTemplate orientDbTemplate;

    @Autowired
    public final void setDatabase(ODatabaseRecord database) {
        this.orientDbTemplate = createOrientDbTemplate(database);
    }
    
    public final OrientDbTemplate getOrientDbTemplate() {
        return orientDbTemplate;
    }

    protected OrientDbTemplate createOrientDbTemplate(ODatabaseRecord database) {
        OrientDbTemplate tpl = new OrientDbTemplate();
        return tpl;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        if (this.orientDbTemplate == null) {
            throw new IllegalArgumentException("'database' or 'orientDbTemplate' is required");
        }
    }
}
