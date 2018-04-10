package br.pucrio.tecgraf.demo.resources;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpHeaders;

import br.pucrio.tecgraf.demo.utils.Cfg;
import io.jsonwebtoken.Jwts;

// TODO Exemplo semelhante
// https://stackoverflow.com/questions/26777083/best-practice-for-rest-token-based-authentication-with-jax-rs-and-jersey

/**
 * Filtro que será aplicado em todos os endpoints privados da aplicação.
 * Verifica se o usuário está autenticado. Os endpoints públicos devem ser
 * explicitamente excluidos, como é o caso de "/login".
 * 
 * @author karla
 *
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

	private static final String AUTHENTICATION_SCHEME = "Bearer";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		if (requestContext.getUriInfo().getPath().indexOf("login") < 0) {

			// Verifica header "Authorization"
			String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
			if (authorizationHeader == null || authorizationHeader.isEmpty()
					|| !authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ")) {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				return;
			}

			// Valida "tec_token" obtido
			String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
			try {
				// se token foi assinado pela aplicação e ainda não expirou
				Jwts.parser().setSigningKey(Cfg.instance().getProperty("privateKey")).parseClaimsJws(token);
			} catch (Exception e) {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}else{
			System.out.println(requestContext);
		}

	}
}