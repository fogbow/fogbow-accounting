package org.fogbowcloud.accounting.resources;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fogbowcloud.accounting.Authentication;
import org.fogbowcloud.accounting.FogbowConstants;
import org.fogbowcloud.accounting.db.AccountingDataStore;
import org.fogbowcloud.accounting.model.AccountingInfo;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("usage")
public class UsageResource {
	private static final Logger LOGGER = LogManager.getLogger(UsageResource.class);

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
		String memberId = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_ID_PROP);
		if (memberId == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		List<AccountingInfo> accountingInfo = dataStore.getLocalMemberUsersConsumption(memberId);
		for (AccountingInfo info : accountingInfo) {
			usage.put(info.toJSON());
		}
		return Response.status(Status.OK)
				.entity(usage.toString()).build();
	}
	
	@GET
	@Path("/member/{memberId}/user/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsageByUser(@PathParam("memberId") String memberId, 
			@PathParam("userId") String userId) {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			List<AccountingInfo> userConsumption = dataStore.getUserConsumptionPerMemberByMemberId(userId, memberId);
			JSONArray usage = new JSONArray();
			for (AccountingInfo accountingInfo : userConsumption) {
				usage.put(accountingInfo.toJSON());
			}
			return Response.status(Status.OK)
					.entity(usage).build();
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
		String localMemberId = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_ID_PROP);
		JSONArray membersUsage = new JSONArray();
		
		List<AccountingInfo> consumption = dataStore.getLocalMemberConsumptionFromMembers(localMemberId);
		List<AccountingInfo> provision = dataStore.getLocalMemberProvisionToMembers(localMemberId);
		
		Map<String, JSONObject> usageMap = new HashMap<String, JSONObject>();
		//setting consumption
		for (AccountingInfo accountingInfo : consumption) {
			JSONObject memberUsage = new JSONObject();
			String memberId = accountingInfo.getProvidingMember();
			memberUsage.put("memberId", memberId);
			memberUsage.put("consumed", accountingInfo.getUsage());
			memberUsage.put("donated", 0);
			usageMap.put(memberId, memberUsage);
		}
		
		//setting donation and debt
		for (AccountingInfo accountingInfo : provision) {
			String memberId = accountingInfo.getRequestingMember();
			JSONObject memberUsage = usageMap.get(memberId);
			if (membersUsage == null) {
				memberUsage = new JSONObject();
				memberUsage.put("memberId", memberId);
				memberUsage.put("consumed", 0);
			}
			memberUsage.put("donated", accountingInfo.getUsage());
			usageMap.put(memberId, memberUsage);
		}
		
		for (Entry<String, JSONObject> entry : usageMap.entrySet()) {
			JSONObject memberUsage = entry.getValue();
			DecimalFormat df = new DecimalFormat("#.##");
			double nofBalance = 0, 
				consumed = memberUsage.getDouble("consumed"), 
				donated = memberUsage.getDouble("donated"), 
				balance = 0;
			balance = consumed - donated;
			nofBalance = Math.max(0, (consumed - donated));
			
			memberUsage.put("donated", df.format(donated));
			memberUsage.put("consumed", df.format(consumed));
			memberUsage.put("balance", df.format(balance));
			memberUsage.put("nofBalance", df.format(nofBalance));
			membersUsage.put(memberUsage);
		}
		
		return Response.status(Status.OK).entity(membersUsage.toString()).build();
	}
	
	@GET
	@Path("/consumedfrom/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocalMemberConsumptionPerUser(@PathParam("memberId") String memberId) {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		JSONArray usage = new JSONArray();
		String localMemberId = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_ID_PROP);
		List<AccountingInfo> accountingInfo = dataStore.getMemberConsumptionPerUser(localMemberId, memberId);
		LOGGER.debug("Local member consumption on " + memberId + " per user: " + accountingInfo.toString());
		for (AccountingInfo info : accountingInfo) {
			usage.put(info.toJSON());
		}
		return Response.status(Status.OK).entity(usage).build();
	}
	
	@GET
	@Path("/donatedto/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocalMemberDonationPerUser(@PathParam("memberId") String requestingMember) {
		if (!Authentication.checkAuthToken(request, properties)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		JSONArray usage = new JSONArray();
		String localMemberId = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_ID_PROP);
		List<AccountingInfo> accountingInfo = dataStore.getMemberConsumptionPerUser(requestingMember, localMemberId);
		LOGGER.debug("Local member donation to " + requestingMember + " per user: " + accountingInfo.toString());
		for (AccountingInfo info : accountingInfo) {
			usage.put(info.toJSON());
		}
		return Response.status(Status.OK).entity(usage).build();
	}
}