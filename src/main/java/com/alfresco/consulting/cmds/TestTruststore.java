package com.alfresco.consulting.cmds;

import com.alfresco.consulting.Main;
import com.alfresco.consulting.help.UsageService;
import com.alfresco.consulting.services.TrustStoreService;
import com.alfresco.consulting.services.impl.SavingTrustManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by alexmahabir on 9/2/15.
 * <p/>
 * Variation of Sun Java's ImportCert.java
 */
@EnableAutoConfiguration
@Component
public class TestTruststore extends Main implements CommandLineRunner, ExitCodeGenerator {

    @Value("${short_name.testtruststore}")
    protected String short_name;

    int exitCode = 0;
    @Value("${tt.host:}")
    protected String host;

    @Value("${tt.store:}")
    protected File store;

    @Value("${tt.pass:}")
    protected String storepass;

    @Value("${usage.testtruststore}")
    protected String usage;

    @Value("${tt.port:443}")
    protected Integer port;

    @Value("${tt.trust.trusted")
    protected String trusted;
    Logger log = Logger.getLogger(TestTruststore.class);

    @Value("${tt.update.cert:}")
    protected String updateCert;

    @Value("${tt.storetype:JCEKS}")
    String keystore_type;

    @Value("${tt.backup:}")
    protected File backup;
    @Value("${tt.writeto:}")
    protected File writeto;

    @Autowired
    protected TrustStoreService defaultTrustService;

    /**
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if (!this.isCliTarget(args))
            return;

        updateTrustStore();

    }

    /**
     * Function to update the trust store based CAS and certs presented by the host that was specified.
     *
     * @throws Exception
     */
    private void updateTrustStore() throws Exception {
        File store = getStore();
        String host = getHost();
        String pass = getStorepass();
        String storeType = getKeystore_type();

        if (host == null || host.isEmpty() || store == null || store.getName().isEmpty() ||
                pass == null || pass
                .isEmpty()) {
            UsageService.printUssage(usage);
            exitCode = 30;
            return;
        }
        if (!store.exists()) {
            exitCode = 31;
            log.error("The following Keystore does not exist: " + store.getAbsolutePath());
            return;
        }

        String[] hostr = host.split(":");
        host = hostr[0];
        port = hostr.length == 2 ? Integer.parseInt(hostr[1]) : port;
        KeyStore ks = defaultTrustService.loadKeystore(store, pass.toCharArray(), storeType);
        defaultTrustService.printKeyStore(ks);
        SavingTrustManager tm = new SavingTrustManager();
        SSLSocket socket = defaultTrustService.sslConnect(ks, host, port, tm);
        defaultTrustService.sslHandShake(socket);
        X509Certificate[] chain = tm.getChain();
        if (chain == null) {
            log.error("Could not obtain server certificate chain. Exiting...");
            return;
        }

        System.out.println();
        System.out.println("List of Certificates that were sent by the server");
        System.out.println();
        defaultTrustService.listCertChain(
                defaultTrustService.getIndexedCertificates(
                        chain));
        TrustStoreService.X509IndexedCertificate[] CAs = defaultTrustService.findCAs(chain);

        System.out.println("If the targeted host is to be trusted, the Certificate Authorities in the X509 chain " +
                "should be added to the truststore");


        X509Certificate[] certsToInstall = getCAtoUpdate(chain, CAs);
        if (certsToInstall != null) {

            File backupFile = getBackupFile();
            System.out.println("Backing up to: " + backupFile.getAbsolutePath());
            Files.copy(store.toPath(), backupFile.toPath());

            if (!store.canWrite()) {
                log.error("KeyStore is not writeable");
                return;
            }
            for (int i = 0; i < certsToInstall.length; i++) {
                X509Certificate cert = certsToInstall[i];
                String alias = cert.getSubjectDN().getName();
                alias = alias.split(",")[0];
                alias = alias.substring(3, alias.length());
                System.out.println("Adding: " + alias);
                ks.setCertificateEntry(alias, cert);
            }
            File output = getOutputFile();
            defaultTrustService.printKeyStore(ks);
            System.out.println("Saving Keystore to: " + output.getAbsolutePath());
            OutputStream out = new FileOutputStream(output);
            ks.store(out, pass.toCharArray());
            out.close();

        }
    }

