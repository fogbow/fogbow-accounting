package org.fogbowcloud.accounting.resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.fogbowcloud.accounting.db.AccountingDataStore;
import org.fogbowcloud.accounting.model.AccountingInfo;
import org.glassfish.grizzly.http.server.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class UsageResourceTest {
	private HttpServer server;
	private WebTarget target;
	private NewCookie sessionCookie;
	private Properties props;

	@Before
	public void setUp() throws Exception {
		props = ServerTestHelper.loadProperties();
		dropDatabase();
		server = new ServerTestHelper().startWebSocketServer();
		Client c = ClientBuilder.newClient();
		target = c.target(ServerTestHelper.BASE_URL);
		
		insertSampleData();
		doLogin();
	}
	
	@After
	public void tearDown() {
		server.shutdownNow();
	}
	
	private boolean doLogin() throws Exception {
		//get the proxy
		String proxyAuth = IOUtils.toString(new FileInputStream(new File("/tmp/x509up_u1350")));
		MultivaluedMap<String, String> formParam = new MultivaluedHashMap<String, String>();
		formParam.add("authToken", proxyAuth);
		
		//do the login
		Response loginResponse = target.path("auth/login").request(
				MediaType.APPLICATION_FORM_URLENCODED).post(Entity.form(formParam));
		
		if (loginResponse.getStatus() != Status.OK.getStatusCode()) {
			return false;
		}
		
		//save the session cookie
		Map<String, NewCookie> cookies = loginResponse.getCookies();
		sessionCookie = cookies.get("JSESSIONID");
		return true;
	}

	private void insertSampleData() throws Exception {
		try {
			AccountingDataStore dataStore = new AccountingDataStore(props);
			List<AccountingInfo> sampleData = new LinkedList<AccountingInfo>();
			String sampleDataJsonStr = IOUtils.toString(new FileInputStream(props.getProperty("test.sample.data.file")));
			JSONArray sampleDataJson = new JSONArray(sampleDataJsonStr);
			
			for (int i = 0; i < sampleDataJson.length(); i++) {
				JSONObject sampleInfo = sampleDataJson.optJSONObject(i);
				AccountingInfo info = new AccountingInfo(sampleInfo.getString("user"), 
						sampleInfo.getString("requesting_member"), sampleInfo.getString("providing_member"));
				info.addConsuption(sampleInfo.getDouble("usage"));
				sampleData.add(info);
			}
			
			dataStore.insertData(sampleData);
		} catch (Exception e) {
			throw e;
		}
	}

	private void dropDatabase() {
		String dataStoreUrl = props.getProperty("accounting.datastore.url");
		String[] dataStoreUrlPieces = dataStoreUrl.split(":");
		File dbFile = new File(dataStoreUrlPieces[dataStoreUrlPieces.length-1]);
		if (dbFile.exists()) {
			dbFile.delete();
		}
	}
	
	@Test
	public void testGetUsage() {
		String jsonStr = target.path("usage").request().cookie(sessionCookie).get(String.class);
		JSONArray response = new JSONArray(jsonStr);
		Assert.assertEquals(4, response.length());
		
		Double expectedUsage = 90.0;
		Double actualUsage = 0.0;
		for (int i = 0; i < response.length(); i++) {
			JSONObject accountingInfo = response.getJSONObject(i);
			actualUsage += accountingInfo.getDouble("usage");
		}
		Assert.assertEquals(expectedUsage, actualUsage);
	}
	
	@Test
	public void testGetUsageByUser() throws Exception {
		String testCasesJsonStr = IOUtils.toString(new FileInputStream("src/test/testCasesGetUsageByUser.json"));
		JSONArray testCases = new JSONArray(testCasesJsonStr);
		
		for (int i = 0; i < testCases.length(); i++) {
			JSONObject testCase = testCases.getJSONObject(i);
			String localMemberId = testCase.getString("memberId");
			String localUserId = testCase.getString("user");
			String response = target.path("usage/member/" + localMemberId + "/user/" + localUserId)
					.request().cookie(sessionCookie).get(String.class);
			JSONArray usage = new JSONArray(response);
			Assert.assertEquals(testCase.getInt("expectedEntries"), usage.length());
			
			Double expectedUsage = testCase.getDouble("expectedUsage");
			Double actualUsage = 0.0;
			for (int j = 0; j < usage.length(); j++) {
				JSONObject accountingInfo = usage.getJSONObject(j);
				actualUsage += accountingInfo.getDouble("usage");
			}
			Assert.assertEquals(expectedUsage, actualUsage);
		}
	}
	
	
}
