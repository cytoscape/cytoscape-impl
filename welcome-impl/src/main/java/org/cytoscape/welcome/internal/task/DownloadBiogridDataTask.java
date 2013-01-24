package org.cytoscape.welcome.internal.task;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComboBox;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DownloadBiogridDataTask extends AbstractTask {

	private static final String DEF_URL = "http://thebiogrid.org/downloads/archives/Release%20Archive/BIOGRID-3.1.81/BIOGRID-ORGANISM-3.1.81.mitab.zip";

	public final static int BUF_SIZE = 1024;
	private static final String LOCAL = "biogrid";
	private URL source;
	private File localFile;

	private JComboBox fileList;

	private static final Map<String, String> FILTER = new HashMap<String, String>();
	private final Map<String, URL> sourceMap;

	static {
		FILTER.put("Homo_sapiens", "BioGRID: Human Interactome");
		FILTER.put("Saccharomyces_cerevisiae", "BioGRID: Yeast Interactome");
		FILTER.put("Drosophila_melanogaster", "BioGRID: Fly Interactome");
		FILTER.put("Mus_musculus", "BioGRID: Mouse Interactome");
	}

	public DownloadBiogridDataTask(final File settingFileLocation, JComboBox fileList) {
		this.fileList = fileList;
		fileList.addItem("Preparing files.  Please wait...");
		fileList.setEnabled(false);
		sourceMap = new HashMap<String, URL>();

		localFile = new File(settingFileLocation, LOCAL);
		if (localFile.exists() == false)
			localFile.mkdir();
	}

	private boolean isUp2Date() {

		final String[] listOfFiles = localFile.list();

		if (listOfFiles.length != 0)
			return true;
		else
			return false;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		if (isUp2Date() == false) {
			source = new URL(DEF_URL);
			download();
		} else {
			File[] files = localFile.listFiles();
			for (File file : files) {
				String name = file.getName();
				String fileName = createName(name);
				if (name.equals(fileName))
					continue;
				fileList.addItem(fileName);
				sourceMap.put(fileName, file.toURI().toURL());
				
			}
		}

		fileList.removeItemAt(0);
		fileList.setEnabled(true);
		taskMonitor.setProgress(1.0);
	}

	private void download() throws IOException {
		ZipInputStream zis = new ZipInputStream(source.openStream());

		try {

			// Extract list of entries
			ZipEntry zen = null;
			String entryName = null;

			while ((zen = zis.getNextEntry()) != null) {
				entryName = zen.getName();
				// Remove .txt
				String newName = entryName.replace(".txt", "");
				final String name = createName(newName);
				if (name.equals(newName))
					continue;

				File outFile = new File(localFile, newName);
				this.fileList.addItem(name);

				processOneEntry(outFile, zis);
				zis.closeEntry();
				this.sourceMap.put(name, outFile.toURI().toURL());
			}

		} finally {
			if (zis != null)
				zis.close();
			zis = null;
		}
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

	private String createName(String name) {
		for (String key : FILTER.keySet()) {
			if (name.contains(key)) {
				return FILTER.get(key);
			}
		}

		return name;
	}
	
	Map<String, URL> getSourceMap() {
		return this.sourceMap;
	}

}
