package org.cytoscape.datasource.biogrid.internal;

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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.filechooser.FileSystemView;

import org.cytoscape.datasource.DataSource;
import org.cytoscape.datasource.DefaultDataSource;
import org.cytoscape.io.DataCategory;

public class BiogridDataLoader {

	// Default resource file location.
	private static final String DEF_RESOURCE = "biogrid/BIOGRID-ORGANISM-3.1.83.mitab.zip";

	public final static int BUF_SIZE = 1024;
	private static final String LOCAL = "biogrid";
	private URL source;
	private File localFile;

	private static final Map<String, String[]> FILTER = new HashMap<String, String[]>();
	
	private final Set<DataSource> sources;

	
	static {
		FILTER.put("Homo_sapiens", new String[]{"Human Interactome", "BioGRID", "Human Interactome from BioGRID database"});
		FILTER.put("Saccharomyces_cerevisiae", new String[]{"Yeast Interactome", "BioGRID", "Yeast Interactome from BioGRID database"});
		FILTER.put("Drosophila_melanogaster", new String[]{"Fly Interactome", "BioGRID","Fly Interactome from BioGRID database"} );
		FILTER.put("Mus_musculus", new String[]{"Mouse Interactome", "BioGRID","Mouse Interactome from BioGRID database"});
	}

	public BiogridDataLoader(final File settingFileLocation) {
		this(null, settingFileLocation);
	}
	
	public BiogridDataLoader(final URL dataSource, final File settingFileLocation) {
		if(dataSource == null)
			source = this.getClass().getClassLoader().getResource(DEF_RESOURCE);
		else
			this.source = dataSource;

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
				
				final DataSource ds = new DefaultDataSource(data[0], data[1], data[2], DataCategory.NETWORK, outFile.toURI().toURL());
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
			final DataSource ds = new DefaultDataSource(data[0], data[1], data[2], DataCategory.NETWORK, file.toURI()
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
