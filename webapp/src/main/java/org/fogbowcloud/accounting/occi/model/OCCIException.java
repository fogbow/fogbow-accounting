package org.fogbowcloud.accounting.occi.model;

import javax.ws.rs.core.Response.Status;

public class OCCIException extends javax.ws.rs.WebApplicationException {
	
	private static final long serialVersionUID = 6258329809337522269L;
	private ErrorType type;
	
	public OCCIException(ErrorType type, String description) {
		super(getStatus(type));
		this.type = type;
	}
	
	public ErrorType getType() {
		return type;
	}

	private static int getStatusCode(ErrorType type) {
		return getStatus(type).getStatusCode();
	}
	
	private static Status getStatus(ErrorType type) {
		switch (type) {
		case UNAUTHORIZED:
			return Status.UNAUTHORIZED;
		case NOT_FOUND:
			return Status.NOT_FOUND;
		case BAD_REQUEST:
			return Status.BAD_REQUEST;
		case NOT_ACCEPTABLE:
			return Status.NOT_ACCEPTABLE;
		case METHOD_NOT_ALLOWED:
			return Status.METHOD_NOT_ALLOWED;
		default:
			break;
		}
		return Status.INTERNAL_SERVER_ERROR;
	}
}