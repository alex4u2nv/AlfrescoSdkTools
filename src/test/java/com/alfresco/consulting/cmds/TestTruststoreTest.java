package com.alfresco.consulting.cmds;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 * Created by alexmahabir on 9/2/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestTruststore.class)
public class TestTruststoreTest extends TestTruststore {


    @Value("${tt.host:www.google.com:443}")
    protected String host;

    @Value("${test.tt.keystore}")
    protected File store;

    @Value("${test.tt.pass}")
    protected String storepass;

    protected String updateCert = "A";

    @Test
    public void testLookup() throws Exception {
        String[] args = new String[]{"tt"};
        System.out.println(getStore().getAbsolutePath());
        this.run(args);

    }


    @Override
    public String getHost() {
        return host;
    }

    @Override
    public File getStore() {
        return store;
    }

    @Override
    public String getStorepass() {
        return storepass;
    }

    @Override
    public String getUpdateCert() {
        return updateCert;
    }
}
