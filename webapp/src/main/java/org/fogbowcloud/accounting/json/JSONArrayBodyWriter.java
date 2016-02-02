package org.fogbowcloud.accounting.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

@Produces("application/json")
@Provider
public class JSONArrayBodyWriter implements MessageBodyWriter<JSONArray> {

	@Override
	public long getSize(JSONArray arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4) {
		return arg0.toString().length();
	}

	@Override
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		return arg1 == JSONArray.class;
	}

	@Override
	public void writeTo(JSONArray arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6)
			throws IOException, WebApplicationException {
		IOUtils.write(arg0.toString(), arg6);
	}

}