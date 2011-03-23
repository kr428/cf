package cfiles.exporter.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataExporter {
	private static final Logger logger = LoggerFactory
			.getLogger(DataExporter.class);

	private boolean bulkXml = false;

	public boolean isBulkXml() {
		return bulkXml;
	}

	public void setBulkXml(boolean bulkXml) {
		this.bulkXml = bulkXml;
	}

	private Database database = null;
	private String outputFolder = "/opt/data/export";

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public Database getDatabase() {
		return database;
	}

	public DataExporter() {
		this.database = new Database("localhost", "importer");
	}

	public DataExporter(String host, String database) {
		this.database = new Database(host, database);
	}

	public void run(String root) {
		final List<String> documents = this.getValidDocIds(this
				.transformActiveRoot(root));
		logger.debug("documents to be exported: " + documents);

		final Map<String, DocumentDataEntity> entities = new HashMap<String, DocumentDataEntity>();

		for (String document : documents) {
			try {
				entities.putAll(this.processDocument(document));
			} catch (IOException ex) {
				logger.error("Export FAILED in {}", document);
				logger.debug("Trace:", ex);
			}
		}

		if (!entities.isEmpty()) {
			final DocumentDataEntities ddes = new DocumentDataEntities(
					entities.values());
			try {
				final JAXBContext context = JAXBContext.newInstance(ddes
						.getClass());
				final Marshaller marshaller = context.createMarshaller();
				marshaller.marshal(ddes,
						new FileWriter(this.getOutputFolder() + File.separator
								+ "export-" + (new Date()).getTime() + ".xml"));
			} catch (Exception ex) {
				logger.error("FAIL: {}", ex.getMessage());
				logger.debug("Trace: ", ex);
			}

		}

	}

	private String transformActiveRoot(String givenRoot) {
		String activeRoot = "";
		if (givenRoot.startsWith("/")) {
			activeRoot = givenRoot;
		} else {
			activeRoot = "/" + givenRoot;
		}
		activeRoot = activeRoot.replace("/", "::");
		return activeRoot;
	}

	private List<String> getValidDocIds(String activeRoot) {
		final List<String> returns = new ArrayList<String>();
		final ViewResult<Map> result = this.getDatabase().listDocuments(null,
				null);

		for (ValueRow<Map> m : result.getRows()) {
			if (m.getId().startsWith(activeRoot)) {
				returns.add(m.getId());
			}
		}
		return returns;
	}

	private Map<String, DocumentDataEntity> processDocument(String documentId)
			throws IOException {
		logger.debug("-> id: {}", documentId);

		final Map<String, DocumentDataEntity> ddes = new HashMap<String, DocumentDataEntity>();

		final Map doc = this.getDatabase().getDocument(Map.class, documentId);

		logger.debug(" -> rev: " + doc.get("_rev"));
		logger.debug(" -> stored: " + doc.get("stored"));

		final DocumentDataEntity dde = new DocumentDataEntity();
		dde.setRevision("" + doc.get("_rev"));
		dde.setId("" + documentId);
		dde.setPath("" + doc.get("path"));
		dde.setKeywords("" + doc.get("keywords"));

		final String fileNameBase = FilenameUtils.getBaseName(("" + doc
				.get("name")).replaceAll(" ", "_"));
		final String fileNameExt = FilenameUtils.getExtension(("" + doc
				.get("name")).replaceAll(" ", "_"));

		final File storePath = (new File(this.getOutputFolder()
				+ File.separator + doc.get("path")).getParentFile());

		if (!storePath.exists()) {
			storePath.mkdirs();
		}

		try {
			String xmlPath = storePath.getAbsolutePath() + File.separator
					+ fileNameBase + ".xml";

			if (this.isBulkXml()) {
				ddes.put(xmlPath, dde);
			} else {
				final JAXBContext context = JAXBContext.newInstance(dde
						.getClass());
				final Marshaller marshaller = context.createMarshaller();
				marshaller.marshal(dde, new FileWriter(xmlPath));
			}
			FileUtils.copyFile(new File("" + doc.get("stored")), new File(
					storePath.getAbsolutePath() + File.separator + fileNameBase
							+ "." + fileNameExt));

		} catch (JAXBException ex) {
			throw new IOException(ex);
		}

		return ddes;
	}
}
