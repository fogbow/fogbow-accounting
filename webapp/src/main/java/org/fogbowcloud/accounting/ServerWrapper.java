package org.fogbowcloud.accounting;

import javax.servlet.ServletRegistration;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.websockets.WebSocketAddOn;

public class ServerWrapper {

	public HttpServer create(int port, Class<?> applicationClass) {
		final HttpServer server = HttpServer.createSimpleServer(null, port);

        final WebappContext ctx = new WebappContext("ctx", "");

        final WebSocketAddOn addon = new WebSocketAddOn();

        for (NetworkListener listener : server.getListeners()) {
            listener.registerAddOn(addon);
        }
        
        AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
        AtmosphereFramework f = atmosphereServlet.framework();

        ReflectorServletProcessor r = new ReflectorServletProcessor();
        r.setServletClassName("org.glassfish.jersey.servlet.ServletContainer");
        f.addAtmosphereHandler("/*", r);
        
        ServletRegistration atmosphereServletRegistration = ctx.addServlet("AtmosphereServlet", atmosphereServlet);
        atmosphereServletRegistration.setInitParameter("javax.ws.rs.Application", applicationClass.getCanonicalName());
        atmosphereServletRegistration.setInitParameter("org.atmosphere.websocket.messageContentType", "application/json");
        atmosphereServletRegistration.setInitParameter(ApplicationConfig.CLIENT_HEARTBEAT_INTERVAL_IN_SECONDS, "60");
        atmosphereServletRegistration.addMapping("/*");
        
        ctx.deploy(server);

        return server; 
	}
	
}