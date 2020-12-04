package com.example.letmelisten;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class InternalCertificate {
    private SSLContext ssl;

    public InternalCertificate() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = null;
        String temp = "-----BEGIN CERTIFICATE-----\nMIIFZzCCA0+gAwIBAgIUe4l7IEiFoscz27RhJCAQvd2nb7cwDQYJKoZIhvcNAQEN\nBQAwQzELMAkGA1UEBhMCS1IxNDAyBgNVBAMMK2xldG1lbGlzdGVuLmtvcmVhY2Vu\ndHJhbC5jbG91ZGFwcC5henVyZS5jb20wHhcNMjAxMjAzMjMyNTU1WhcNMzAxMjAx\nMjMyNTU1WjBDMQswCQYDVQQGEwJLUjE0MDIGA1UEAwwrbGV0bWVsaXN0ZW4ua29y\nZWFjZW50cmFsLmNsb3VkYXBwLmF6dXJlLmNvbTCCAiIwDQYJKoZIhvcNAQEBBQAD\nggIPADCCAgoCggIBALH2gBKcijW2iXTP+45dvRQY9Ehh5o6zmeCYhN3Z4plmXziG\nURur++S88grtI91CPiQZbrIMz+CRa1YcQewXlm3NJ4bYhhohH4qZINZDkYi7Vw6A\nlwygvLK8vBVOjShnI23mtjI0nhwdQbtmg2d5vwX5MBLZH2mxGydalUZSjspp2FdD\n14KK3F1PGKbitu37+cO4PpEbDczVTUisIJruKmmp3MvHexPYBeKa6Wz4el1vn5r5\nsYQKe06mzV4h6ypjSj70lZdqO87pq+tJPPkgI05Wi8ezlUwvEFP+AnHcKlwFPfaq\n5OP0wE+CopHVQzvDkUF1A+ZHq8j3stDmzTMnvkCme65u7XXkAFHjuCOgylmoZvQx\nruzDZgtb7Xk4B4MkQI01W9T/apW/+e8LetYC785iD8BOL5IsAxGeDNZVJDKOdb9o\nW74JMl8rDu8Nne9NdW2IkgvgA1Virizr40aUayjS4aaBJ53LVUmy4Y9tg0CkolqQ\nh4PaWPn6nWFZq15RKXn2cmYpY9ilitUlwjhiqnlBl6x6smeHomX1zYM7C1isJP7Q\n85HSipMtg27n0UTy2Kp6PquXe3Dhby6A11/m0mWYCC9P/49Oj5htfNjRvpyuRe9w\n0hQU/HRq9mKoQ1nchaWGRFe5oWTl6lvw2QbojSNGKnzpRiO5a3XzQsMX8LsPAgMB\nAAGjUzBRMB0GA1UdDgQWBBTGBy2ETfdHn4N4F5ceg/H7AmdGwDAfBgNVHSMEGDAW\ngBTGBy2ETfdHn4N4F5ceg/H7AmdGwDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3\nDQEBDQUAA4ICAQBCRydo4su/kNL6tmcNaK+auci50qvAMlfVZ1vd0BEk61DHaKnJ\nlydOsvZEAJqTkMbqC/OTWHu+gg51XvLuyLSsxQG3Z92USZ+J63RKlfepIVEhbyaq\nyndlygOiHNrmf1yxcfVXZ3wnXpqhWNA9keSIYxNKsOp+Iaz2s3LjK+ozraWDJgH8\nCTC/a5at+NNYKtBOKXl0WcC8pnZnjVL3zeCRC9nS/P4vOti+HFFqaTjIhGHnetUw\nUTlbNz5cMPDWz64GXltb7pQo6sR8z2+Cvg9MAwSypnU8mtiiVj+uweX/Ikm4knuW\nDWkJspaKmDHy2j8Ogqbwykh+sm50SfGdzGGItWZhwmQLox3qIgCXi6gowJe2y8vK\n+YxIZZZv4yfRaB73mxiLGXp7ri1xcPtxv1RNShSJJxnbGtiKhjLM5tqrB76Yf9Rw\nvdH7EGzopzvt3hB4LJ59OaHclxp/60xVEnf6hbRuDAW42Ub1LS7hmvihOBluJTcT\nPW7tgeXfOZBnoq4HlTr9Ex4zUBbNd5gzKnnnobLLLzJeyRUFYIsLY20jsaPicC/7\n490Ov9qWS3OuPLV9S4FQmw3tDb0mZVs0Mgj8jEnwUbfFZTq47/w4phUdaAsL1oyL\n3M8I3ifDA0tm9b1lZfLe42GLBM7OPDhx1LHeDlcgqmrDCiJxYx93/BcYsg==\n-----END CERTIFICATE-----";
        temp = temp.replace("\n","\r\n");
        InputStream caInputStream = new ByteArrayInputStream(temp.getBytes());

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(caInputStream);

        String alias = cert.getSubjectX500Principal().getName();

        // Load Client CA
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry(alias, cert);

        // Create KeyManager using Client CA
        String kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
        kmf.init(keyStore, null);

        // Create TrustManager using Client CA
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create SSLContext using KeyManager and TrustManager
        ssl = SSLContext.getInstance("TLS");
        ssl.init(null, tmf.getTrustManagers(), null);
    }

    public SSLContext get() {
        return ssl;
    }
}