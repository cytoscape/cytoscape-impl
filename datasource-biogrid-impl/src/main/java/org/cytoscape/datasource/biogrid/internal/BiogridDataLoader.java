package org.cytoscape.datasource.biogrid.internal;

/*
 * #%L
 * Cytoscape BioGrid Datasource Impl (datasource-biogrid-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DefaultDataSource;
import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiogridDataLoader {

	private static final Logger logger = LoggerFactory.getLogger(BiogridDataLoader.class);

	private static final String FILE_LOCATION = "biogrid.file.url";
	private static final String TAG = "<meta>preset,interactome</meta>"; 
	
	// Default resource file location.
	private static final String DEF_RESOURCE = "biogrid/BIOGRID-ORGANISM-LATEST.mitab.zip";
	
	// Remote URL for the latest release

	public final static int BUF_SIZE = 1024;
	private static final String LOCAL = "biogrid";
	private URL source;
	private File localFile;
	
	private String version = null;

	private static final Map<String, String[]> FILTER = new HashMap<String, String[]>();
	private final Set<DataSource> sources;

	static {
		FILTER.put("Homo_sapiens", new String[]{"H. sapiens", "BioGRID", "Human Interactome from BioGRID database"});
		FILTER.put("Saccharomyces_cerevisiae", new String[]{"S. cerevisiae", "BioGRID", "Yeast Interactome from BioGRID database"});
		FILTER.put("Drosophila_melanogaster", new String[]{"D. melanogaster", "BioGRID","Fly Interactome from BioGRID database"} );
		FILTER.put("Mus_musculus", new String[]{"M. musculus", "BioGRID", "Mouse Interactome from BioGRID database"});
		FILTER.put("Arabidopsis_thaliana", new String[]{"A. thaliana", "BioGRID", "Arabidopsis Interactome from BioGRID database"});
		FILTER.put("Caenorhabditis_elegans", new String[]{"C. elegans", "BioGRID", "Caenorhabditis elegans Interactome from BioGRID database"});
		FILTER.put("Escherichia_coli", new String[]{"E. coli", "BioGRID", "Escherichia coli Interactome from BioGRID database"});
		FILTER.put("Danio_rerio", new String[]{"D. rerio", "BioGRID", "Zebrafish Interactome from BioGRID database"});
	}

	public BiogridDataLoader(final CyProperty<?> props, final File settingFileLocation) {
		this(props, null, settingFileLocation);
	}


	public BiogridDataLoader(final CyProperty<?> props, final URL dataSource, final File settingFileLocation) {
		// First priority: optional URL props.
		final Properties propObject = (Properties) props.getProperties();
		final String locationString = propObject.getProperty(FILE_LOCATION);

		if(locationString != null && dataSource == null) {
			try {
				source = new URL(locationString);
			} catch (MalformedURLException e) {
				source = null;
			}
		} else if(dataSource == null) {
			source = this.getClass().getClassLoader().getResource(DEF_RESOURCE);
		} else {
			this.source = dataSource;
		}
		
		this.sources = new HashSet<DataSource>();
		
		localFile = new File(settingFileLocation, LOCAL);
		if (localFile.exists() == false)
			localFile.mkdir();
	}

	public void extractVersionNumber(final String fileName) {
		final String[] parts = fileName.split("-");
		if(parts == null || parts.length < 3)
			version = "Unknown";
		else {
			final String[] nextPart = parts[parts.length-1].split(".mitab");
			if(nextPart == null || nextPart.length != 1)
				version = "Unknown";
			else
				version = nextPart[0];
		}
		
		logger.info("BioGRID relese version is: " + version);
	}
	
	/**
	 * Check local file is available or not.
	 * 
	 * @return true if files are already exists.
	 */
	private boolean isExist() {
		if(localFile.isDirectory()) {
			final String[] fileNames = localFile.list();
			if(fileNames == null || fileNames.length == 0)
				return false;
			else {
				for(String fileName: fileNames) {
					if(fileName.contains("mitab"))
						return true;
				}
			}
		} else {
			throw new IllegalStateException("given location is not a directory.");
		}
		
		return false;
	}


	public void extract() throws IOException {
		this.extract(false);
	}
	/**
	 * Extract list of files from resource (local or remote)
	 * 
	 * @throws IOException
	 */
	public void extract(final boolean forceUpdate) throws IOException {
		if(isExist() && forceUpdate == false) {
			logger.info("Local network data file exists.  Processing local files...");
			processExistingFiles();
			return;
		}
		
		ZipInputStream zis = new ZipInputStream(source.openStream());
		try {
			// Extract list of entries
			ZipEntry zen = null;
			String entryName = null;

			while ((zen = zis.getNextEntry()) != null) {
				entryName = zen.getName();
				// Remove .txt
				String newName = entryName.replace(".txt", "");
				logger.info("* Processing new organism data file: " + newName);
				
				final String[] data = createName(newName);
				if (data==null)
					continue;

				File outFile = new File(localFile, newName);
				if(version == null) {
					extractVersionNumber(newName);
				}
				
				processOneEntry(outFile, zis);
				zis.closeEntry();
				
				final DataSource ds = new DefaultDataSource(data[0], data[1], TAG + data[2] + " Release " + version, DataCategory.NETWORK, outFile.toURI().toURL());
				sources.add(ds);
			}

		} finally {
			if (zis != null)
				zis.close();
			zis = null;
		}
	}


	private void processExistingFiles() throws IOException {
		// Just need to create from existing files.
		final File[] dataFiles = localFile.listFiles();

		for (File file : dataFiles) {
			logger.info("* Processing local organism network file: " + file.getName());
			if(version == null) {
				extractVersionNumber(file.getName());
			}
			final String[] data = createName(file.getName());
			final DataSource ds = new DefaultDataSource(
					data[0], data[1], TAG + data[2] + " Release " + version, DataCategory.NETWORK, file.toURI().toURL());
			sources.add(ds);
		}
		return;
	}


	private void processOneEntry(File outFile, InputStream is) throws IOException {
		outFile.createNewFile();
		FileWriter outWriter = new FileWriter(outFile);
		String line;
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int count = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#"))
				continue;
			outWriter.write(line + "\n");
			count++;
		}
		outWriter.close();
	}

	private String[] createName(String name) {
		for (String key : FILTER.keySet()) {
			if (name.contains(key)) {
				return FILTER.get(key);
			}
		}

		return null;
	}
	
	public Set<DataSource> getDataSources() {
		return this.sources;
	}
	
	public String getVersion() {
		return this.version;
	}
}