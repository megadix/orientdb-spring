package it.megadix.orientdb.spring;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
public abstract class AbstractTest extends AbstractJUnit4SpringContextTests {

}
