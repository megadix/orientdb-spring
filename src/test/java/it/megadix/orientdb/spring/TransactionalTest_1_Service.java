package it.megadix.orientdb.spring;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Service
public class TransactionalTest_1_Service extends OrientDbDaoSupport {

    PlatformTransactionManager transactionManager;
    TransactionTemplate template;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.template = new TransactionTemplate(transactionManager);
    }

    public void test_A(final CyclicBarrier barrier, TransactionalTest_1 test_1) {

        TransactionCallback<Object> tcb = new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                try {
                    assertEquals(0, countDocuments());
                    // insert one document
                    assertNotNull(insertDocument("Thread A"));
                    assertEquals(1, countDocuments());
                    // wait at barrier while thread B inserts another document
                    barrier.await();
                    // count must be == 1, because transactions must be isolated
                    assertEquals(1, countDocuments());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        };

        template.execute(tcb);
    }

    public void test_B(final CyclicBarrier barrier, TransactionalTest_1 test_1) {

        TransactionCallback<Object> tcb = new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                try {
                    // wait at barrier while thread A inserts a document
                    barrier.await();
                    // count must be == 0, because transactions must be isolated
                    assertEquals(0, countDocuments());
                    // insert one document
                    assertNotNull(insertDocument("Thread B"));
                    // count must be == 1, because transactions must be isolated
                    assertEquals(1, countDocuments());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        };

        template.execute(tcb);
    }

    private long countDocuments() {
        OrientDbCallback<Long> action = new OrientDbCallback<Long>() {
            @Override
            public Long doInOrientDb(ODatabaseRecord database) {
                OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
                        "select count(*) from TransactionalTest_1");
                List<ODocument> result = database.command(query).execute();
                Number count = result.get(0).field("count");
                return count.longValue();
            }
        };

        long count = getOrientDbTemplate().execute(action);

        return count;
    }

    private ODocument insertDocument(final String name) {
        OrientDbCallback<ODocument> action = new OrientDbCallback<ODocument>() {
            @Override
            public ODocument doInOrientDb(ODatabaseRecord database) {
                ODocument doc = new ODocument("TransactionalTest_1");
                doc.field("name", name);
                doc.save();
                return doc;
            }
        };

        ODocument doc = getOrientDbTemplate().execute(action);

        return doc;
    }
}