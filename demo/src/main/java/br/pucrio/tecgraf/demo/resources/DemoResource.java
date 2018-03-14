package br.pucrio.tecgraf.demo.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.internal.RequestScoped;

@Path("demo")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class DemoResource {

	@GET
	public Response get() throws IOException {
		Map<String, Object> retorno = new HashMap<>();
		retorno.put("msg", "OK");
		return Response.status(200).entity(retorno).build();
	}

}
