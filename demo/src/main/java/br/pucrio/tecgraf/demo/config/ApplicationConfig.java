package br.pucrio.tecgraf.demo.config;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {

	@Inject
	public ApplicationConfig(final ServiceLocator serviceLocator, final ServletContext context) {
		packages("br.pucrio.tecgraf.demo.resources");		
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				System.out.println("");
			}
		});

	}

}
