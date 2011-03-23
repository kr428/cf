package cfiles.exporter.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
	private static final Logger logger = LoggerFactory
			.getLogger(Launcher.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("[launcher] starting up...");

		String logConfigPath = "/opt/arcsystem/conf/log4j.properties";

		if (System.getProperties().containsKey("arcsystem.logfile")) {
			logConfigPath = System.getProperty("arcsystem.logfile");
		}

		final DataExporter de = new DataExporter();
		de.setBulkXml(true);
		de.run("/");
	}

}
