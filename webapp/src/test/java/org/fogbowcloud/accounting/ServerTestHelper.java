package org.fogbowcloud.accounting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.glassfish.grizzly.http.server.HttpServer;

public class ServerTestHelper {
	private static final int DEFAULT_PORT = 8080;
	public static final String BASE_URL = "http://localhost:8080/";
	
	public HttpServer startWebSocketServer() throws IOException, SQLException, ServletException {
    	Properties props = loadProperties();
    	String basePort = props.getProperty("base.port");
        HttpServer server = new ServerWrapper().create(
        		basePort == null ? DEFAULT_PORT : Integer.valueOf(basePort), AccountingApplicationTestHelper.class);
        server.start();
        return server; 
	}
	
	public static Properties loadProperties() {
		Properties props = new Properties();
		props.put("base.port", "8080");
		props.put("fogbow.manager.url", "http://150.165.15.81:8182");
		props.put("fogbow.manager.id", "servers.lsd.ufcg.edu.br");
		props.put("accounting.datastore.url", "jdbc:sqlite:fogbowAccountingTestDB.sqlite");
		props.put("test.sample.data.file", "src/test/testsampledata.json");
		return props;
	}
	
}
