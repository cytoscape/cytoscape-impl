package org.cytoscape.psi_mi.internal.plugin;

import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapFromCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapInteractionsToPsiOne;
import org.cytoscape.psi_mi.internal.data_mapper.MapInteractionsToPsiTwoFive;
import org.cytoscape.psi_mi.internal.data_mapper.SchemaMapper;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.work.TaskMonitor;

public class PsiMiWriter implements CyWriter {

	private final OutputStream outputStream;
	private final CyNetwork network;
	private final SchemaVersion version;
	
	public PsiMiWriter(OutputStream outputStream, CyNetwork network, SchemaVersion version) {
		this.outputStream = outputStream;
		this.network = network;
		this.version = version;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		//  First, map to Data Service Objects
		MapFromCytoscape modelMapper = new MapFromCytoscape(network);
		modelMapper.doMapping();

		List<Interaction> interactions = modelMapper.getInteractions();

		//  Second, map to PSI-MI
		SchemaMapper<?> schemaMapper;
		switch (version) {
		case LEVEL_1:
			schemaMapper = new MapInteractionsToPsiOne(interactions);
			break;
		case LEVEL_2_5:
			schemaMapper = new MapInteractionsToPsiTwoFive(interactions);
			break;
		default:
			throw new IllegalArgumentException(); 
		}
		
		schemaMapper.doMapping();
		Object model = schemaMapper.getModel();
		Marshaller marshaller = createMarshaller(schemaMapper.getSchemaNamespace());
		marshaller.marshal(model, outputStream);
	}

	@Override
	public void cancel() {
	}
	
	private Marshaller createMarshaller(String schema) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(schema, getClass().getClassLoader());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	
		return marshaller;
	}

}
