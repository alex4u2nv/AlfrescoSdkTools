package com.alfresco.consulting.services;

import com.alfresco.consulting.services.impl.SavingTrustManager;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by alexmahabir on 9/4/15.
 */
public interface TrustStoreService {

    /**
     *
     * @throws Exception
     */
    public void updateTrustStore() throws Exception;

    /**
     *
     * @param ks
     * @throws KeyStoreException
     */
    public void printKeyStore(KeyStore ks) throws KeyStoreException;

    /**
     *
     * @param chain
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    public void listCertChain(X509IndexedCertificate[] chain) throws CertificateEncodingException, NoSuchAlgorithmException;

    /**
     *
     * @param chain
     * @return
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    public X509IndexedCertificate[] findCAs(X509Certificate[] chain) throws CertificateEncodingException, NoSuchAlgorithmException;

    /**
     *
     * @param certificate
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    public void outputCertificate(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException;

    /**
     *
     * @param socket
     * @throws IOException
     */
    public void sslHandShake(SSLSocket socket) throws IOException;

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
    public KeyStore loadKeystore(File keystore, char[] pass, String storetype)   throws KeyStoreException, IOException,
    CertificateException, NoSuchAlgorithmException ;

    /**
     *
     * @param chain
     * @return
     */
    public X509IndexedCertificate[] getIndexedCertificates(X509Certificate[] chain);
    /**
     *
     * @param ks
     * @param host
     * @param port
     * @param savingTrustManager
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    public SSLSocket sslConnect(KeyStore ks, String host, Integer port, SavingTrustManager savingTrustManager) throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException, IOException;


    /**
     * X509 Indexed Certificate as they were received from the server.
     */
    public class X509IndexedCertificate {
        public int index;
        public X509Certificate x509Certificate;

        public X509IndexedCertificate(int index, X509Certificate x509Certificate) {
            this.index = index;
            this.x509Certificate = x509Certificate;
        }
    }

}
