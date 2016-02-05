package org.fogbowcloud.accounting;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class Authentication {
	
	public static boolean checkAuthToken(HttpServletRequest httpRequest, Properties prop) {
		Client client = ClientBuilder.newClient();
		HttpSession session = httpRequest.getSession();
    	Object sessionAuthToken = session.getAttribute(FogbowConstants.SESSION_AUTH_TOKEN_ATTRIBUTE);
    	
    	if (sessionAuthToken == null) {
    		return false;
    	}
    	
		String managerUrl = prop.getProperty(FogbowConstants.FOGBOW_MANAGER_URL_PROP);
    	WebTarget target = client.target(managerUrl + FogbowConstants.REQUEST_TERM);
    	Invocation get = target.request()
    			.header(FogbowConstants.CONTENT_TYPE_HEADER_ATTR, FogbowConstants.CONTENT_TYPE_TEXT_OCCI)
    			.header(FogbowConstants.AUTH_TOKEN_HEADER_ATTR, sessionAuthToken.toString())
    			.accept(MediaType.TEXT_PLAIN)
    			.buildGet();
    	Response response = get.invoke();
    	
		return response.getStatus() == Status.OK.getStatusCode();
	}

}
