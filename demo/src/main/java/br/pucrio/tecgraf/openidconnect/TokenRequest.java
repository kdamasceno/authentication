package br.pucrio.tecgraf.openidconnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import br.pucrio.tecgraf.demo.utils.Cfg;
import br.pucrio.tecgraf.demo.utils.ClientUtils;

public class TokenRequest {
	
	private GrantType grantType;
	private String code;
	private String redirectUri;
	
	private String clientId;
	private String clientSecret;

	public TokenRequest(GrantType grantType){
		this.grantType = grantType;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public void setRedirectUri(String redirectUri){
		this.redirectUri = redirectUri;
	}
	
	public void setClientID(String clientId){
		this.clientId = clientId;
	}
	
	public void setClientSecret(String clientSecret){
		this.clientSecret = clientSecret;
	}

	public String getGrantType(){
		return this.grantType.toString().toLowerCase();
	}
	
	public String getCode(){
		return this.code;
	}
	
	public String getRedirectUri(){
		return this.redirectUri;
	}
	
	public String getClientID(){
		return this.clientId;
	}
	
	public String getClientSecret(){
		return this.clientSecret;
	}
	
}
