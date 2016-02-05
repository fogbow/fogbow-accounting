package org.fogbowcloud.accounting.resources;

import java.io.FileInputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.accounting.Authentication;
import org.json.JSONArray;

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
			String fakeAccountingFilename = properties.getProperty("fake.accounting.file");
			String fakeAccountingJsonStr = IOUtils.toString(new FileInputStream(fakeAccountingFilename));
			return Response.status(Status.OK)
					.entity(fakeAccountingJsonStr).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK)
				.entity(new JSONArray().toString()).build();
	}
}