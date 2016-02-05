package org.fogbowcloud.accounting.resources;

import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fogbowcloud.accounting.Authentication;
import org.fogbowcloud.accounting.FogbowConstants;

/**
 * Login resource (exposed at "auth" path)
 */
@Path("auth")
public class LoginResource {
	
	@Inject 
	private Properties properties;
	
	@Context
	private HttpServletRequest request;
	
	@POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("authToken") String authToken) {
    	if (authToken == null || authToken.isEmpty()) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	
    	authToken = authToken.replace("\n", "").replace("\r", "");
    	
    	HttpSession session = request.getSession(true);
    	session.setAttribute(FogbowConstants.SESSION_AUTH_TOKEN_ATTRIBUTE, authToken);
    	
    	if (!Authentication.checkAuthToken(request, properties)) {
    		session.removeAttribute(FogbowConstants.SESSION_AUTH_TOKEN_ATTRIBUTE);
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	return Response.status(Status.OK).build();
    }
    
    @GET
    @Path("/logout")
    public Response logout() {
    	HttpSession session = request.getSession();
    	session.removeAttribute(FogbowConstants.SESSION_AUTH_TOKEN_ATTRIBUTE);
    	return Response.status(Status.OK).build();
    }
    
    @GET
    @Path("/checkSession")
    public Response checkSession() {
    	
    	boolean authTokenIsValid = Authentication.checkAuthToken(request, properties);
    	if (!authTokenIsValid) {
    		return Response.status(Status.UNAUTHORIZED).build();
    	}
    	
    	return Response.status(Status.OK).build();
    }
}
