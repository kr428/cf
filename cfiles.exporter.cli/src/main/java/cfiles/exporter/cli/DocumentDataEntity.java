package cfiles.exporter.cli;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "document", namespace = "http://cfiles.zimmer428.net/document")
public class DocumentDataEntity {

	private String id = "";
	private String revision = "";
	private String path = "";
	private String keywords = "";
	private String comment = "";
	private String content = "";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
