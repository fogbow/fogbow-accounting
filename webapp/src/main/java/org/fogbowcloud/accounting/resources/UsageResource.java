package org.fogbowcloud.accounting.resources;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.accounting.Authentication;
import org.fogbowcloud.accounting.FogbowConstants;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("usage")
public class UsageResource {
	
	@Inject
	private Properties properties;
	
	@Context
	private HttpServletRequest request;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsage() {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		try {
			String fakeAccountingFilename = "fake.accounting.local.json";
			String fakeAccountingJsonStr = IOUtils.toString(new FileInputStream(fakeAccountingFilename));
			return Response.status(Status.OK)
					.entity(fakeAccountingJsonStr).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK)
				.entity(new JSONArray().toString()).build();
	}
	
	@GET
	@Path("/user/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsageByUser(@PathParam("userId") String userId) {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			String fakeAccountingFilename = "fake.accounting.local." + userId.toLowerCase() + ".json";
			String fakeAccountingJsonStr = IOUtils.toString(new FileInputStream(fakeAccountingFilename));
			return Response.status(Status.OK)
					.entity(fakeAccountingJsonStr).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response.status(Status.NOT_FOUND).entity("User not found").build();
	}
	
	@GET
	@Path("/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMembersUsage() {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		JSONArray membersUsage = new JSONArray();
		
		HttpSession session = request.getSession();
		Object sessionAuthToken = session.getAttribute(FogbowConstants.SESSION_AUTH_TOKEN_ATTRIBUTE);
		String managerUrl = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_URL_PROP);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(managerUrl + FogbowConstants.USAGE_TERM + FogbowConstants.MEMBER_TERM);
		String responseStr = target.request()
				.header(FogbowConstants.CONTENT_TYPE_HEADER_ATTR, FogbowConstants.CONTENT_TYPE_TEXT_OCCI)
				.header(FogbowConstants.AUTH_TOKEN_HEADER_ATTR, sessionAuthToken.toString())
				.accept(MediaType.TEXT_PLAIN)
				.get(String.class);
		String[] usageLines = responseStr.split("\n");
		for (String usageLine : usageLines) {
			String idMember = "";
			double balance = 0, consumed = 0, donated = 0;
			String[] usageProperties = usageLine.split(",");
			for (String property : usageProperties) {
				String[] propertyPair = property.trim().split("=");
				String propertyKey = propertyPair[0];
				String propertyValue = propertyPair[1];
				if (propertyKey.equals("memberId")) {
					idMember = propertyValue;
				} else if (propertyKey.equals("donated")) {
					donated = Double.valueOf(propertyValue);
				} else if (propertyKey.equals("consumed")) {
					consumed = Double.valueOf(propertyValue);
				}
			}
			balance = Math.max(0, (consumed - donated) + Math.sqrt(donated));
			
			DecimalFormat df = new DecimalFormat("#.##");
			JSONObject memberUsage = new JSONObject();
			memberUsage.put("memberId", idMember);
			memberUsage.put("donated", df.format(donated));
			memberUsage.put("consumed", df.format(consumed));
			memberUsage.put("debit", df.format(balance));
			membersUsage.put(memberUsage);
		}
		
		return Response.status(Status.OK).entity(membersUsage.toString()).build();
	}
}