    /**
     * Determine the file to backup keystore to
     *
     * @return
     */
    private File getBackupFile() {
        final File backup = getBackup();
        final File store = getStore();
        File backupFile = null;

        if (backup == null) {
            backupFile = new File(store.getAbsolutePath() + "-" + (new Date()).getTime() + ".bak");
        } else if (backup.exists() && backup.isDirectory()) {
            backupFile = new File(backup.getAbsolutePath() + File.separator + store.getName() + "-" + (new Date())
                    .getTime() + ".bak");
        } else if (!backup.exists() && backup.canWrite()) {
            backupFile = getBackup();
        }
        return backupFile;
    }

    /**
     * Determine the file to write the keystore to.
     *
     * @return
     */
    private File getOutputFile() throws IOException {
        final File store = getStore();
        final File writeto = getWriteto();
        File output = null;
        if (writeto != null && writeto.isDirectory()) {
            output = new File(writeto.getAbsolutePath() + File.separator + store.getName());
            output.createNewFile();
        } else if (writeto != null && !writeto.exists() && writeto.canWrite()) {
            output = writeto;
            output.createNewFile();
        } else {
            output = store;
        }
        return output;
    }


    /**
     * @param list
     * @return
     */
    private ArrayList<Integer> getCertIndex(String list) {
        if (list == null)
            return null;
        String[] sList = list.split(",");
        ArrayList<Integer> ret = new ArrayList<>();
        for (String i : sList) {
            try {
                ret.add(Integer.parseInt(i) - 1);
            } catch (Exception e) {
                log.error("Invalid entry");
                return null;
            }

        }
        return ret;
    }

    /**
     * @param indexedCertificates
     * @return
     */
    private X509Certificate[] getCAtoUpdate(final X509Certificate[] chain, final TrustStoreService
            .X509IndexedCertificate[] indexedCertificates) {
        Console c = System.console();
        String rec;
        ArrayList<Integer> selected = null;
        String recommendedValues = "";
        for (TrustStoreService.X509IndexedCertificate ic : indexedCertificates) {
            recommendedValues += (ic.index + 1) + ",";
        }
        if (recommendedValues.length() > 1)
            recommendedValues = recommendedValues.substring(0, recommendedValues.length() - 1);
        do {
            if (getUpdateCert() == null || getUpdateCert().isEmpty()) {
                rec = c.readLine("Choose from the following options to update the truststore:\n"
                        + "\tA) Update truststore with recommended values: " + recommendedValues
                        + "\t[0,1,..]) Update truststore with custom selection\n"
                        + "\tQ) To exist without updating the truststore\n\t\t=> ");
            } else {
                rec = getUpdateCert();
            }


        }
        while (!rec.toLowerCase().equals("q") && !rec.toLowerCase().equals("a") && (selected = getCertIndex(rec)) !=
                null);

        if (selected != null) {
            X509Certificate[] selectedCerts = new X509Certificate[selected.size()];
            for (int i = 0; i < selected.size(); i++) {
                selectedCerts[i] = chain[selected.get(i)];
            }
            return selectedCerts;
        } else if (rec.toLowerCase().equals("a")) {
            X509Certificate[] CAs = new X509Certificate[indexedCertificates.length];
            for (int i = 0; i < indexedCertificates.length; i++) {
                CAs[i] = indexedCertificates[i].x509Certificate;
            }
            return CAs;
        }
        return null;
    }


    @Override
    public String getShort_name() {
        return this.short_name;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }


    public String getHost() {
        return host;
    }

    public File getStore() {
        return store;
    }

    public String getStorepass() {
        return storepass;
    }

    public Integer getPort() {
        return port;
    }

    public String getTrusted() {
        return trusted;
    }

    public String getUpdateCert() {
        return updateCert;
    }

    public File getBackup() {
        return backup;
    }

    public File getWriteto() {
        return writeto;
    }

    public String getKeystore_type() {
        return keystore_type;
    }


    public TrustStoreService getDefaultTrustService() {
        return defaultTrustService;
    }
}
