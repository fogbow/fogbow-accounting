package org.fogbowcloud.accounting;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.glassfish.grizzly.http.server.HttpServer;

public class Main {

    public static final int DEFAULT_PORT = 8080;
    
    @SuppressWarnings("deprecation")
	private static void startWebSocketServer() throws IOException, SQLException, ServletException {
    	Properties props = new Properties();
    	props.load(new FileInputStream("accounting.conf"));
    	String basePort = props.getProperty("base.port");
        HttpServer server = new ServerWrapper().create(
        		basePort == null ? DEFAULT_PORT : Integer.valueOf(basePort), AccountingApplication.class);
        try {
            server.start();
            Thread.currentThread().suspend();
        } finally {
            server.shutdownNow();
        } 
	}

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
    	startWebSocketServer();
    }

}