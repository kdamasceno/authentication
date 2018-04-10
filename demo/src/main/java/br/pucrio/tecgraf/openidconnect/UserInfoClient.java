package br.pucrio.tecgraf.openidconnect;

import java.io.IOException;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

public class UserInfoClient {

	private CloseableHttpClient client;
	private String userInfoEndPoint;
	private UserInfoRequest userInfoRequest;

	public UserInfoClient(String userInfoEndPoint) {
		this.userInfoEndPoint = userInfoEndPoint;
	}

	public void setExecutor(CloseableHttpClient client) {
		this.client = client;
	}
	
	public void setRequest(UserInfoRequest userInfoRequest) {
		this.userInfoRequest = userInfoRequest;
	}

	/**
	 * Envia a requisição de informações do usuário para OP.
	 *
	 * @return Resposta da requisição enviada
	 */
	public UserInfoResponse exec() {
		final HttpGet request = new HttpGet(userInfoEndPoint+"?access_token="+userInfoRequest.getAccessToken());
		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userInfoRequest.getAccessToken());
		try {
			CloseableHttpResponse response = client.execute(request);
			return new UserInfoResponse(response, userInfoRequest.getSubject());
		} catch (final IOException e) {
			throw new RuntimeException("Erro ao enviar requisição");
		}
	}

}
