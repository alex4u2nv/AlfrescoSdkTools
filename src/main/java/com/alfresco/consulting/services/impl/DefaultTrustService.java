package com.alfresco.consulting.services.impl;

import com.alfresco.consulting.modelconf.AnsiConstants;
import com.alfresco.consulting.services.TrustStoreService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by alexmahabir on 9/4/15.
 */
@Service
public class DefaultTrustService implements TrustStoreService {
    Logger log = Logger.getLogger(DefaultTrustService.class);

    @Value("${tt.trust.trusted}")
    String trusted;
    @Override
    public void updateTrustStore() throws Exception {

    }

    /**
     *
     * @param ks
     * @throws KeyStoreException
     */
    @Override
    public void printKeyStore(KeyStore ks) throws KeyStoreException {
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
     * @param chain
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public void listCertChain(final X509IndexedCertificate[] chain) throws CertificateEncodingException, NoSuchAlgorithmException {
        System.out.println();
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i].x509Certificate;
            System.out.println("Index: " + (chain[i].index + 1));
            outputCertificate(cert);
        }

    }


    /**
     *
     * @param certificate
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public void outputCertificate(final X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        System.out.println(certificate.getBasicConstraints() < 0 ? AnsiConstants.ANSI_CYAN : (certificate.getBasicConstraints()
                == 0 ? AnsiConstants.ANSI_BLUE : AnsiConstants.ANSI_PURPLE));
        System.out.println("   Subject " + certificate.getSubjectDN());
        System.out.println("   Issuer  " + certificate.getIssuerDN());
        sha1.update(certificate.getEncoded());
        System.out.println("   sha1    " + toHexString(sha1.digest()));
        md5.update(certificate.getEncoded());
        System.out.println("   md5     " + toHexString(md5.digest()));
        System.out.println("   constraint " + certificate.getBasicConstraints());
        System.out.println(AnsiConstants.ANSI_RESET);

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
     *
     * @param chain
     * @return
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public X509IndexedCertificate[] findCAs(final X509Certificate[] chain) throws CertificateEncodingException, NoSuchAlgorithmException {
        List<X509IndexedCertificate> x509cas = new ArrayList<>();
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            if (cert.getBasicConstraints() != -1) {
                x509cas.add(new X509IndexedCertificate(i,cert));
            }

        }
        return x509cas.toArray(new X509IndexedCertificate[x509cas.size()]);
    }

    /**
     *
     * @param chain
     * @return
     */
    @Override
    public X509IndexedCertificate[] getIndexedCertificates(X509Certificate[] chain) {
        List<X509IndexedCertificate> idxCerts = new ArrayList<>();
        for(int i=0; i<chain.length ; i++) {
            idxCerts.add(new X509IndexedCertificate(i, chain[i]));
        }

        return idxCerts.toArray(new X509IndexedCertificate[idxCerts.size()]);

    }

    /**
     *
     * @param socket
     * @throws IOException
     */
    @Override
    public void sslHandShake(SSLSocket socket) throws IOException {
        try {
            log.info("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();

            System.out.println(trusted);
        } catch (SSLException e) {
            log.error(AnsiConstants.ANSI_RED + e.getMessage() + AnsiConstants.ANSI_RESET);
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
     *
     * @param keystore
     * @param pass
     * @param storetype
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public KeyStore loadKeystore( File keystore,final char[] pass,final String storetype) throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException {

        log.info("Loading Keystore: " + keystore.getAbsolutePath());
        log.info("Passphrase: " + pass);

        InputStream in = new FileInputStream(keystore);
        KeyStore ks = KeyStore.getInstance(storetype);
        ks.load(in, pass);
        in.close();
        return ks;

    }

    /**
     *
     * @param ks
     * @param host
     * @param port
     * @param tm
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    @Override
    public SSLSocket sslConnect(final KeyStore ks, final String host, final Integer port, SavingTrustManager tm)
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        tm.setTm(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host,port);

        return socket;
    }
}
