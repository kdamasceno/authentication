package br.pucrio.tecgraf.openidconnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import br.pucrio.tecgraf.demo.utils.ClientUtils;

public class TokenClient {

	private CloseableHttpClient client;
	private String tokenEndPoint;
	private TokenRequest tokenRequest;

	public TokenClient(String tokenEndPoint){
		this.tokenEndPoint = tokenEndPoint;
	}
	
	public void setExecutor(CloseableHttpClient client){
		this.client = client;
	}
	
	public void setRequest(TokenRequest tokenRequest) {
		this.tokenRequest = tokenRequest;
	}
	
	/**
	 * Envia a requisição de Token para OP.
	 *
	 * @return Resposta da requisição enviada
	 */
	public TokenResponse exec() {
		final HttpPost request = new HttpPost(tokenEndPoint);
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		request.setHeader(HttpHeaders.AUTHORIZATION, "Basic "
				+ ClientUtils.getAuthorizationHeader(tokenRequest.getClientID(), tokenRequest.getClientSecret()));

		final List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("grant_type", tokenRequest.getGrantType()));
		data.add(new BasicNameValuePair("code", tokenRequest.getCode()));
		data.add(new BasicNameValuePair("redirect_uri", tokenRequest.getRedirectUri()));

		try {
			request.setEntity(new UrlEncodedFormEntity(data));
			CloseableHttpResponse response = client.execute(request);
			return new TokenResponse(response);
		} catch (final IOException e) {
			throw new RuntimeException("Erro ao enviar requisição");
		}
	}

}
