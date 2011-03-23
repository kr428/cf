package cfiles.frontend.rap.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.couchlink.CouchTreeLink;
import cfiles.frontend.rap.couchlink.CouchTreeLink.ValueContainer;


public class ViewContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private String nodeFilter = "";

	public String getNodeFilter() {
		return nodeFilter;
	}

	public void setNodeFilter(String nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	private CouchTreeLink couchLink = null;
	private List<String> hierarchySet = new ArrayList<String>();

	public CouchTreeLink getCouchLink() {
		return couchLink;
	}

	public void setCouchLink(CouchTreeLink couchLink) {
		this.couchLink = couchLink;
	}

	public List<String> getHierarchySet() {
		return hierarchySet;
	}

	public void setHierarchySet(List<String> hierarchySet) {
		this.hierarchySet = new ArrayList<String>();
		this.hierarchySet.addAll(hierarchySet);
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		System.out.println("getChildren: " + parent);
		if (parent instanceof TreeParent) {
			System.out.println("treeparent: " + parent);
			final List<String> currentNextLevel = new ArrayList<String>();
			final List<TreeObject> ps = new ArrayList<TreeObject>();
			String name = ((TreeParent) parent).getName();
			String origName = name;

			if (name.equals("root.fulltext")) {
				return ((TreeParent) parent).getChildren();
			}

			if (((TreeParent) parent).getName().contains("root.fulltext")) {
				final List<TreeObject> obs = new ArrayList<TreeObject>();
				final Map<String, String> ids = SessionNexus.getInstance()
						.getCouchLink().listFilesForText((name.split("/")[1]));
				for (String key : ids.keySet()) {
					final TreeObject o = new TreeObject(ids.get(key));
					o.setParent((TreeParent) parent);
					o.setIdentifier(key);
					obs.add(o);
				}
				return obs.toArray();
			}

			if (!name.startsWith("/")) {
				name = "/" + name;
			}

			if (!name.endsWith("/")) {
				name = name + "/";
			}

			System.out.println("processed name: " + name);
			for (String s : this.getHierarchySet()) {
				if (!s.startsWith("/")) {
					s = "/" + s;
				}
				System.out.println("string in list: " + s);
				if (s.startsWith(name)) {
					System.out.println("-> startsWith " + name);
					String[] fields = s.replaceFirst(name, "").split("/");
					String validCn = "";
					if (fields.length > 0) {
						final String cn = fields[0];
						System.out.println("effective cn: " + cn);

						if (!this.getNodeFilter().isEmpty()) {
							if (cn.contains(this.getNodeFilter())) {
								System.out.println("filter-match: added cn: "
										+ cn);
								validCn = cn;
							}
						} else {
							System.out.println("added cn: " + cn);
							validCn = cn;
						}

						if ((!validCn.isEmpty()) && (!(validCn.equals("/")))
								&& (!currentNextLevel.contains(validCn))) {
							currentNextLevel.add(validCn);
						}
					}

				}
			}

			System.out.println("effective cns: " + currentNextLevel);
			for (String c : currentNextLevel) {
				final TreeParent tp = new TreeParent(c);
				tp.setParent((TreeParent) parent);

				ps.add(tp);
			}

			if (!(name.equals(""))) {
				System.out.println("searching files: " + origName);
				final List<ValueContainer> currentFiles = this.getCouchLink()
						.listFilesForRoot(origName);
				System.out.println("files for origName: " + currentFiles);
				for (ValueContainer f : currentFiles) {

					if (!currentNextLevel.contains(f.getId())) {
						if (!this.getNodeFilter().isEmpty()) {
							if (f.getName().contains(this.getNodeFilter())) {
								final TreeObject o = new TreeObject(f.getName());
								o.setIdentifier(f.getId());
								o.setRevision(f.getRevision());
								o.setParent((TreeParent) parent);
								ps.add(o);
							}

						} else {
							final TreeObject o = new TreeObject(f.getName());
							o.setIdentifier(f.getId());
							o.setRevision(f.getRevision());
							o.setParent((TreeParent) parent);
							ps.add(o);
						}
					}

				}
			}

			return (TreeObject[]) ps.toArray(new TreeObject[] {});

		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			// return ((TreeParent) parent).hasChildren();
			return true;
		return false;
	}

	public TreeParent getRoot() {
		return new TreeParent("");
	}

	public TreeParent getFullTextRoot(String text) {
		final TreeParent p = new TreeParent("root.fulltext");
		if (!text.isEmpty()) {
			final TreeParent child = new TreeParent(text);
			child.setParent(p);
			p.addChild(child);
		}
		return p;
	}

}