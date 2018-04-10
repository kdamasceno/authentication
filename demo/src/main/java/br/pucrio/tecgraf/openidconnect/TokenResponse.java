package br.pucrio.tecgraf.openidconnect;

import java.util.HashMap;
import java.util.Map;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import br.pucrio.tecgraf.demo.utils.Cfg;
import br.pucrio.tecgraf.demo.utils.ClientUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenResponse {

	private HttpEntity entity;
	private String status;

	private Object accessToken;
	private Object idToken;
	private Map<String, Object> tokenIDClaims;

	public TokenResponse(CloseableHttpResponse tokenResponse) {
		this.entity = tokenResponse.getEntity();

		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject tokenObject = (JSONObject) jsonParser.parse(EntityUtils.toString(entity));

			if (tokenResponse.getStatusLine().getStatusCode() == 400) {
				// Error Response
				status = tokenObject.get("error").toString();

			} else {

				// Successful Response -> Validar resposta recebida
				tokenIDClaims = new HashMap<String, Object>();

				if (!tokenResponseValidation(tokenObject, tokenIDClaims)) {
					status = "invalid_token";
				} else {
					accessToken = tokenObject.get("access_token");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			status = e.getMessage();
		}

	}

	public Object getAccessToken() {
		return accessToken;
	}

	public Object getIdToken() {
		return idToken;
	}

	public HttpEntity getEntity() {
		return entity;
	}

	public String getStatus() {
		return status;
	}

	public static String createTecToken(String userInfoEndPoint, TokenResponse tokenResponse){
		UserInfoClient userInfoClient = new UserInfoClient(userInfoEndPoint);
		
		userInfoClient.setExecutor(ClientUtils.getClient());
		String subject = tokenResponse.getSubject();
		userInfoClient.setRequest(new UserInfoRequest(tokenResponse.getAccessToken(), subject));
		UserInfoResponse userInfoResponse = userInfoClient.exec();
		
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("access_token", tokenResponse.getAccessToken());
		claims.put("sub", tokenResponse.getAccessToken());
		claims.put("user", userInfoResponse.getUser());
		
		return Jwts.builder().setSubject(subject).setClaims(claims)
				.signWith(SignatureAlgorithm.HS512, Cfg.instance().getProperty("privateKey")).compact();
	}
	
	/**
	 * @see http://openid.net/specs/openid-connect-core-1_0.html#TokenResponseValidation
	 * 
	 * @return
	 */
	public boolean tokenResponseValidation(JSONObject tokenJson, Map<String, Object> tokenIDClaims) {

		// 1. Follow the validation rules in RFC 6749, especially those in
		// Sections 5.1 and 10.12.
		// TODO

		// 2. Follow the ID Token validation rules in Section 3.1.3.7.
		Object idtoken = tokenJson.get("id_token");
		if (!validateIDToken(idtoken, tokenIDClaims)) {
			return false;
		}

		// 3. Follow the Access Token validation rules in Section 3.1.3.8.
		Object accesstoken = tokenJson.get("access_token");
		if (!validateAccessToken(idtoken, accesstoken)) {
			return false;
		}

		return true;
	}

	/**
	 * @see 3.1.3.7. ID Token Validation
	 *      (http://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation)
	 * @param idtoken
	 * 
	 * @return TRUE se token ID está válido
	 */
	private boolean validateIDToken(Object idtoken, Map<String, Object> tokenIDClaims) {

		try {

			if (idtoken == null) {
				return false;
			}

			Claims claims = Jwts.parser().setSigningKey(ClientUtils.getClientSecretGluu().getBytes("UTF-8"))
					.parseClaimsJws(idtoken.toString()).getBody();

			// Verificação de informações no payload do IDToken
			// Comparar com informação na configuração no Cliente no OP

			String issuer = (String) claims.get("iss");
			if (!issuer.equals(Cfg.instance().getProperty("issuer"))) {
				return false;
			}

			// FIXME aud pode retornar uma lista, verificar se contém o
			// client_id
			// se for lista verificar também se 'azp' (authorized party) existe
			// e se é igual a client_id
			String audience = (String) claims.get("aud");
			if (!audience.equals(ClientUtils.getClientID())) {
				return false;
			}

			// TODO outras regras da especificação, finalizar

			tokenIDClaims.put("sub", claims.get("sub"));

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @see http://openid.net/specs/openid-connect-core-1_0.html#CodeFlowTokenValidation
	 * 
	 * @param accesstoken
	 * 
	 * @return TRUE se access token está válido
	 */
	private boolean validateAccessToken(Object idtoken, Object accesstoken) {

		if (accesstoken == null) {
			return false;
		}

		// se ID Token contem "at_hash", validar access_token
		// TODO

		return true;
	}

	public String getSubject() {
		return tokenIDClaims.get("sub").toString();
	}


}
