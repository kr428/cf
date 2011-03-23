package cfiles.frontend.rap.couchlink;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.exception.DataAccessException;

import cfiles.frontend.rap.core.SessionNexus;
import cfiles.frontend.rap.tree.TreeObject;
import cfiles.frontend.rap.tree.TreeParent;

public class CouchTreeLink {

	private String hostName = "localhost";
	private String dbName = "importer";

	private Database database = null;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public Database getDatabase() {
		if (null == this.database) {
			this.database = new Database(this.getHostName(), this.getDbName());
		}
		return database;
	}

	public TreeObject createHierarchy() {
		final TreeParent tp = new TreeParent("ROOT");

		try {
			for (String r : this.listDataStore()) {
				tp.addChild(new TreeObject(r));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			tp.addChild(new TreeObject("FAIL"));
		}
		return tp;
	}

	public List<String> listDataStore() {

		final List<String> roots = new ArrayList<String>();

		try {

			final ViewResult<Map> listDocuments = this.getDatabase().query(
					"_design/parentView/_view/parentView", Map.class, null,
					null, null);

			final List<ValueRow<Map>> rows = listDocuments.getRows();

			for (ValueRow<Map> m : rows) {
				if (!roots.contains("" + m.getKey())) {
					roots.add("" + m.getKey());
				}
			}

		} catch (DataAccessException ex) {
			if (ex.getMessage().contains("404")) {
				System.out.println("404? trying to init database structure...");
				this.createDBStructure();
			}
		}
		return roots;
	}

	private void createDBStructure() {
		final Server s = new ServerImpl("localhost");

		try {
			s.createDatabase(this.getDbName());
		} catch (Exception ex) {
			System.err.println("database creation failed: " + ex);
		}

		try {
			this.database = new Database(this.getHostName(), this.getDbName());

			final DesignDocument parentView = new DesignDocument("parentView");
			final String PARENT_VIEW = "function(doc) { if (doc.type == 'file'){emit(doc.parent, doc.name); }}";
			parentView.addView("parentView", new View(PARENT_VIEW));
			this.getDatabase().createDocument(parentView);

			final Map<String, Object> ftx = new HashMap<String, Object>();

			ftx.put("_id", "_design/fulltext");

			final Map<String, Object> ftxView = new HashMap<String, Object>();

			final Map<String, Object> ftxByName = new HashMap<String, Object>();
			ftxByName
					.put("index",
							"function(doc) { var ret=new Document(); ret.add(doc.name); return ret }");
			final Map<String, Object> ftxByPath = new HashMap<String, Object>();
			ftxByPath
					.put("index",
							"function(doc) { var ret=new Document(); ret.add(doc.path); return ret }");
			final Map<String, Object> ftxFull = new HashMap<String, Object>();
			ftxFull.put(
					"index",
					"function(doc) { var ret = new Document(); ret.add(doc.path); ret.add(doc.name); ret.add(doc.content); if (doc._attachments) { for (var i in doc._attachments) { ret.attachment('attachment',i);} if (doc.annotation) {ret.add(doc.annotation);} }return ret; }");

			ftxView.put("by_name", ftxByName);
			ftxView.put("by_path", ftxByPath);
			ftxView.put("full", ftxFull);

			ftx.put("fulltext", ftxView);
			this.getDatabase().createDocument(ftx);

		} catch (Exception ex) {
			System.err.println("design document creation failed:");
		}

	}

	public Map<String, String> getEntity(String id) {
		final Map<String, String> stuff = new HashMap<String, String>();

		final Map outcome = this.getDatabase().getDocument(Map.class, "" + id);

		for (Object key : outcome.keySet()) {
			stuff.put("" + key, "" + outcome.get(key));
		}

		return stuff;
	}

	public Map<String, String> listFilesForText(String text) {
		try {
			ViewResult<Map> listDocuments = this.getDatabase()
					.query((new URI("_fti/_design/fulltext/full?q=attachment:"
							+ text).toString()), Map.class, null, null, null);

			if (listDocuments.getRows().isEmpty()) {
				listDocuments = this
						.getDatabase()
						.query((new URI("_fti/_design/fulltext/full?q=" + text)
								.toString()),
								Map.class, null, null, null);
			}

			final List<ValueRow<Map>> rows = listDocuments.getRows();
			final List<String> roots = new ArrayList<String>();
			final Map<String, String> outcome = new HashMap<String, String>();

			for (ValueRow<Map> row : rows) {
				roots.add(row.getId());
			}

			for (String root : roots) {
				final Map<String, String> data = this.getDatabase()
						.getDocument(Map.class, root);
				outcome.put(data.get("_id"), data.get("name"));
			}

			return outcome;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new HashMap<String, String>();
		}
	}

	public List<ValueContainer> listFilesForRoot(String root) {

		try {

			final ViewResult<Map> listDocuments = this.getDatabase().query(
					(new URI("_design/parentView/_view/parentView?key=%22"
							+ (root.replace(" ", "%20")) + "%22").toString()),
					Map.class, null, null, null);

			final List<ValueRow<Map>> rows = listDocuments.getRows();
			final List<ValueContainer> roots = new ArrayList<ValueContainer>();

			final List<String> seenRoots = new ArrayList<String>();

			for (ValueRow<Map> m : rows) {
				if (!seenRoots.contains("" + m.getId())) {
					seenRoots.add("" + m.getId());
				}
				System.out.println("value: " + m.getValue());
			}

			for (String id : seenRoots) {
				final Map<String, String> e = this.getEntity(id);
				roots.add(new ValueContainer(e.get("name"), e.get("_id"), e
						.get("_rev")));
			}

			return roots;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<ValueContainer>();
		}
	}

	public void storeAnnotation(String docId, String annotationText) {
		final Map<String, String> doc = this.getDatabase().getDocument(
				Map.class, docId);
		doc.put("annotation", annotationText);
		this.getDatabase().updateDocument(doc);
		System.out.println("updated " + doc);
	}

	public void storeState(String docId, String state) {
		final Map<String, Object> doc = this.getDatabase().getDocument(
				Map.class, docId);

		doc.put("userstate", state);
		this.getDatabase().updateDocument(doc);
		System.out.println("updated state for" + doc);

	}

	public void storeKeywords(String docId, String keywords) {
		final Map<String, Object> doc = this.getDatabase().getDocument(
				Map.class, docId);

		final List<String> ks = new ArrayList<String>();
		doc.put("keywords", keywords);

		this.getDatabase().updateDocument(doc);
		System.out.println("updated " + doc);
	}

	public String getAttachmentContent(String docId) {
		try {
			return new String(this.getDatabase().getAttachment(docId,
					"content.txt"));
		} catch (Exception ex) {
			return "FAIL: " + ex.getMessage();
		}
	}

	public String getCouchState() {
		return this.getDatabase().getServer().get("/").getContentAsString();
	}

	public String getIndexerState() {
		return this.getDatabase().getServer()
				.get("/importer/_fti/_design/fulltext/full")
				.getContentAsString();
	}

	public String getDBChanges() {
		return this.getDatabase().getServer().get("/importer/_changes")
				.getContentAsString();
	}

	public class ValueContainer {

		private String name = "";
		private String id = "";
		private String revision = "";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

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

		public ValueContainer(String name, String id, String revision) {
			super();
			this.name = name;
			this.id = id;
			this.revision = revision;
		}
	}

	public SortedMap<String, Revision> getRevisionHistoryForDocument(
			String docId) {
		System.out.println("to get revisions for " + docId);
		final String path = this.getEntity(docId).get("stored");
		final SortedMap<String, Revision> revs = new TreeMap<String, CouchTreeLink.Revision>();

		final SortedMap<String, String> results = new TreeMap<String, String>();
		final Map outcome = this
				.getDatabase()
				.getServer()
				.get("/" + this.getDatabase().getName() + "/"
						+ docId.replace(" ", "%20") + "?revs_info=true")
				.getContentAsMap();
		final List revisions = (List) outcome.get("_revs_info");

		for (Object o : revisions) {
			final Map line = (Map) o;
			results.put((String) line.get("rev"),
					path + "." + ((String) line.get("rev")));
		}

		String lastSeenFile = "-";

		for (String rev : results.keySet()) {
			final File revFile = new File(path + "." + rev);
			if (revFile.exists()) {
				lastSeenFile = revFile.getAbsolutePath();
			}

			final Revision revision = new Revision();
			revision.setDocumentId(docId);
			revision.setRevision(rev);
			revision.setFile(lastSeenFile);
			revs.put(rev, revision);
		}
		System.out.println("--> revs: " + revs.values());
		return revs;
	}

	public static void main(String[] args) {
		final CouchTreeLink ctl = new CouchTreeLink();
		System.out
				.println("result: "
						+ ctl.getRevisionHistoryForDocument("::ROOT::login_tutorial.pdf"));
	}

	public class Revision {

		private String documentId = "";
		private String revision = "";
		private String file = "";

		public String getDocumentId() {
			return documentId;
		}

		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}

		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public String getFile() {
			if (SessionNexus.getInstance().getCurrentEntity().get("_rev")
					.equals(this.getRevision())) {
				return SessionNexus.getInstance().getCurrentEntity()
						.get("stored");
			}
			return file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		@Override
		public String toString() {
			return "Revision [documentId=" + documentId + ", revision="
					+ revision + ", file=" + file + "]";
		}

	}

}
