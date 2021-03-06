package org.fogbowcloud.accounting;

import java.sql.SQLException;
import java.util.Properties;

import org.fogbowcloud.accounting.db.AccountingDataStore;
import org.fogbowcloud.accounting.json.JSONArrayBodyReader;
import org.fogbowcloud.accounting.json.JSONArrayBodyWriter;
import org.fogbowcloud.accounting.json.JSONObjectBodyReader;
import org.fogbowcloud.accounting.json.JSONObjectBodyWriter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class AccountingApplicationTestHelper extends ResourceConfig {
	private AccountingDataStore dataStore;

	public AccountingApplicationTestHelper() throws Exception {
		Properties props = ServerTestHelper.loadProperties();
		init(props);
	}

	protected AccountingApplicationTestHelper(Properties props) throws Exception {
		init(props);
	}
	
	private AccountingApplicationTestHelper init(final Properties properties) throws SQLException {
		this.dataStore = new AccountingDataStore(properties);
		packages(AccountingApplication.class.getPackage().toString());
		register(new AbstractBinder() {
            @Override
            protected void configure() {
            	bind(dataStore).to(AccountingDataStore.class);
                bind(properties).to(Properties.class);
            }
        });
		
		register(JSONObjectBodyReader.class);
		register(JSONObjectBodyWriter.class);
		register(JSONArrayBodyReader.class);
		register(JSONArrayBodyWriter.class);
		register(MultiPartFeature.class);
		return this;
	}
}
