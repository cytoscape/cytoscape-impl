package org.cytoscape.task.internal.quickstart.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilePreprocessor implements InteractionFilePreprocessor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractFilePreprocessor.class);
	
	private static final String INTERACTION_DIR_NAME = "interactions";
	
	protected final Map<String, URL> sourceMap;
	protected LineProcessor processor;
	
	protected final File dataFileDirectory;

	
	public AbstractFilePreprocessor(final CyProperty<Properties> properties, final CyApplicationConfiguration config) {
		if (properties == null)
			throw new NullPointerException("Property service is null.");

		final Properties props = properties.getProperties();

		if (props == null)
			throw new NullPointerException("Property is missing.");

		this.dataFileDirectory = new File(config.getConfigurationDirectoryLocation(), INTERACTION_DIR_NAME);
		if (dataFileDirectory.exists() == false)
			dataFileDirectory.mkdir();
		
		this.sourceMap = new HashMap<String, URL>();
	}
	

	protected void processZipTextFile(final URL source) throws IOException {
		ZipInputStream zis = new ZipInputStream(source.openStream());
		try {

			// Extract list of entries
			ZipEntry zen = null;
			String entryName = null;

			while ((zen = zis.getNextEntry()) != null) {
				entryName = zen.getName();
				File outFile = new File(dataFileDirectory, entryName + ".sif");
				
				processOneEntry(outFile, zis, entryName);
				zis.closeEntry();
			}

		} finally {
			if (zis != null)
				zis.close();
			zis = null;
		}
		
		logger.info("Processed and created " + this.sourceMap.size() + " data files.");
	}
	
	private void processOneEntry(File outFile, InputStream is, String name) throws IOException {
		outFile.createNewFile();
		FileWriter outWriter = new FileWriter(outFile);
		String line;
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int count = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#"))
				continue;
			outWriter.write(processor.processLine(line) + "\n");
			count++;
		}
		outWriter.close();
		this.sourceMap.put(createFileName(name), outFile.toURI().toURL());
	}
	
	abstract String createFileName(final String originalFileName);

	@Override
	public boolean isLatest() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, URL> getDataSourceMap() {
		return this.sourceMap;
	}

}
