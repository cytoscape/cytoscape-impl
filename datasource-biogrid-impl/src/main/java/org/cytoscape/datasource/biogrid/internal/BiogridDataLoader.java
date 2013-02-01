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

public class BiogridDataLoader {

	private static final String FILE_LOCATION = "biogrid.file.url";
	private static final String TAG = "<meta>preset,interactome</meta>"; 
	
	// Default resource file location.
	private static final String DEF_RESOURCE = "biogrid/BIOGRID-ORGANISM-3.2.95.mitab.zip";

	public final static int BUF_SIZE = 1024;
	private static final String LOCAL = "biogrid";
	private URL source;
	private File localFile;
	
	private String version;

	private static final Map<String, String[]> FILTER = new HashMap<String, String[]>();
	private final Set<DataSource> sources;

	static {
		FILTER.put("Homo_sapiens", new String[]{"Human", "BioGRID", "Human Interactome from BioGRID database"});
		FILTER.put("Saccharomyces_cerevisiae", new String[]{"Yeast", "BioGRID", "Yeast Interactome from BioGRID database"});
		FILTER.put("Drosophila_melanogaster", new String[]{"Fly", "BioGRID","Fly Interactome from BioGRID database"} );
		FILTER.put("Mus_musculus", new String[]{"Mouse", "BioGRID", "Mouse Interactome from BioGRID database"});
		FILTER.put("Arabidopsis_thaliana", new String[]{"Arabidopsis", "BioGRID", "Arabidopsis from BioGRID database"});
		FILTER.put("Caenorhabditis_elegans", new String[]{"C. Elegans", "BioGRID", "Caenorhabditis Elegans from BioGRID database"});
	}

	public BiogridDataLoader(final CyProperty props, final File settingFileLocation) {
		this(props, null, settingFileLocation);
	}


	public BiogridDataLoader(final CyProperty props, final URL dataSource, final File settingFileLocation) {
		
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
		
		String[] parts = source.toString().split("BIOGRID-ORGANISM-");
		if(parts == null || parts.length != 2)
			version = "Unknown";
		else {
			String[] nextPart = parts[1].split(".mitab.zip");
			if(nextPart == null || nextPart.length != 1)
				version = "Unknown";
			else
				version = nextPart[0];
		}
		
		this.sources = new HashSet<DataSource>();
		
		localFile = new File(settingFileLocation, LOCAL);
		if (localFile.exists() == false)
			localFile.mkdir();
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

	
	/**
	 * Extract list of files from resource (local or remote)
	 * 
	 * @throws IOException
	 */
	void extract() throws IOException {
		
		if(isExist()) {
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
				final String[] data = createName(newName);
				if (data==null)
					continue;

				File outFile = new File(localFile, newName);

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
			final String[] data = createName(file.getName());
			final DataSource ds = new DefaultDataSource(data[0], data[1], TAG + data[2] + " Release " + version, DataCategory.NETWORK, file.toURI()
					.toURL());
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
}
