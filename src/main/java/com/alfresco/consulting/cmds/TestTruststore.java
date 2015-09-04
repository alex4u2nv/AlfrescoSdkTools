package com.alfresco.consulting.cmds;

import com.alfresco.consulting.Main;
import com.alfresco.consulting.help.UsageService;
import com.alfresco.consulting.modelconf.AnsiConstants;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

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
    SavingTrustManager tm;


    @Value("${tt.backup:}")
    protected  File backup;
    @Value("${tt.writeto:}")
    protected File writeto;


    /**
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if (!this.isCliTarget(args))
            return;
        File store  = getStore();
        String host = getHost();
        String pass=getStorepass();

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
        KeyStore ks = loadCertFile(store, getStorepass());
        printKeyStore(ks);
        SSLSocketFactory factory = getFactory(ks);
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        testHandShake(socket);
        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            log.error("Could not obtain server certificate chain. Exiting...");
            return;
        }

        String recommended = reviewChain(chain);

        System.out.println("If the targeted host is to be trusted, the following certs should be added\n\t=> " +
                AnsiConstants.ANSI_BLUE + recommended + AnsiConstants.ANSI_RESET);


        ArrayList<Integer> cas= getCAtoUpdate(recommended);
        if (cas!=null) {

            File backupFile = getBackupFile();
            System.out.println("Backing up to: " + backupFile.getAbsolutePath());
            Files.copy(store.toPath(), backupFile.toPath());

            if (!store.canWrite()) {
                log.error("KeyStore is not writeable");
                return;
            }
            for (int i: cas) {
                X509Certificate cert = chain[i];
                String alias=cert.getSubjectDN().getName();
                alias=alias.split(",")[0];
                alias = alias.substring(3, alias.length());
                System.out.println("Adding: " + alias);
                ks.setCertificateEntry(alias, cert);
            }
            File output=getOutputFile();
            printKeyStore(ks);
            System.out.println("Saving Keystore to: " + output.getAbsolutePath());
            OutputStream out = new FileOutputStream(output);
            ks.store(out, pass.toCharArray());
            out.close();

        }

    }

    /**
     * Determine the file to backup keystore to
     * @return
     */
    private File getBackupFile() {
        final File backup = getBackup();
        final File store = getStore();
        File backupFile=null;

        if (backup==null) {
            backupFile=new File(store.getAbsolutePath()+ "-" + (new Date()).getTime() + ".bak");
        } else if (backup.exists() && backup.isDirectory()) {
            backupFile=new File(backup.getAbsolutePath()+ File.separator+ store.getName() + "-" + (new Date()).getTime() + ".bak");
        } else if (!backup.exists() && backup.canWrite()) {
            backupFile=getBackup();
        }
        return backupFile;
    }

    /**
     * Determine the file to write the keystore to.
     * @return
     */
    private File getOutputFile() throws IOException {
        final File store = getStore();
        final File writeto = getWriteto();
        File output=null;
        if (writeto!=null && writeto.isDirectory()) {
            output = new File(writeto.getAbsolutePath() + File.separator + store.getName());
            output.createNewFile();
        } else if (writeto!=null && !writeto.exists() && writeto.canWrite()) {
            output = writeto;
            output.createNewFile();
        } else {
            output = store;
        }
        return output;
    }

    private void printKeyStore(KeyStore ks) throws KeyStoreException {
        if (!log.isDebugEnabled())
            return;

        Enumeration aliases=ks.aliases();
        for (; aliases.hasMoreElements(); ){
            String alias = (String)aliases.nextElement();
            log.debug(ks.getCertificate(alias));
            log.debug("===========================");
        }

    }

    /**
     *
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
                ret.add(Integer.parseInt(i)-1);
            } catch (Exception e) {
                log.error("Invalid entry");
                return null;
            }

        }
        return ret;
    }

    /**
     *
     * @param recommended
     * @return
     */
    private ArrayList<Integer> getCAtoUpdate(String recommended) {
        Console c = System.console();
        String rec;
        ArrayList<Integer> selected = null;
        do {
            if (getUpdateCert()==null || getUpdateCert().isEmpty()) {
                rec = c.readLine("Choose from the following options to update the truststore:\n"
                        + "\tA) Update truststore with recommended values: " + recommended
                        + "\t[0,1,..]) Update truststore with custom selection\n"
                        + "\tQ) To exist without updating the truststore\n\t\t=> ");
            } else {
                rec=getUpdateCert();
            }


        }
        while (!rec.toLowerCase().equals("q") && !rec.toLowerCase().equals("a") && (selected = getCertIndex(rec)) !=
                null);

        if (selected != null) {
            return selected;
        }
        else if (rec.toLowerCase().equals("a"))
            return getCertIndex(recommended);
        return null;
    }

    /**
     * Output Certificate Chain to user
     *
     * @param chain
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    private String reviewChain(X509Certificate[] chain) throws CertificateEncodingException, NoSuchAlgorithmException {
        System.out.println();
        System.out.println("Server sent " + chain.length + " certificate(s):");
        System.out.println();
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String ret = "";
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            System.out.println(cert.getBasicConstraints() < 0 ? AnsiConstants.ANSI_CYAN : (cert.getBasicConstraints()
                    == 0 ? AnsiConstants.ANSI_BLUE : AnsiConstants.ANSI_PURPLE));
            System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            System.out.println("   md5     " + toHexString(md5.digest()));
            System.out.println("   constraint " + cert.getBasicConstraints());
            System.out.println(AnsiConstants.ANSI_RESET);
            if (cert.getBasicConstraints() != -1) {
                ret += (i + 1) + ",";
            }

        }
        if (ret.length() > 0)
            return ret.substring(0, ret.length() - 1);
        return "";
    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    /**
     * Convert to HEX String
     *
     * @param bytes
     * @return
     */
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * @param socket
     * @throws IOException
     */
    private void testHandShake(SSLSocket socket) throws IOException {
        try {
            log.info("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();

            System.out.println(trusted);
        } catch (SSLException e) {
            log.error(e.getMessage());
            if (e.getMessage().indexOf("unable to find valid certification path to requested target") > -1) {
                System.out.println(AnsiConstants.ANSI_RED + "Certificate not trusted by this store." + AnsiConstants
                        .ANSI_RESET);
                System.out.println(AnsiConstants.ANSI_RED + "If certificate should be trusted, then follow prompt to" +
                        " add certificates to " +
                        "the store." + AnsiConstants.ANSI_RESET);
            }
        }
    }

    /**
     * Load the certificate keystore
     *
     * @param certFile
     * @param passphrase
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    private KeyStore loadCertFile(File certFile, String passphrase) throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException {
        log.info("Loading Keystore: " + certFile.getAbsolutePath());
        log.info("Passphrase: " + passphrase.toCharArray());

        InputStream in = new FileInputStream(certFile);
        KeyStore ks = KeyStore.getInstance(keystore_type);
        ks.load(in, passphrase.toCharArray());
        in.close();
        return ks;
    }

    /**
     * Build SSL factore with loaded keystore
     *
     * @param ks
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private SSLSocketFactory getFactory(KeyStore ks) throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        log.info("Opening connection to: " + host + ":" + port);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        return factory;
    }

    @Override
    public String getShort_name() {
        return this.short_name;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Saving trust manager to add certificates to the trust store.
     */
    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
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
}
