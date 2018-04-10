package br.pucrio.tecgraf.openidconnect;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UserInfoResponse {

	private HttpEntity entity;
	private String status;
	private Object user;

	public UserInfoResponse(CloseableHttpResponse userInfoResponse, String subjectIDToken) {
		this.entity = userInfoResponse.getEntity();

		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject userInfoObject = (JSONObject) jsonParser.parse(EntityUtils.toString(entity));

			if (userInfoResponse.getStatusLine().getStatusCode() == 400) {
				// Error Response
				status = userInfoObject.get("error").toString();

			} else {

				// Successful Response -> Validar resposta recebida
				boolean userIsValid = userInfoResponseValidation(userInfoObject);

				// NOTE: Due to the possibility of token substitution attacks
				// (see
				// Section 16.11), the UserInfo
				// Response is not guaranteed to be about the End-User
				// identified by
				// the sub (subject) element of the ID Token.
				// The sub Claim in the UserInfo Response MUST be verified to
				// exactly match the sub Claim in the ID Token;
				// if they do not match, the UserInfo Response values MUST NOT
				// be
				// used.
				if (!userIsValid || !userInfoObject.get("sub").equals(subjectIDToken)) {
					status = "Falha ao obter informações do usuário";
				}

				// System.out.println("name =" + userInfoObject.get("name"));
				// System.out.println("gender =" +
				// userInfoObject.get("gender"));
				// System.out.println("profile =" +
				// userInfoObject.get("profile"));
				// System.out.println("sub =" + userInfoObject.get("sub"));

				user = userInfoObject;
			}

		} catch (Exception e) {
			e.printStackTrace();
			status = e.getMessage();
		}
	}

	public Object getUser() {
		return user;
	}

	public String getStatus() {
		return status;
	}
	
	/**
	 * @see http://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponseValidation
	 * 
	 * @return
	 */
	public boolean userInfoResponseValidation(JSONObject userInfoJson) {

		// NOTE: Due to the possibility of token substitution attacks (see
		// Section 16.11), the UserInfo Response
		// is not guaranteed to be about the End-User identified by the sub
		// (subject) element of the ID Token.
		// The sub Claim in the UserInfo Response MUST be verified to exactly
		// match the sub Claim in the ID Token;
		// if they do not match, the UserInfo Response values MUST NOT be used.
		// TODO

		// Verify that the OP that responded was the intended OP through a TLS
		// server certificate check, per RFC 6125 [RFC6125].
		// TODO

		// If the Client has provided a userinfo_encrypted_response_alg
		// parameter during Registration,
		// decrypt the UserInfo Response using the keys specified during
		// Registration.
		// TODO

		// If the response was signed, the Client SHOULD validate the signature
		// according to JWS [JWS].
		// TODO

		return true;
	}

}
