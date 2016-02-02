package org.fogbowcloud.accounting;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.fogbowcloud.accounting.json.JSONArrayBodyReader;
import org.fogbowcloud.accounting.json.JSONArrayBodyWriter;
import org.fogbowcloud.accounting.json.JSONObjectBodyReader;
import org.fogbowcloud.accounting.json.JSONObjectBodyWriter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class AccountingApplication extends ResourceConfig {

	public AccountingApplication() throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream("accounting.conf"));
		init(props);
	}
	
	protected AccountingApplication(Properties props) throws Exception {
		init(props);
	}
	
	private AccountingApplication init(final Properties properties) throws SQLException {
		packages(AccountingApplication.class.getPackage().toString());
		register(new AbstractBinder() {
            @Override
            protected void configure() {
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