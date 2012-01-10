package org.cytoscape.property.internal;


import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropsReader extends AbstractConfigDirPropsReader {

	private static final Logger logger = LoggerFactory.getLogger(PropsReader.class);

	/**
	 * Creates a new PropsReader object.
	 */
	public PropsReader(String name, String fileName) {
		super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
	}
}
