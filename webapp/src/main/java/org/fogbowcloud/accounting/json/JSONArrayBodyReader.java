package org.fogbowcloud.accounting.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONTokener;

@Consumes("application/json")
@Provider
public class JSONArrayBodyReader implements MessageBodyReader<JSONArray> {

	private static final Logger LOGGER = LogManager.getLogger(JSONArrayBodyReader.class);
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg1 == JSONArray.class;
	}

	@Override
	public JSONArray readFrom(Class<JSONArray> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3, MultivaluedMap<String, String> arg4, InputStream arg5)
			throws IOException, WebApplicationException {
		try {
			return new JSONArray(new JSONTokener(arg5));
		} catch (Exception e) {
			LOGGER.error("Couldn't parse JSONArray input.", e);
			throw e;
		}
	}

}