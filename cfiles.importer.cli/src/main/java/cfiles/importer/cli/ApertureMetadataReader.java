package cfiles.importer.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.ontoware.rdf2go.model.node.URI;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.FileDataObject;
import org.semanticdesktop.aperture.accessor.base.RDFContainerFactoryImpl;
import org.semanticdesktop.aperture.accessor.file.FileAccessor;
import org.semanticdesktop.aperture.extractor.Extractor;
import org.semanticdesktop.aperture.extractor.jpg.JpgExtractor;
import org.semanticdesktop.aperture.extractor.office.OfficeExtractor;
import org.semanticdesktop.aperture.extractor.opendocument.OpenDocumentExtractor;
import org.semanticdesktop.aperture.extractor.pdf.PdfExtractor;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApertureMetadataReader {

	private static final Logger logger = LoggerFactory
			.getLogger(ApertureMetadataReader.class);

	public Map<String, String> getMetadata(String file) {
		logger.info("getMetadata: {}", file);

		final Map<String, String> metadata = new HashMap<String, String>();
		final File f = new File(file);

		try {

			DataObject dataObject = new FileAccessor().getDataObject("file://"
					+ f.getAbsolutePath(), null, null,
					new RDFContainerFactoryImpl());

			logger.info("dataObject: {}", dataObject.getClass()
					.getCanonicalName());

			if (dataObject instanceof FileDataObject) {
				logger.info("fileDataObject...");
				final Extractor extractor = this.getExtractorForFileName(
						"file://" + f.getAbsolutePath(), dataObject.getID(),
						f.getName());

				if (null == extractor) {
					logger.warn("kein Metadaten-Extraktor fuer {}", f.getName());
				} else {
					logger.info("extrahiere: {} mit {}", f.getName(), extractor
							.getClass().getCanonicalName());
					extractor.extract(dataObject.getID(),
							((FileDataObject) dataObject).getContent(), null,
							null, dataObject.getMetadata());
					final StringWriter xmlWriter = new StringWriter();
					dataObject.getMetadata().getModel().writeTo(xmlWriter);
					xmlWriter.flush();
					logger.info("rslt: aperture.rdf: {}", xmlWriter.toString());
					metadata.put("aperture.rdf", xmlWriter.toString());
				}
			}
			dataObject.dispose();
		} catch (Exception ex) {
			logger.error("aperture: " + ex);
		}

		return metadata;
	}

	private Extractor getExtractorForFileName(String fURI, URI doURI,
			String origFileName) {
		Extractor e = null;
		final MagicMimeTypeIdentifier identifier = new MagicMimeTypeIdentifier();

		try {
			final byte[] bytes = new byte[identifier.getMinArrayLength()];
			(new FileInputStream(new File(fURI.replace("file://", ""))))
					.read(bytes);
			String mimeType = identifier.identify(bytes,
					fURI.replace("file://", ""), doURI);

			logger.debug("mimetype fuer " + fURI + " : " + mimeType);

			if (mimeType.equals("application/pdf")) {
				e = new PdfExtractor();
			}

			if (mimeType.equals("image/jpeg")) {
				e = new JpgExtractor();
			}

			if (mimeType.equals("application/vnd.ms-office")) {
				e = new OfficeExtractor();
			}

			if (null == e) {
				final String extension = FilenameUtils
						.getExtension(origFileName);
				if ((extension.startsWith("od"))
						|| (extension.startsWith("ot"))) {
					e = new OpenDocumentExtractor();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return e;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		final ApertureMetadataReader amr = new ApertureMetadataReader();
		System.out.println(amr
				.getMetadata("/home/kr/kontext/stapel/manual.pdf").values());

	}

}
