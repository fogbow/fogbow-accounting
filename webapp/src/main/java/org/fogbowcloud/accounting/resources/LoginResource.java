package org.fogbowcloud.accounting.resources;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.accounting.FogbowConstants;

/**
 * Login resource (exposed at "auth" path)
 */
@Path("auth")
public class LoginResource {
	private static final String AUTH_TOKEN_ATTRIBUTE = "authToken";
	
	@Inject
	private Properties properties;
	
	@Context 
	private HttpServletRequest request;

	@GET
	public Response get() {
		String loginForm = "";
		try {
			loginForm = IOUtils.toString(new FileInputStream("templates/login.phtml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK)
				.entity(loginForm).build();
	}
	
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("authToken") String authToken, 
    		@Context HttpServletRequest request) {
    	if (authToken == null || authToken.isEmpty()) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	
    	HttpSession session = request.getSession(true);
    	
    	String managerUrl = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_URL_PROP);
    	Response response = checkAuthToken(authToken, managerUrl);
    	if (response.getStatus() != Status.OK.getStatusCode()) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	session.setAttribute(AUTH_TOKEN_ATTRIBUTE, authToken);
    	
    	return Response.status(Status.OK).build();
    }
    
    @GET
    @Path("/logout")
    public Response logout() {
    	HttpSession session = request.getSession();
    	session.removeAttribute(AUTH_TOKEN_ATTRIBUTE);
    	return Response.seeOther(URI.create("/auth")).build();
    }
    
    @GET
    @Path("/checkSession")
    public Response checkSession() {
    	HttpSession session = request.getSession();
    	Object sessionAuthToken = session.getAttribute(AUTH_TOKEN_ATTRIBUTE);
    	if (sessionAuthToken == null) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	
    	String managerUrl = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_URL_PROP);
    	
    	Response response = checkAuthToken(sessionAuthToken, managerUrl);
    	if (response.getStatus() != Status.OK.getStatusCode()) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	
    	return Response.status(Status.OK).build();
    }

	protected Response checkAuthToken(Object sessionAuthToken, String managerUrl) {
		Client client = ClientBuilder.newClient();
    	WebTarget target = client.target(managerUrl + FogbowConstants.REQUEST_TERM);
    	Invocation get = target.request()
    			.header(FogbowConstants.CONTENT_TYPE_HEADER_ATTR, FogbowConstants.CONTENT_TYPE_TEXT_OCCI)
    			.header(FogbowConstants.AUTH_TOKEN_HEADER_ATTR, sessionAuthToken.toString())
    			.accept(MediaType.TEXT_PLAIN)
    			.buildGet();
    	Response response = get.invoke();
		return response;
	}
}
