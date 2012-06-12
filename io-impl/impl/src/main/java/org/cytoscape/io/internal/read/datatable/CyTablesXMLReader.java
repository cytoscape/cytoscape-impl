package org.cytoscape.io.internal.read.datatable;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.internal.util.cytables.model.CyTables;
import org.cytoscape.io.internal.util.cytables.model.ObjectFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CyTablesXMLReader extends AbstractTask {
	private static String CYTABLES_PACKAGE = CyTables.class.getPackage().getName();
	
	private InputStream inputStream;
	private CyTables cyTables;

	public CyTablesXMLReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public CyTables getCyTables() {
		return cyTables;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(CYTABLES_PACKAGE, getClass().getClassLoader());
		taskMonitor.setProgress(0.33);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		taskMonitor.setProgress(0.67);
        cyTables = (CyTables) unmarshaller.unmarshal(inputStream);
		taskMonitor.setProgress(1);
	}
}
