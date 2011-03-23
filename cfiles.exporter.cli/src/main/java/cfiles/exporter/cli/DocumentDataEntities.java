package cfiles.exporter.cli;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "documents")
public class DocumentDataEntities {

	private DocumentDataEntity[] documents = new DocumentDataEntity[] {};

	public DocumentDataEntities() {
	}

	public DocumentDataEntities(Collection<DocumentDataEntity> es) {
		this.documents = es.toArray(new DocumentDataEntity[] {});
	}

	@XmlElement(name = "entry")
	public DocumentDataEntity[] getDocuments() {
		return this.documents;
	}
}
