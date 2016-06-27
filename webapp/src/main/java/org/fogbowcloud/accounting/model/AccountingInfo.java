package org.fogbowcloud.accounting.model;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;


public class AccountingInfo {
	
	private String user;
	private String requestingMember;
	private String providingMember;
	private double usage;
	
	public AccountingInfo(String user, String requestingMember, String providingMember) {
		this.user = user;
		this.requestingMember = requestingMember;
		this.providingMember = providingMember;
		this.usage = 0;
	}
	
	public void addConsuption(double consuption) {
		this.usage += consuption;
	}

	public String getUser() {
		return user;
	}

	public String getRequestingMember() {
		return requestingMember;
	}

	public String getProvidingMember() {
		return providingMember;
	}

	public double getUsage() {
		return usage;
	}
	
	public JSONObject toJSON() {
		JSONObject infoJSON = new JSONObject();
		infoJSON.put("user", this.user);
		infoJSON.put("requestingMember", this.requestingMember);
		infoJSON.put("providingMember", this.providingMember);
		DecimalFormat df = new DecimalFormat("#.##");
		infoJSON.put("usage", df.format(this.usage));
		return infoJSON;
	}
	
	public static AccountingInfo fromJSON(String accountingEntryJSON) throws JSONException {
		JSONObject jsonObject = new JSONObject(accountingEntryJSON);
		AccountingInfo accountingEntry = new AccountingInfo(jsonObject.optString("user"),
				jsonObject.optString("requestingMember"), jsonObject.optString("providingMember"));
		accountingEntry.addConsuption(Double.parseDouble(jsonObject.optString("usage")));
		return accountingEntry;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccountingInfo) {
			AccountingInfo other = (AccountingInfo) obj;
			return getUser().equals(other.getUser())
					&& getRequestingMember().equals(other.getRequestingMember())
					&& getProvidingMember().equals(other.getProvidingMember())
					&& (getUsage() - other.getUsage() <= 0.00000001); 
		}
		return false;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return toJSON().toString();
	}
}