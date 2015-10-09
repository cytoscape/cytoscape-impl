package org.cytoscape.filter.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class FilterIO {
	CyTransformerReader reader;
	CyTransformerWriter writer;
	
	public FilterIO(CyTransformerReader reader, CyTransformerWriter writer) {
		this.reader = reader;
		this.writer = writer;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void readTransformers(File file, AbstractPanel panel) throws IOException {
		try(BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			NamedTransformer<CyNetwork, CyIdentifiable>[] transformers = (NamedTransformer<CyNetwork, CyIdentifiable>[]) reader.read(stream);
			AbstractPanelController controller = panel.getController();
			controller.addNamedTransformers(panel, transformers);
		}
	}
	

	public void writeFilters(File file, NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers) throws IOException {
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
		writer.write(stream, namedTransformers);
		stream.close();
	}

}
