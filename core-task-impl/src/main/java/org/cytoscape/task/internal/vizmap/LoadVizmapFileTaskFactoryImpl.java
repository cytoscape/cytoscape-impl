package org.cytoscape.task.internal.vizmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class LoadVizmapFileTaskFactoryImpl extends AbstractTaskFactory implements LoadVizmapFileTaskFactory {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private final CyServiceRegistrar serviceRegistrar;

	public LoadVizmapFileTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, createTask());
	}
	
	public LoadVizmapFileTask createTask() {
		return new LoadVizmapFileTask(serviceRegistrar);
	}

	@Override
	public Set<VisualStyle> loadStyles(File f) {
		// Set up map containing values to be assigned to tunables.
		// The name "file" is the name of the tunable field in LoadVizmapFileTask.
		Map<String, Object> m = new HashMap<>();
		m.put("file", f);

		LoadVizmapFileTask task = createTask();
		SynchronousTaskManager<?> syncTaskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
		syncTaskManager.setExecutionContext(m);
		syncTaskManager.execute(new TaskIterator(2, task));

		return task.getStyles();
	}

	@Override
	public Set<VisualStyle> loadStyles(final InputStream is) {
		// Save the contents of inputStream in a tmp file
		File f = null;
		
		try {
			f = this.getFileFromStream(is);
		} catch (IOException e) {
			throw new IllegalStateException("Could not create temp file", e);
		}

		if (f == null)
			throw new NullPointerException("Could not create temp file.");
		
		return loadStyles(f);
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		return createTaskIterator(file, null);
	}

	@Override
	public TaskIterator createTaskIterator(File file, TaskObserver observer) {
		final Map<String, Object> m = new HashMap<>();
		m.put("file", file);

		TunableSetter tunableSetter = serviceRegistrar.getService(TunableSetter.class);
		
		return tunableSetter.createTaskIterator(this.createTaskIterator(), m, observer);
	}
	
	// Read the inputStream and save the content in a tmp file
	private File getFileFromStream(final InputStream is) throws IOException {
		File returnFile = null;

		// Get the contents from inputStream
		final List<String> list = new ArrayList<>();

		BufferedReader bf = null;
		String line;

		try {
			bf = new BufferedReader(new InputStreamReader(is));
			while (null != (line = bf.readLine())) {
				list.add(line);
			}
		} catch (IOException e) {
			logger.error("Could not read the VizMap file.", e);
		} finally {
			try {
				if (bf != null)
					bf.close();
			} catch (IOException e) {
				logger.error("Could not Close the stream.", e);
				bf = null;
			}
		}

		if (list.size() == 0)
			return null;

		// Save the content to a tmp file
		Writer output = null;
		
		try {
			returnFile = File.createTempFile("visualStyles", "", new File(System.getProperty("java.io.tmpdir")));
			returnFile.deleteOnExit();

			// use buffering
			output = new BufferedWriter(new FileWriter(returnFile));
			// FileWriter always assumes default encoding is OK!
			for (int i = 0; i < list.size(); i++) {
				output.write(list.get(i) + "\n");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					output = null;
				} catch (IOException e) {
					logger.error("Could not close stream.", e);
					output = null;
				}
			}
		}

		if (returnFile == null)
			throw new NullPointerException("Could not create temp VizMap file.");

		final String originalFileName = returnFile.getAbsolutePath();
		
		if (isXML(list)) {
			final File xmlFile = new File(originalFileName + ".xml");
			final boolean renamed = returnFile.renameTo(xmlFile);
			if (renamed)
				return xmlFile;
			else
				throw new IOException("Could not create temp vizmap file: " + xmlFile);
		} else {
			// Return ad legacy property format
			final File propFile = new File(originalFileName + ".props");
			final boolean renamed = returnFile.renameTo(propFile);
			if (renamed)
				return propFile;
			else
				throw new IOException("Could not create temp vizmap file: " + propFile);
		}
	}

	/**
	 * Perform simple test whether this file is XML or not.
	 */
	private final boolean isXML(final List<String> list) {
		if (list == null || list.isEmpty())
			return false;

		return list.get(0).contains("<?xml");
	}
}
