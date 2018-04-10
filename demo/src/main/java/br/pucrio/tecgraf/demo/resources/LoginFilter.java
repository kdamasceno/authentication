package br.pucrio.tecgraf.demo.resources;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;

import br.pucrio.tecgraf.demo.utils.Cfg;
import br.pucrio.tecgraf.demo.utils.ClientUtils;
import br.pucrio.tecgraf.openidconnect.GrantType;
import br.pucrio.tecgraf.openidconnect.TokenClient;
import br.pucrio.tecgraf.openidconnect.TokenRequest;
import br.pucrio.tecgraf.openidconnect.TokenResponse;
import jersey.repackaged.com.google.common.base.Preconditions;

//TODO considerar migrar código do openid para o filtro
//https://github.com/GluuFederation/oxAuth/blob/master/RP-Demo/src/main/java/org/xdi/oxauth/rp/demo/LoginFilter.java
//https://www.gluu.org/blog/java-openid-connect-servlet-sample/
//public class LoginFilter {
//	
//}
@Provider
@Priority(Priorities.AUTHENTICATION)
public class LoginFilter implements Filter {

	private String authorizeParameters;
	private String redirectUri;
	private String clientId;
	private String clientSecret;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		authorizeParameters = filterConfig.getInitParameter("authorizeParameters");
		redirectUri = filterConfig.getInitParameter("redirectUri");
		clientId = filterConfig.getInitParameter("clientId");
		clientSecret = filterConfig.getInitParameter("clientSecret");
		Preconditions.checkState(redirectUri.startsWith("https:"),
				"Redirect URI must use https protocol for client application_type=web.");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
//		boolean redirectForLogin = fetchTokenIfCodeIsPresent(request);
//
//		Object tec_token = request.getSession(true).getAttribute("tec_token");
//		if (tec_token == null) {
//			if (redirectForLogin) {
//				redirectToLogin(request, response);
//			} else {
//				System.out.println("Login failed.");
//				response.setContentType("text/html;charset=utf-8");
//
//				PrintWriter pw = response.getWriter();
//				pw.println("<h3>Login failed.</h3>");
//			}
//		} else {
//			System.out.println("User is already authenticated.");
//			filterChain.doFilter(servletRequest, servletResponse);
//		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	/**
	 * @param request
	 * @return whether login is still required
	 */
	private boolean fetchTokenIfCodeIsPresent(HttpServletRequest request) {
		String code = request.getParameter("code");
		if (code != null && !code.trim().isEmpty()) {
			System.out.println("Obtendo token id para code " + code + " ...");

			TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
			tokenRequest.setCode(code);
			tokenRequest.setRedirectUri(redirectUri);
			tokenRequest.setClientID(clientId);
			tokenRequest.setClientSecret(clientSecret);

			TokenClient tokenClient = new TokenClient(Cfg.instance().getProperty("tokenEndPoint"));
			tokenClient.setExecutor(ClientUtils.getClient());
			tokenClient.setRequest(tokenRequest);

			TokenResponse tokenResponse = tokenClient.exec();
			if (tokenResponse.getIdToken() != null && !tokenResponse.getIdToken().toString().isEmpty()) {
				System.out.println("Token ID foi obtido com sucesso.");
				System.out.println("Colocamos na sessão access_token: " + tokenResponse.getAccessToken() + ", id_token: "
						+ tokenResponse.getIdToken() + ", userinfo_endpoint: "
						+ Cfg.instance().getProperty("userinfoEndpoint"));
				
				request.getSession(true).setAttribute("access_token", tokenResponse.getAccessToken());
				request.getSession(true).setAttribute("id_token", tokenResponse.getIdToken());
				request.getSession(true).setAttribute("userinfo_endpoint", Cfg.instance().getProperty("userinfoEndpoint"));
				request.getSession(true).setAttribute("tec_token", tokenResponse.createTecToken(Cfg.instance().getProperty("userinfoEndpoint"), tokenResponse));
				
			} else {
				System.out.println("Falha ao obter tec token. Status: " + tokenResponse.getStatus() + ", entity: "
						+ tokenResponse.getEntity());
			}
			return false;
		}
		return true;
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String redirectTo = Cfg.instance().getProperty("authorizationEndpoint") + "?redirect_uri=" + redirectUri
				+ "&client_id=" + clientId + "&" + authorizeParameters;
		System.out.println("Redirecting to authorization url : " + redirectTo);
		response.sendRedirect(redirectTo);
	}

	@Override
	public void destroy() {
	}
	
}