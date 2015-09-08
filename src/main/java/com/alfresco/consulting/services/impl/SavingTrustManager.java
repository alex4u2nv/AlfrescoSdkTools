package com.alfresco.consulting.services.impl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by alexmahabir on 9/4/15.
 */
public class SavingTrustManager implements X509TrustManager {
    private  X509TrustManager tm;
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

    public X509Certificate[] getChain() {
        return chain;
    }

    public X509TrustManager getTm() {
        return tm;
    }

    public SavingTrustManager() {
        super();
    }

    public void setTm(X509TrustManager tm) {
        this.tm = tm;
    }
}

