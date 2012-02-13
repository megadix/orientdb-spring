package it.megadix.orientdb.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SampleService extends OrientDbDaoSupport {

    private SampleDao sampleDao;

    @Autowired
    public void setSampleDao(SampleDao sampleDao) {
        this.sampleDao = sampleDao;
    }

    public List<String> read() {
        return sampleDao.read();
    }
}
