package cfiles.frontend.rap.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.SessionSingletonBase;

import cfiles.frontend.rap.couchlink.CouchTreeLink;


public class SessionNexus {

	private CouchTreeLink couchLink = null;
	private String itemInSelection = "";
	private String fullTextTerm = "";

	public String getFullTextTerm() {
		return fullTextTerm;
	}

	private SessionNexus() {
	}

	public static SessionNexus getInstance() {
		return (SessionNexus) SessionSingletonBase
				.getInstance(SessionNexus.class);
	}

	public CouchTreeLink getCouchLink() {
		if (null == this.couchLink) {
			this.couchLink = new CouchTreeLink();
			this.couchLink.setDbName("importer");
			this.couchLink.setHostName(RWT.getRequest().getServerName());
		}
		return couchLink;
	}

	public String getHostBase() {
		return RWT.getRequest().getServerName() + ":"
				+ RWT.getRequest().getServerPort();
	}

	public void setItemInSelection(String identifier) {
		this.itemInSelection = identifier;
	}

	public Map<String, String> getCurrentEntity() {
		return this.getCouchLink().getEntity(this.getItemInSelection());
	}

	public String getItemInSelection() {
		return this.itemInSelection;
	}

	public Map<String, String> getEntityInSelection() {
		return this.getCouchLink().getEntity(this.getItemInSelection());
	}

	public void setFullTextTerm(String text) {
		this.fullTextTerm = text;
	}

}
