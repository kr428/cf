package cfiles.frontend.rap.tree;

public class TreeObject {
	private String name;
	private TreeParent parent = null;
	private String identifier = "";
	private String revision = "";

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public TreeObject(String name) {
		this.name = name;
	}

	public String getName() {
		if (null != this.parent) {
			return this.parent.getName() + "/" + this.name;
		}
		return name;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	public String toString() {
		return getName();
	}
}