package it.megadix.orientdb.spring;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.OStorage.CLUSTER_TYPE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@Transactional
public class TransactionalTest_1 {
    static final String DB_PATH = "databases/TransactionalTest_1";

    @Configuration
    static class ContextConfiguration {

        ODatabaseComplex database;

        @Bean
        ODatabaseComplex database() throws Exception {
            if (database == null) {
                // delete database from previous runs (if any)
                File dbDir = new File(DB_PATH);
                if (dbDir.exists()) {
                    FileUtils.cleanDirectory(dbDir);
                }
                database = new ODatabaseDocumentTx("local:" + DB_PATH);
                if (!database.exists()) {
                    database.create();
                }
                database.getMetadata().getSchema().createClass("TransactionalTest_1");
            }
            return database;
        }

        @Bean
        PlatformTransactionManager transactionManager() throws Exception {
            return new OrientDbTransactionManager();
        }

        @Bean
        MyTransactionalService myTransactionalService() {
            return new MyTransactionalService();
        }
    }

    /* ------------------------------------------------------- */

    @Autowired
    MyTransactionalService service;

    /* ------------------------------------------------------- */

    @Test
    public void test_concurrent_write_read() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);

        Runnable runnable_A = new Runnable() {
            @Override
            public void run() {
                try {
                    assertEquals(0, service.countDocuments());
                    // insert one document
                    assertNotNull(service.insertDocument("Thread A"));
                    assertEquals(1, service.countDocuments());
                    // wait at barrier while thread B inserts another document
                    barrier.await();
                    // count must be == 1, because transactions must be isolated
                    assertEquals(1, service.countDocuments());

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        Runnable runnable_B = new Runnable() {
            @Override
            public void run() {
                try {
                    // wait at barrier while thread A inserts a document
                    barrier.await();

                    // count must be == 0, because transactions must be isolated
                    assertEquals(0, service.countDocuments());

                    // insert one document
                    assertNotNull(service.insertDocument("Thread B"));

                    // count must be == 1, because transactions must be isolated
                    assertEquals(1, service.countDocuments());

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        
        Future future_A =  executorService.submit(runnable_A);
        Future future_B =  executorService.submit(runnable_B);
        assertNull(future_A.get());
        assertNull(future_B.get());

        executorService.shutdown();
    }

    /* ------------------------------------------------------- */
}

@Transactional
class MyTransactionalService extends OrientDbDaoSupport {

    public long countDocuments() {
        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select count(*) from TransactionalTest_1");
        List<ODocument> result = database.command(query).execute();
        Number count = result.get(0).field("count");
        return count.longValue();
    }

    public ODocument insertDocument(String name) {
        ODocument doc = new ODocument("TransactionalTest_1");
        doc.field("name", name);
        doc.save();
        return doc;
    }
}