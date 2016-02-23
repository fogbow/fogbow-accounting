package org.fogbowcloud.accounting.resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.accounting.ServerTestHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LoginResourceTest {
	private HttpServer server;
	private WebTarget target;
	
	@Before
	public void setUp() throws Exception {
		server = new ServerTestHelper().startWebSocketServer();
		Client c = ClientBuilder.newClient();
		target = c.target(ServerTestHelper.BASE_URL);
	}
	
	@After
	public void tearDown() {
		server.shutdownNow();
	}
	
	@Test
	public void testCheckSessionUnauthorized() {
		Response response = target.path("auth/checkSession").request().get();
		Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testCheckSessionAuthorized() throws Exception {
		String proxyAuth = IOUtils.toString(new FileInputStream(new File("/tmp/x509up_u1350")));
		MultivaluedMap<String, String> formParam = new MultivaluedHashMap<String, String>();
		formParam.add("authToken", proxyAuth);
		
		Response loginResponse = target.path("auth/login").request(
				MediaType.APPLICATION_FORM_URLENCODED).post(Entity.form(formParam));
		Assert.assertEquals(Status.OK.getStatusCode(), loginResponse.getStatus());
		Map<String, NewCookie> cookies = loginResponse.getCookies();
		
		Response checkSessionResponse = target.path("auth/checkSession").request().cookie(cookies.get("JSESSIONID")).get();
		Assert.assertEquals(Status.OK.getStatusCode(), checkSessionResponse.getStatus());
	}
	
	@Test
	public void testCheckSessionLogout() throws Exception {
		String proxyAuth = IOUtils.toString(new FileInputStream(new File("/tmp/x509up_u1350")));
		MultivaluedMap<String, String> formParam = new MultivaluedHashMap<String, String>();
		formParam.add("authToken", proxyAuth);
		
		Response loginResponse = target.path("auth/login").request(
				MediaType.APPLICATION_FORM_URLENCODED).post(Entity.form(formParam));
		Assert.assertEquals(Status.OK.getStatusCode(), loginResponse.getStatus());
		Map<String, NewCookie> cookies = loginResponse.getCookies();
		
		Response checkSessionResponse = target.path("auth/checkSession").request().cookie(cookies.get("JSESSIONID")).get();
		Assert.assertEquals(Status.OK.getStatusCode(), checkSessionResponse.getStatus());
		
		Response logoutResponse = target.path("auth/logout").request().cookie(cookies.get("JSESSIONID")).get();
		Assert.assertEquals(Status.OK.getStatusCode(), logoutResponse.getStatus());
		
		Response checkSessionAfterLogoutResponse = target.path("auth/checkSession").request().cookie(cookies.get("JSESSIONID")).get();
		Assert.assertNotEquals(Status.OK.getStatusCode(), checkSessionAfterLogoutResponse.getStatus());
	}
}
