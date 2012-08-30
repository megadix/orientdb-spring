package it.megadix.orientdb.spring;

import static junit.framework.Assert.assertNull;

import java.io.File;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TransactionalTest_1 {
    static final String DB_PATH = "databases/TransactionalTest_1";

    @Configuration
    static class ContextConfiguration {

        ODatabaseRecord database;

        @Bean
        ODatabaseRecord database() throws Exception {
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
            OrientDbPoolDocumentTransactionManager manager = new OrientDbPoolDocumentTransactionManager();
            manager.setDatabaseURL("local:" + DB_PATH);
            manager.setUsername("admin");
            manager.setPassword("admin");
            manager.setMaxConnections(2);

            return manager;
        }

        @Bean
        TransactionalTest_1_Service myTransactionalService() {
            return new TransactionalTest_1_Service();
        }
    }

    /* ------------------------------------------------------- */

    @Autowired
    TransactionalTest_1_Service service;

    /* ------------------------------------------------------- */

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    public void test_concurrent_write_read() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> future_A = executorService.submit(new Runnable() {
            @Override
            public void run() {
                service.test_A(barrier, TransactionalTest_1.this);
            }
        });

        Future<?> future_B = executorService.submit(new Runnable() {
            @Override
            public void run() {
                service.test_B(barrier, TransactionalTest_1.this);
            }
        });

        assertNull(future_A.get());
        assertNull(future_B.get());

        executorService.shutdown();
    }

    /* ------------------------------------------------------- */
    
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ContextConfiguration.class);
        ctx.scan(TransactionalTest_1.class.getPackage().getName());

        TransactionalTest_1 test = new TransactionalTest_1();
        test.service = ctx.getBean(TransactionalTest_1_Service.class);
        
        try {
            test.test_concurrent_write_read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ctx.destroy();
    }
}