package br.pucrio.tecgraf.demo.utils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import org.apache.http.impl.client.CloseableHttpClient;

public class ClientUtils {

	public static String getClientID() {
		return Cfg.instance().getProperty("clientId");
	}

	public static String getClientSecretGluu() {
		return Cfg.instance().getProperty("clientSecret");
	}

	/**
	 * Monta header Authorization conforme protocolo de autenticação.
	 * 
	 * @return - id do cliente e segredo codificado em Base64
	 */
	public static String getAuthorizationHeader() {
		if (ClientUtils.getClientID() == null || ClientUtils.getClientSecretGluu() == null) {
			return null;
		}
		String authString = ClientUtils.getClientID() + ":" + ClientUtils.getClientSecretGluu();
		String header = Base64.getEncoder().encodeToString(authString.getBytes());
		header = header.replace("\n", "");
		header = header.replace("\r", "");
		return header;
	}

	public static CloseableHttpClient getClient() {
		// return HttpClients.createDefault();
		// TODO desenvolvimento... pois estamos usando certificado auto-assinado
		try {
			return HttpClientAcceptSelfSignedCertificate.createAcceptSelfSignedCertificateClient();
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}

}
