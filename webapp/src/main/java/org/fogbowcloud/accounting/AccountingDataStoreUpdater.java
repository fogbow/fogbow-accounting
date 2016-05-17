package org.fogbowcloud.accounting;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fogbowcloud.accounting.db.AccountingDataStore;
import org.fogbowcloud.accounting.identity.TokenProvider;
import org.fogbowcloud.accounting.model.AccountingInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class AccountingDataStoreUpdater {
	private static final Logger LOGGER = LogManager.getLogger(AccountingDataStoreUpdater.class);
	private static final String DEFAULT_UPDATE_INTERVAL = "5";
	private Properties properties;
	private String managerUrl;
	private WebTarget target;
	private TokenProvider tokenProvider;
	private AccountingDataStore dataStore;
	
	public AccountingDataStoreUpdater(Properties properties, ScheduledExecutorService handleDataUpdateExecutor) {
		this.properties = properties;
		this.dataStore = new AccountingDataStore(properties);
		this.managerUrl = properties.getProperty(FogbowConstants.FOGBOW_MANAGER_URL_PROP);
		this.target = ClientBuilder.newClient().target(managerUrl);
		try {
			this.tokenProvider = new TokenProvider(properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		ScheduledExecutorService handleDataStoreUpdateExecutor = handleDataUpdateExecutor;
		handleDataStoreUpdate(handleDataStoreUpdateExecutor);
	}
	
	public AccountingDataStoreUpdater(Properties properties) {
		this(properties, Executors.newScheduledThreadPool(1));
	}
	
	private void handleDataStoreUpdate(ScheduledExecutorService handleDataStoreUpdateExecutor) {
		LOGGER.debug("Starting to handle dataStore update");
		long updateInterval = Long.valueOf(properties.getProperty(
				FogbowConstants.ACCOUNTING_DATASTORE_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL));
		handleDataStoreUpdateExecutor.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				fetchAccountingFromManager();
			}
		}, updateInterval, updateInterval, TimeUnit.MINUTES);
	}
	
	private void fetchAccountingFromManager() {
		LOGGER.debug("Fetching accounting data");
		try {
			String response = target.path(FogbowConstants.COMPUTE_ACCOUNTING_TERM).request()
					.header(FogbowConstants.CONTENT_TYPE_HEADER_ATTR, FogbowConstants.CONTENT_TYPE_TEXT_OCCI)
					.header(FogbowConstants.AUTH_TOKEN_HEADER_ATTR, this.tokenProvider.getToken().getAccessId().replaceAll("\n", ""))
					.accept(MediaType.APPLICATION_JSON).get(String.class);
			JSONObject responseJson = new JSONObject(response);
			JSONArray accounting = responseJson.getJSONArray("accounting");
			List<AccountingInfo> toUpdate = new LinkedList<AccountingInfo>();
			for (int i = 0; i < accounting.length(); i++) {
				AccountingInfo accountingInfo = AccountingInfo.fromJSON(accounting.getJSONObject(i).toString());
				toUpdate.add(accountingInfo);
			}
			if (toUpdate.size() > 0) {
				dataStore.update(toUpdate);
			}
		} catch (Exception e) {
			LOGGER.debug("Error while trying to fetch accounting data", e);
		}
	}

}
