package cfiles.importer.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
	private static final Logger logger = LoggerFactory
			.getLogger(Launcher.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("[launcher] starting up...");

			String logConfigPath = "/opt/arcsystem/conf/log4j.properties";

			if (System.getProperties().containsKey("arcsystem.logfile")) {
				logConfigPath = System.getProperty("arcsystem.logfile");
			}

			System.out.println("[launcher] log file: " + logConfigPath);

			if (new File(logConfigPath).exists()) {
				System.out
						.println("logging: externe log4j.properties konfiguriert... ");
				PropertyConfigurator.configureAndWatch(
						"/opt/ocrtools/log4j.properties", 60 * 1000);
			} else {
				System.out
						.println("nutze interne log4j.properties, extern nicht gefunden?!");
				PropertyConfigurator.configureAndWatch("log4j.properties",
						60 * 1000);
			}

			logger.info("[ImporterAppLauncher running...]");
			final CommandLineParser parser = new GnuParser();
			final CommandLine line = parser.parse(
					ImporterApp.generateOptions(), args);

			final ImporterApp ia = new ImporterApp(line);

			if (line.hasOption("h")) {
				final HelpFormatter h = new HelpFormatter();
				h.printHelp(Launcher.class.getSimpleName(),
						ImporterApp.generateOptions());
			}

			logger.info("- instance to run: {}", ia);

			if (line.hasOption("l")) {
				try {
					ia.listDataStore();
				} catch (Exception ex) {
					logger.error("list failed: {}", ex.getMessage());
				}
				return;
			}

			try {
				ia.doImport();
			} catch (Exception ex) {
				logger.error("import failed: {}", ex.getMessage());
				logger.debug("trace: ", ex);
			}

		} catch (ParseException ex) {
			logger.error("invocation failed: {}", ex.getMessage());

		}

	}

}
