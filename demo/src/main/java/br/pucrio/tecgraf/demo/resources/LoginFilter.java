package br.pucrio.tecgraf.demo.resources;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.pucrio.tecgraf.demo.utils.Cfg;
import br.pucrio.tecgraf.demo.utils.ClientUtils;
import io.jsonwebtoken.lang.Strings;
import jersey.repackaged.com.google.common.base.Preconditions;

//TODO considerar migrar código do openid para o filtro
//https://github.com/GluuFederation/oxAuth/blob/master/RP-Demo/src/main/java/org/xdi/oxauth/rp/demo/LoginFilter.java
//https://www.gluu.org/blog/java-openid-connect-servlet-sample/

public class LoginFilter implements Filter {

	public static final String WELL_KNOWN_CONNECT_PATH = "/.well-known/openid-configuration";

	private String authorizeParameters;
	private String redirectUri;
	private String authorizationServerHost;
	private String clientId;
	private String clientSecret;

	@Override
	 public void init(FilterConfig filterConfig) throws ServletException {
	 authorizeParameters =
	 filterConfig.getInitParameter("authorizeParameters");
	 redirectUri = filterConfig.getInitParameter("redirectUri");
	 authorizationServerHost =
	 filterConfig.getInitParameter("authorizationServerHost");
	 clientId = filterConfig.getInitParameter("clientId");
	 clientSecret = filterConfig.getInitParameter("clientSecret");
	 Preconditions.checkState(redirectUri.startsWith("https:"),
	 "Redirect URI must use https protocol for client application_type=web.");
	 }

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		boolean redirectForLogin = fetchTokenIfCodeIsPresent(request);

		Object accessToken = request.getSession(true).getAttribute("access_token");
		if (accessToken == null) {
			if (redirectForLogin) {
				redirectToLogin(request, response);
			} else {
				System.out.println("Login failed.");
				response.setContentType("text/html;charset=utf-8");

				PrintWriter pw = response.getWriter();
				pw.println("<h3>Login failed.</h3>");
			}
		} else {
			System.out.println("User is already authenticated.");
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	/**
	 * @param request
	 *            request
	 * @return whether login is still required
	 */
	private boolean fetchTokenIfCodeIsPresent(HttpServletRequest request) {
		String code = request.getParameter("code");
		if (code != null && !code.trim().isEmpty()) {
			System.out.println("Fetching token for code " + code + " ...");
			fetchDiscovery(request);

			tokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
			tokenRequest.setCode(code);
			tokenRequest.setRedirectUri(redirectUri);
			tokenRequest.setAuthUsername(clientId);
			tokenRequest.setAuthPassword(clientSecret);
			tokenRequest.setAuthenticationMethod(ClientUtils.getClientSecretGluu());

			TokenClient tokenClient = new TokenClient(Cfg.instance().getProperty("tokenEndPoint"));
			tokenClient.setExecutor(Utils.createTrustAllExecutor());
			tokenClient.setRequest(tokenRequest);

			TokenResponse tokenResponse = tokenClient.exec();
			if (!Strings.isNullOrEmpty(tokenResponse.getAccessToken())) {
				System.out.println("Token is successfully fetched.");

				System.out.println("Put in session access_token: " + tokenResponse.getAccessToken() + ", id_token: "
						+ tokenResponse.getIdToken() + ", userinfo_endpoint: "
						+ Cfg.instance().getProperty("userinfoEndpoint"));
				request.getSession(true).setAttribute("access_token", tokenResponse.getAccessToken());
				request.getSession(true).setAttribute("id_token", tokenResponse.getIdToken());
				request.getSession(true).setAttribute("userinfo_endpoint", Cfg.instance().getProperty("userinfoEndpoint")));
			} else {
				System.out.println("Failed to obtain token. Status: " + tokenResponse.getStatus() + ", entity: "
						+ tokenResponse.getEntity());
			}
			return false;
		}
		return true;
	}

	private void fetchDiscovery(HttpServletRequest request) {
		try {
			if (discoveryResponse != null) { // already initialized
				return;
			}

			OpenIdConfigurationClient discoveryClient = new OpenIdConfigurationClient(
					authorizationServerHost + WELL_KNOWN_CONNECT_PATH);
			discoveryClient.setExecutor(Utils.createTrustAllExecutor());

			discoveryResponse = discoveryClient.execOpenIdConfiguration();
			System.out.println("Discovery: " + discoveryResponse);

			if (discoveryResponse.getStatus() == 200) {
				return;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		throw new RuntimeException(
				"Failed to fetch discovery information at : " + authorizationServerHost + WELL_KNOWN_CONNECT_PATH);
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		fetchDiscovery(request);

		String redirectTo = Cfg.instance().getProperty("authorizationEndpoint") + "?redirect_uri=" + redirectUri
				+ "&client_id=" + clientId + "&" + authorizeParameters;
		System.out.println("Redirecting to authorization url : " + redirectTo);
		response.sendRedirect(redirectTo);
	}

	@Override
	public void destroy() {
	}
}