package cfiles.importer.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.NotFoundException;

public class ImporterApp {

	private ApertureMetadataReader apertureReader = new ApertureMetadataReader();
	private AutoDetectParser parser = new AutoDetectParser();
	private static final Logger logger = LoggerFactory
			.getLogger(ImporterApp.class);

	private List<String> fileTypes = Arrays.asList(new String[] { "pdf", "xls",
			"doc", "ppt", "jpg", "hpgl", "odt", "odp", "ods", "png", "html",
			"xml" });

	public List<String> getFileTypes() {
		return fileTypes;
	}

	public void setFileTypes(List<String> fileTypes) {
		this.fileTypes = fileTypes;
	}

	private Database systemDatabase = null;
	private CommandLine options;
	private Database database = null;
	private File folderToStartWith = null;
	private boolean recurse = false;
	private boolean index = false;
	private boolean knownonly = false;
	private boolean replaceParent = false;
	private String targetPath = "";
	private boolean relocateAndDelete = false;

	private String defaultKeywords = "";
	private String rootPrefix = "/";
	private String currentWorkingDir = System.getProperty("user.dir");

	private List<File> filesToDelete = new ArrayList<File>();

	public ApertureMetadataReader getApertureReader() {
		return apertureReader;
	}

	public List<File> getFilesToDelete() {
		return filesToDelete;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public boolean isRelocateAndDelete() {
		return relocateAndDelete;
	}

	public void setRelocateAndDelete(boolean relocateAndDelete) {
		this.relocateAndDelete = relocateAndDelete;
	}

	public String getCurrentWorkingDir() {
		return currentWorkingDir;
	}

	public void setCurrentWorkingDir(String currentWorkingDir) {
		this.currentWorkingDir = currentWorkingDir;
	}

	public boolean isKnownonly() {
		return knownonly;
	}

	public void setKnownonly(boolean knownonly) {
		this.knownonly = knownonly;
	}

	public boolean isReplaceParent() {
		return replaceParent;
	}

	public void setReplaceParent(boolean replaceParent) {
		this.replaceParent = replaceParent;
	}

	public ImporterApp(CommandLine options) {
		logger.info("starting with... {}", options);
		this.options = options;
		String hostName = "localhost";
		String dbName = "importer";
		if (options.hasOption("host")) {
			hostName = options.getOptionValue("host");
		}
		if (options.hasOption("database")) {
			dbName = options.getOptionValue("database");
		}
		this.database = new Database(hostName, dbName);

		if (options.hasOption("r")) {
			this.recurse = true;
		}

		if (options.hasOption("i")) {
			this.index = true;
		}

		if (options.hasOption("t")) {
			this.knownonly = true;
		}

		if (options.hasOption("c")) {
			this.replaceParent = true;
		}

		if (options.hasOption("x")) {
			this.relocateAndDelete = true;
		}

		if (options.hasOption("m")) {
			this.targetPath = options.getOptionValue("m");
		}

		if (options.hasOption("ft")) {
			this.setKnownonly(true);
			final String fields = (String) (options.getOptionValue("ft"))
					.replace("\"", "");

			final ArrayList<String> types = new ArrayList<String>();
			if (!fields.contains(",")) {
				types.add(fields);
			} else {
				types.addAll(Arrays.asList(fields.split(",")));
			}
			this.setFileTypes(types);

		}

		final File file = new File(this.targetPath);

		if (!file.exists()) {
			boolean created = file.mkdirs();
			if (created) {
				System.err.println("targetFile not existent. ignored. ");
				this.relocateAndDelete = false;
			}
		}

		if (file.isFile()) {
			System.err.println("targetFile not a folder existent. ignored. ");
			this.relocateAndDelete = false;
		}

		if (!file.canWrite()) {
			System.err.println("targetFile exists but not writable. ignored. ");
			this.relocateAndDelete = false;
		}

		String folder = System.getProperty("java.io.tmpdir");
		if (options.hasOption("d")) {
			folder = options.getOptionValue("d");
		}

		this.folderToStartWith = new File(folder);

		if (options.hasOption("p")) {
			this.rootPrefix = options.getOptionValue("p");
		}

		if (options.hasOption("k")) {
			this.defaultKeywords = options.getOptionValue("k")
					.replace("\"", "");
		}

	}

	public CommandLine getOptions() {
		return options;
	}

	public void setOptions(CommandLine options) {
		this.options = options;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public File getFolderToStartWith() {
		return folderToStartWith;
	}

	public void setFolderToStartWith(File folderToStartWith) {
		this.folderToStartWith = folderToStartWith;
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public boolean isIndex() {
		return index;
	}

	public void setIndex(boolean index) {
		this.index = index;
	}

	public String getDefaultKeywords() {
		return defaultKeywords;
	}

	public void setDefaultKeywords(String defaultKeywords) {
		this.defaultKeywords = defaultKeywords;
	}

	public String getRootPrefix() {
		return rootPrefix;
	}

	public void setRootPrefix(String rootPrefix) {
		if (!rootPrefix.startsWith("/")) {
			rootPrefix = "/" + rootPrefix;
		}
		this.rootPrefix = rootPrefix;
	}

	@Override
	public String toString() {
		return "ImporterApp [options=" + options + ", database=" + database
				+ ", folderToStartWith=" + folderToStartWith + ", recurse="
				+ recurse + ", index=" + index + ", defaultKeywords="
				+ defaultKeywords + ", rootPrefix=" + rootPrefix + "]";
	}

	public static final Options generateOptions() {
		final Options o = new Options();

		final Option help = new Option("h", "print this help");
		final Option recurse = new Option("r", "recurse into subfolders");
		final Option index = new Option("i", "do content indexing");
		final Option cut = new Option("c",
				"cut / replace current folder (if indexing subfolders)");
		final Option knownonly = new Option("t", "known file types only");
		final Option list = new Option("l", "list database content");
		final Option expunge = new Option("x",
				"expunge source file (requires target path for move)");

		final Option rootPrefix = OptionBuilder.withArgName("rootPrefix")
				.hasArgs().withDescription("prefix to actual root path")
				.create("p");

		final Option defaultKeywords = OptionBuilder.withArgName("keywords")
				.hasArgs().withDescription("default keywords separated by ';'")
				.create("k");

		final Option startFolder = OptionBuilder
				.withArgName("folder")
				.hasArgs()
				.withDescription(
						"import base folder (defaults to java.io.tmpdir)")
				.create("d");

		final Option targetFolder = OptionBuilder
				.withArgName("targetFolder")
				.hasArgs()
				.withDescription(
						"migrate files (to target folder) before storing them")
				.create("m");

		final Option types = OptionBuilder.withArgName("filetypes").hasArgs()
				.withDescription("file types to be imported (separated by , )")
				.create("ft");

		final Option host = OptionBuilder.withArgName("dbhost").hasArgs()
				.withDescription("couchdb hostname (defaults to localhost)")
				.create("host");

		final Option database = OptionBuilder
				.withArgName("db")
				.hasArgs()
				.withDescription("couchdb database name (defaults to importer)")
				.create("database");

		o.addOption(help);
		o.addOption(recurse);
		o.addOption(knownonly);
		o.addOption(cut);
		o.addOption(index);
		o.addOption(database);
		o.addOption(host);
		o.addOption(startFolder);
		o.addOption(defaultKeywords);
		o.addOption(rootPrefix);
		o.addOption(list);
		o.addOption(expunge);
		o.addOption(types);
		o.addOption(targetFolder);
		return o;
	}

	public void listDataStore() throws Exception {
		final ViewResult<Map> listDocuments = this.getDatabase().query(
				"_design/parentView/_view/parentView", Map.class, null, null,
				null);

		final List<ValueRow<Map>> rows = listDocuments.getRows();
		if (logger.isDebugEnabled()) {
			for (ValueRow<Map> r : rows) {
				logger.debug("{} : {}", r.getKey(), r.getValue());
			}
		}
	}

	public void doImport() throws Exception {

		final Map<String, String> importStat = new HashMap<String, String>();
		importStat.put("runstart", "" + (new Date()).getTime());
		importStat.put("options", ""
				+ this.getOptions().getOptions().toString());

		logger.info("processing folder: {}", this.getFolderToStartWith());

		final AdaptableDirectoryWalker walker = new AdaptableDirectoryWalker();
		walker.process();

		if (this.isRelocateAndDelete()) {

			logger.info("cleaning up...");

			for (File f : this.getFilesToDelete()) {
				try {
					FileUtils.forceDelete(f);
				} catch (Exception ex) {
					logger.warn("FAIL: not deleted {}", f);

					logger.debug("Trace:", ex);

				}
			}
		}

		importStat.put("imported", "" + walker.getFilesImported());
		importStat.put("ignored", "" + walker.getFilesIgnored());
		importStat.put("failed", "" + walker.getFilesFailed());
		importStat.put("runend", "" + (new Date()).getTime());
		this.getSystemDatabase().createOrUpdateDocument(importStat);

	}

	protected void insertToCouch(File file, Map<String, String> data)
			throws IOException {
		final Map<String, Object> fileData = new HashMap<String, Object>();
		fileData.put("size", "" + file.length());
		fileData.put("lastmodified", "" + file.lastModified());

		if (file.isDirectory()) {
			fileData.put("type", "dir");
		} else {
			fileData.put("type", "file");
		}

		final List<String> perms = new ArrayList<String>();
		if (file.canExecute()) {
			perms.add("read");
		}
		if (file.canWrite()) {
			perms.add("write");
		}
		if (file.canExecute()) {
			perms.add("execute");
		}

		fileData.put("perms", (String[]) perms.toArray(new String[] {}));

		fileData.put("name", file.getName());
		fileData.put("stored", file.getAbsolutePath());

		String path = "";
		if (this.isReplaceParent()) {
			path = (this.getRootPrefix() + (file.getAbsolutePath().replaceAll(
					"" + File.separator, "/").replace(this
					.getFolderToStartWith().getAbsolutePath(), ""))).replace(
					"//", "");

		} else {
			path = this.getRootPrefix()
					+ (file.getAbsolutePath().replaceAll("" + File.separator,
							"/"));
		}

		if (!path.contains("/")) {
			path = "/" + path;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		fileData.put("path", path);
		fileData.put("parent", path.substring(0, path.lastIndexOf("/")));

		final String id = ((String) fileData.get("path")).replace("::", "_")
				.replace("/", "::");

		String oldRevision = "";
		try {
			final Map rslt = this.getDatabase().getDocument(Map.class, id);
			if (rslt.containsKey("_rev")) {
				fileData.put("_rev", "" + rslt.get("_rev"));
				oldRevision = "" + rslt.get("_rev");
			}
		} catch (org.jcouchdb.exception.NotFoundException nfe) {
			logger.debug("Dokument mit id {} -> neu...: {}", id,
					nfe.getMessage());
		}

		fileData.put("_id", id);
		fileData.put("stored", file.getAbsolutePath());

		boolean copied = false;

		if (!this.getTargetPath().isEmpty()) {
			try {
				final String newPath = this.copyFile(file.getAbsolutePath(),
						path.replace(" ", "_"), oldRevision);
				fileData.put("stored", newPath);
				copied = true;
				if (this.isRelocateAndDelete()) {
					if (!file.getAbsolutePath().equals(
							this.getFolderToStartWith().getAbsolutePath())) {
						this.getFilesToDelete().add(file);
					}
				}

			} catch (Exception ex) {
				logger.warn("file copy failed {} : {}", file.getAbsolutePath(),
						ex.getMessage());

				logger.debug("Trace: ", ex);

			}
		}

		final StringBuilder textContent = new StringBuilder();

		if (null != data) {
			for (String key : data.keySet()) {
				fileData.put("meta." + key, data.get(key));
				textContent.append(data.get(key)).append(",");
			}
		}

		String textualContent = textContent.toString();
		if (textualContent.isEmpty()) {
			textualContent = file.getAbsolutePath();
		}

		fileData.put("content",
				textualContent.substring(0, textualContent.length() - 1));

		fileData.put("keywords", this.getDefaultKeywords());

		logger.info("about to write {}", fileData);

		this.getDatabase().createOrUpdateDocument(fileData);

		try {

			final Map<String, String> apertureData = new HashMap<String, String>();

			if (!copied) {
				apertureData.putAll(this.getApertureReader().getMetadata(
						file.getAbsolutePath()));
			} else {
				apertureData.putAll(this.getApertureReader().getMetadata(
						"" + fileData.get("stored")));
			}

			if (apertureData.containsKey("aperture.rdf")) {

				String content = apertureData.get("aperture.rdf");
				String realContent = content;
				if (content.contains("plainTextContent>")) {
					realContent = content.substring(
							content.indexOf("plainTextContent>"),
							content.lastIndexOf("plainTextContent>"));
				}

				final Map<String, String> retrieveRev = this.getDatabase()
						.getDocument(Map.class, "" + fileData.get("_id"));
				final String rev = retrieveRev.get("_rev");
				this.getDatabase().createAttachment("" + fileData.get("_id"),
						rev, "content.txt", "text/plain",
						realContent.getBytes());
			}
		} catch (Exception ex) {
			logger.error("Aperture-Extraktion misslungen: {}, {}", file,
					ex.getMessage());

			logger.debug("Trace: ", ex);

		}

	}

	private String copyFile(String absolutePath, String path, String oldRevision)
			throws IOException {
		final File sourceFile = new File(absolutePath);
		final File targetFile = new File(this.getTargetPath() + File.separator
				+ path);

		if (sourceFile.isDirectory()) {
			logger.debug("dealing with folder {}", sourceFile);
			targetFile.mkdirs();
			return targetFile.getAbsolutePath();
		}

		logger.info("copy file: {} -> {}", sourceFile.getAbsolutePath(),
				targetFile.getAbsolutePath());

		if (targetFile.exists()) {

			if (!oldRevision.isEmpty()) {
				logger.info(
						"Datei gefunden und Revision -> verschiebe 'Altdatei'...: {},{}",
						targetFile, oldRevision);
				FileUtils.moveFile(targetFile,
						new File(targetFile.getAbsolutePath() + "."
								+ oldRevision));
			} else {
				logger.info("target file {} - exists?", targetFile);
				return targetFile.getAbsolutePath();
			}
		}

		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		FileUtils.copyFile(sourceFile, targetFile);

		return targetFile.getAbsolutePath();
	}

	private class AdaptableDirectoryWalker extends DirectoryWalker {

		private Integer filesImported = 0;
		private Integer filesIgnored = 0;
		private List<String> filesFailed = new ArrayList<String>();

		public List<String> getFilesFailed() {
			return filesFailed;
		}

		public Integer getFilesImported() {
			return filesImported;
		}

		public void setFilesImported(Integer filesImported) {
			this.filesImported = filesImported;
		}

		public Integer getFilesIgnored() {
			return filesIgnored;
		}

		public void setFilesIgnored(Integer filesIgnored) {
			this.filesIgnored = filesIgnored;
		}

		public List<File> process() throws IOException {
			final List<File> files = new ArrayList<File>();
			this.walk(ImporterApp.this.getFolderToStartWith(), files);
			return files;
		}

		private final Logger logger = LoggerFactory
				.getLogger(AdaptableDirectoryWalker.class);

		@Override
		protected boolean handleDirectory(File directory, int depth,
				Collection results) throws IOException {

			if (ImporterApp.this.isRecurse()) {
				logger.info("handleDirectory: {}", directory);
			}
			// ImporterApp.this.insertToCouch(directory, null);

			if (ImporterApp.this.isRecurse()) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void handleFile(File file, int depth, Collection results)
				throws IOException {
			logger.info("handleFile: {}", file);
			if (ImporterApp.this.isIndex()) {
				try {

					final boolean validFile = (ImporterApp.this.getFileTypes()
							.contains(FilenameUtils.getExtension(file.getName()
									.toLowerCase())));

					if ((!validFile) && (ImporterApp.this.isKnownonly())) {

						logger.info("ignoriere {} -> ungueltiger Dateityp... ",
								file);

						this.filesIgnored += 1;
						return;
					}

					logger.debug("insert file: {}", file);
					final Map<String, String> map = ImporterApp.this
							.getMetadataForFile(file.getAbsolutePath());
					ImporterApp.this.insertToCouch(file, map);
					this.filesImported += 1;

				} catch (Exception ex) {
					logger.error("metadata-extraction failed in {}", file);

					logger.debug("trace: ", ex);

					this.getFilesFailed().add(file.getAbsolutePath());
					ImporterApp.this.insertToCouch(file, null);
					this.filesImported += 1;
				}

			} else {
				ImporterApp.this.insertToCouch(file, null);
			}
		}
	}

	public Map<String, String> getMetadataForFile(String diskFile) {
		if (logger.isDebugEnabled()) {
			logger.debug("getMetadataForFile: " + diskFile);
		}

		if (diskFile.toLowerCase().endsWith("xls")
				|| diskFile.toLowerCase().endsWith("xlsx")) {
			logger.info("xls: ignoriere {} -> POI-Error, TODO. ", diskFile);
			return new HashMap<String, String>();
		}

		final Map<String, String> metadata = new HashMap<String, String>();
		try {
			final Metadata m = new Metadata();
			final BodyContentHandler bc = new BodyContentHandler();
			parser.parse(new FileInputStream(new File(diskFile)), bc, m);

			for (String name : m.names()) {
				metadata.put("tika." + name, m.get(name));
			}

		} catch (Exception ex) {
			logger.warn("Fehler bei Metadaten-Extraktion aus File " + diskFile
					+ " : " + ex);
			logger.debug("Trace: ", ex);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("extrahierte Metadaten fuer " + diskFile + " : "
					+ metadata);
		}
		return metadata;
	}

	public Database getSystemDatabase() {
		if (null == this.systemDatabase) {
			this.systemDatabase = new Database(this.getDatabase().getServer(),
					"importappstats");
		}
		return systemDatabase;
	}

}
