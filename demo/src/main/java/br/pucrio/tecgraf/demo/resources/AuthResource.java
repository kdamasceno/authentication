package br.pucrio.tecgraf.demo.resources;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import br.pucrio.tecgraf.demo.utils.Cfg;
import br.pucrio.tecgraf.demo.utils.ClientUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path("login")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthResource {

	/**
	 * 
	 * @param multiPart
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response post(@FormParam("code") String code, @FormParam("redirect_uri") String redirect_uri)
			throws IOException, NoSuchAlgorithmException {

		Map<String, Object> retorno = new HashMap<>();
		CloseableHttpClient client = ClientUtils.getClient();

		if (client == null) {
			retorno.put("error", "Não foi possível criar cliente");
			return Response.status(400).entity(retorno).build();
		}

		try {
			HttpResponse tokenResponse = sendTokenRequest(client, code, redirect_uri);
			String strTokenResponse = EntityUtils.toString(tokenResponse.getEntity());
			JSONParser jsonParser = new JSONParser();
			JSONObject tokenObject = (JSONObject) jsonParser.parse(strTokenResponse);

			// para teste...
			Map<String, Object> tokenIDClaims = new HashMap<>();

			// Token Error Response
			if (tokenResponse.getStatusLine().getStatusCode() == 400) {
				// FIXME apenas repassar json de erro recebido, como protocolo?
				retorno.put("error", tokenObject.get("error"));
				return Response.status(400).entity(retorno).build();
			}

			// Successful Token Response -> Validar resposta recebida
			boolean isValid = tokenResponseValidation(tokenObject, tokenIDClaims);
			if (!isValid) {
				retorno.put("error", "invalid_token");
				return Response.status(400).entity(retorno).build();
			}

			// Recupera informações do usuário para incluir na resposta
			Object accessToken = tokenObject.get("access_token");

			HttpResponse userInfoResponse = sendUserInfoRequest(client, accessToken.toString());
			String strUserInfoResponse = EntityUtils.toString(userInfoResponse.getEntity());
			jsonParser = new JSONParser();
			JSONObject userInfoObject = (JSONObject) jsonParser.parse(strUserInfoResponse);

			// UserInfo Error Response
			if (userInfoResponse.getStatusLine().getStatusCode() == 400) {
				// FIXME apenas repassar json de erro recebido, como protocolo?
				retorno.put("error", userInfoObject.get("error"));
				retorno.put("error_description", userInfoObject.get("error_description"));
				return Response.status(400).entity(retorno).build();
			}

			// Successful UserInfo Response -> Validar resposta recebida
			boolean userIsValid = userInfoResponseValidation(userInfoObject);

			// NOTE: Due to the possibility of token substitution attacks (see
			// Section 16.11), the UserInfo
			// Response is not guaranteed to be about the End-User identified by
			// the sub (subject) element of the ID Token.
			// The sub Claim in the UserInfo Response MUST be verified to
			// exactly match the sub Claim in the ID Token;
			// if they do not match, the UserInfo Response values MUST NOT be
			// used.
			if (!userIsValid || !userInfoObject.get("sub").equals(tokenIDClaims.get("sub"))) {
				retorno.put("error", "Falha ao obter claims do usuário");
				return Response.status(400).entity(retorno).build();
			}

			// System.out.println("name =" + userInfoObject.get("name"));
			// System.out.println("gender =" + userInfoObject.get("gender"));
			// System.out.println("profile =" + userInfoObject.get("profile"));
			// System.out.println("sub =" + userInfoObject.get("sub"));
			// System.out.println("access_token =" + accessToken.toString());

			tokenIDClaims.put("access_token", accessToken);
			String subject = (String) tokenIDClaims.remove("sub");
			tokenIDClaims.put("user", userInfoObject);

			// cria novo token
			String jwt = Jwts.builder().setSubject(subject).setClaims(tokenIDClaims)
					.signWith(SignatureAlgorithm.HS512, Cfg.instance().getProperty("privateKey")).compact();
			retorno.put("tec_token", jwt);

		} catch (Exception e) {
			retorno.put("error", e.getMessage());
			return Response.status(400).entity(retorno).build();
		} finally {
			client.close();
		}

		return Response.status(200).entity(retorno).build();
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

	/**
	 * Envia a requisição de Token para OP.
	 *
	 * @return Resposta da requisição enviada
	 */
	public HttpResponse sendTokenRequest(CloseableHttpClient client, String code, String redirect_uri) {
		final HttpPost request = new HttpPost(Cfg.instance().getProperty("tokenEndPoint"));
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + ClientUtils.getAuthorizationHeader());

		final List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("grant_type", "authorization_code"));
		data.add(new BasicNameValuePair("code", code));
		data.add(new BasicNameValuePair("redirect_uri", redirect_uri));

		try {
			request.setEntity(new UrlEncodedFormEntity(data));
			return client.execute(request);
		} catch (final IOException e) {
			throw new RuntimeException("Erro ao enviar requisição");
		}
	}

	/**
	 * Envia a requisição de informações do usuário para OP.
	 * 
	 * @param client
	 * @param access_token
	 * 
	 * @return Resposta da requisição
	 */
	public HttpResponse sendUserInfoRequest(CloseableHttpClient client, String access_token) {
		String url = Cfg.instance().getProperty("userinfoEndpoint") + "?access_token=" + access_token;
//		System.out.println("url = " + url);
		// *** legacyIdTokenClaims habilitado na aba "JSON Configuration" no
		// Gluuserver
		final HttpGet request = new HttpGet(url);
		request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + access_token);
		try {
			return client.execute(request);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Erro ao enviar requisição");
		}
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
