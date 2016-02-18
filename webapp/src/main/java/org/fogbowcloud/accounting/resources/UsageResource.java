package org.fogbowcloud.accounting.resources;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.fogbowcloud.accounting.Authentication;
import org.fogbowcloud.accounting.db.AccountingDataStore;
import org.fogbowcloud.accounting.model.AccountingInfo;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("usage")
public class UsageResource {

	@Inject
	private AccountingDataStore dataStore;
	
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
		JSONArray usage = new JSONArray();
		List<AccountingInfo> accountingInfo = dataStore.getMemberAccountingInfoPerUser("servers.lsd.ufcg.edu.br");
		for (AccountingInfo info : accountingInfo) {
			usage.put(info.toJSON());
		}
		return Response.status(Status.OK)
				.entity(usage.toString()).build();
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
		
		List<AccountingInfo> consumption = dataStore.getConsumptionPerMember();
		List<AccountingInfo> provision = dataStore.getProvisionPerMember();
		
		Map<String, JSONObject> usageMap = new HashMap<String, JSONObject>();
		//setting consumption
		for (AccountingInfo accountingInfo : consumption) {
			JSONObject memberUsage = new JSONObject();
			memberUsage.put("memberId", accountingInfo.getRequestingMember());
			memberUsage.put("consumed", accountingInfo.getUsage());
			usageMap.put(accountingInfo.getRequestingMember(), memberUsage);
		}
		
		//setting donation and debit
		for (AccountingInfo accountingInfo : provision) {
			String memberId = accountingInfo.getProvidingMember();
			JSONObject memberUsage = usageMap.get(memberId);
			
			DecimalFormat df = new DecimalFormat("#.##");
			double balance = 0, consumed = 0, donated = 0;
			donated = accountingInfo.getUsage();
			consumed = memberUsage.getDouble("consumed");
			balance = Math.max(0, (consumed - donated) + Math.sqrt(donated));
			
			memberUsage.put("donated", df.format(donated));
			memberUsage.put("consumed", df.format(consumed));
			memberUsage.put("debit", df.format(balance));
			membersUsage.put(memberUsage);
		}
		
		return Response.status(Status.OK).entity(membersUsage.toString()).build();
	}
}