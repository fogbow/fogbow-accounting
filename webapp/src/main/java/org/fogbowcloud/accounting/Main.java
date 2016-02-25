package org.fogbowcloud.accounting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.glassfish.grizzly.http.server.HttpServer;

public class Main {

    public static final int DEFAULT_PORT = 8080;
    
	protected static HttpServer startWebSocketServer() throws IOException, SQLException, ServletException {
    	Properties props = loadProperties();
    	String basePort = props.getProperty("base.port");
        HttpServer server = new ServerWrapper().create(
        		basePort == null ? DEFAULT_PORT : Integer.valueOf(basePort), AccountingApplication.class);
        server.start();
		AccountingDataStoreUpdater dataStoreUpdater = new AccountingDataStoreUpdater(props);
        return server; 
	}

	protected static Properties loadProperties() throws IOException,
			FileNotFoundException {
		Properties props = new Properties();
    	props.load(new FileInputStream("accounting.conf"));
		return props;
	}

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
	@SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
    	HttpServer server = null;
    	try {
    		server = startWebSocketServer();
            Thread.currentThread().suspend();
        } finally {
            server.shutdownNow();
        }
    }

}