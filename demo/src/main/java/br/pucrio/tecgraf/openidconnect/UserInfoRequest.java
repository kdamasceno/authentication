package br.pucrio.tecgraf.openidconnect;

public class UserInfoRequest {

	private Object accessToken;
	private String subjectToken;
	
	public UserInfoRequest(Object accessToken, String subjectToken){
		this.accessToken = accessToken;
		this.subjectToken = subjectToken;
	}
	

	public Object getAccessToken() {
		return accessToken;
	}


	public String getSubject() {
		return subjectToken;
	}

}
