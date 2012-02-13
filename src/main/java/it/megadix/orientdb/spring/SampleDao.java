package it.megadix.orientdb.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Component
public class SampleDao extends OrientDbDaoSupport {
    
    public List<String> read() {
        List<ODocument> queryResult = database.query(new OSQLSynchQuery<ODocument>("select * from AnimalRace"));
        List<String> result = new ArrayList<String>(queryResult.size());

        for (ODocument doc : queryResult) {
            result.add(doc.field("name").toString());
        }

        return result;
    }
}
