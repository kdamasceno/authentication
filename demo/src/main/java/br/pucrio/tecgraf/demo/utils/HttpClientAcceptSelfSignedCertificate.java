package br.pucrio.tecgraf.demo.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This example demonstrates how to ignore certificate errors.
 * These errors include self signed certificate errors and hostname verification errors.
 * 
 * https://memorynotfound.com/ignore-certificate-errors-apache-httpclient/
 * 
 */
public class HttpClientAcceptSelfSignedCertificate {

    public static void main(String... args)  {

        try (CloseableHttpClient httpclient = createAcceptSelfSignedCertificateClient()) {
            HttpGet httpget = new HttpGet("https://example.com");
            System.out.println("Executing request " + httpget.getRequestLine());
            
            httpclient.execute(httpget);
            System.out.println("----------------------------------------");
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally disable hostname verification. 
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        
        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        
        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }
}
