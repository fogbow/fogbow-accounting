package org.fogbowcloud.accounting.identity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fogbowcloud.accounting.FogbowConstants;
import org.fogbowcloud.accounting.occi.model.Token;

public class TokenProvider {
	private static final Logger LOGGER = LogManager.getLogger(TokenProvider.class);
	private static final String DEFAULT_USER = "user";
	private Properties properties;
	private Token token;
	
	public TokenProvider(Properties properties, ScheduledExecutorService handleTokeUpdateExecutor) throws FileNotFoundException, IOException{
		this.properties = properties;
		this.token = createNewTokenFromFile(properties.getProperty(FogbowConstants.FOGBOW_VOMS_PROXY_FILEPATH));
		
		ScheduledExecutorService handleTokenUpdateExecutor = handleTokeUpdateExecutor;
		handleTokenUpdate(handleTokenUpdateExecutor, properties.getProperty(FogbowConstants.FOGBOW_VOMS_SERVER),  
				properties.getProperty(FogbowConstants.FOGBOW_VOMS_CERTIFICATE_PASSWORD) );
	}
	
	public TokenProvider(Properties properties) throws Exception {
		this(properties, Executors.newScheduledThreadPool(1));
	}
	
	protected void handleTokenUpdate(ScheduledExecutorService handleTokenUpdateExecutor,
			final String vomsServer, final String password) {
		LOGGER.debug("Turning on handle token update.");
		handleTokenUpdateExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					setToken(createToken(vomsServer, password));
				} catch (Throwable e) {
					LOGGER.error("Error while setting token.", e);
					try {
						setToken(createNewTokenFromFile(
								properties.getProperty(FogbowConstants.FOGBOW_VOMS_PROXY_FILEPATH)));
					} catch (IOException e1) {
						LOGGER.error("Error while getting token from file.", e);
					}
				}
			}
		}, 6, 6, TimeUnit.HOURS);
	}
	
	protected Token createToken(final String vomsServer, final String password) {
		VomsIdentity vomsIdentity = new VomsIdentity(new Properties());

		HashMap<String, String> credentials = new HashMap<String, String>();
		credentials.put("password", password);
		credentials.put("serverName", vomsServer);
		LOGGER.debug("Creating token update with serverName="
				+ vomsServer + " and password="
				+ password);

		Token token = vomsIdentity.createToken(credentials);
		LOGGER.debug("VOMS proxy updated. New proxy is " + token.toString());

		return token;
	}
	
	protected void setToken(Token token) {
		LOGGER.debug("Setting token to " + token);
		this.token = token;
	}
	
	protected Token createNewTokenFromFile(String certificateFilePath) throws FileNotFoundException, IOException {

		String certificate = IOUtils.toString(new FileInputStream(certificateFilePath)).replaceAll("\n", "");
		Date date = new Date(System.currentTimeMillis() + (long) Math.pow(10, 9));

		return new Token(certificate, DEFAULT_USER, date, new HashMap<String, String>());
	}
	
	public Token getToken() {
		return token;
	}
	
}
