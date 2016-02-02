package org.fogbowcloud.accounting.resources;

import java.io.FileInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

@Path("/")
public class HomeResource {
	
	@GET
	public Response index() {
		String templateStr = "";
		try {
			templateStr = IOUtils.toString(new FileInputStream("templates/home.phtml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(templateStr).build();
	}

}
