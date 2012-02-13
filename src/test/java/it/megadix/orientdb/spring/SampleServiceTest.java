package it.megadix.orientdb.spring;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SampleServiceTest extends AbstractTest {

    private SampleService myService;

    @Autowired
    public void setMyService(SampleService myService) {
        this.myService = myService;
    }

    @Test
    public void test_read() {
        List<String> result = myService.read();
        assertTrue(result.size() > 0);
        System.out.println(result);
    }

}
