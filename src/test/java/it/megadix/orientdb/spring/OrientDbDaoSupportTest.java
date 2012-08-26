package it.megadix.orientdb.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class OrientDbDaoSupportTest {

    static final String DB_PATH = "databases/OrientDbDaoSupportTest";
    
    @Configuration
    static class ContextConfiguration {
        @Bean
        ODatabase orientDbDatabase() throws Exception {
            ODatabase database = new ODatabaseDocumentTx("local:" + DB_PATH);
            if (!database.exists()) {
                database.create();
            }
            return database;
        }

        @Bean
        DummyOrientDbDaoSupport dummyOrientDbDaoSupport() {
            return new DummyOrientDbDaoSupport();
        }
    }

    @Autowired
    DummyOrientDbDaoSupport dao;

    @Test
    public void test_autowire() {
        assertNotNull(dao);
        assertNotNull(dao.getDatabase());
        assertTrue(dao.getDatabase().exists());
    }
}

class DummyOrientDbDaoSupport extends OrientDbDaoSupport {
    // dummy implementation
}