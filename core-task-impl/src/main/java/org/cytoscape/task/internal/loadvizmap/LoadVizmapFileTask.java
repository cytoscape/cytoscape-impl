package org.cytoscape.task.internal.loadvizmap;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class LoadVizmapFileTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Import Style";
	}
	
	@Tunable(description="Style file:", params = "fileCategory=vizmap;input=true")
	public File file;

	private final VisualMappingManager vmMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private AddVisualStylesTask addVSTask;

	public LoadVizmapFileTask(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmMgr) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmMgr = vmMgr;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		if (file == null) 
			throw new NullPointerException("No file specified.");

		VizmapReader reader = vizmapReaderMgr.getReader(file.toURI(), file.getName());
		taskMonitor.setProgress(0.9);

		if (reader == null) 
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		addVSTask = new AddVisualStylesTask(reader, vmMgr);

		insertTasksAfterCurrentTask(reader, addVSTask);
		taskMonitor.setProgress(1.0);
	}

	public Set<VisualStyle> getStyles() {
		return addVSTask.getStyles();
	}
}

class AddVisualStylesTask extends AbstractTask implements ObservableTask {

	private final VizmapReader reader;
	private final VisualMappingManager vmMgr;
	private Set<VisualStyle> styles; 

	public AddVisualStylesTask(VizmapReader reader, VisualMappingManager vmMgr) {
		this.reader = reader;
		this.vmMgr = vmMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setTitle("Loading visual styles...");
		styles = reader.getVisualStyles();

		if (styles != null) {
			int count = 1;
			int total = styles.size();

			for (VisualStyle vs : styles) {
				if (cancelled) break;
				taskMonitor.setStatusMessage(count + " of " + total + ": " + vs.getTitle());
				vmMgr.addVisualStyle(vs);
				taskMonitor.setProgress(count / total);
				count++;
			}

			if (cancelled) {
				// remove recently added styles
				for (VisualStyle vs : styles) {
					vmMgr.removeVisualStyle(vs);
				}

			}
		}
		taskMonitor.setProgress(1.0);
	}

	@Override
	public Object getResults(Class expectedResult) {
		if (expectedResult.equals(List.class))
			return new ArrayList<>(styles);
		else if (expectedResult.equals(String.class)) {
			String strRes = "";
			for (VisualStyle style: styles) {
				strRes = style.toString()+"\n";
			}
			return strRes.substring(0, strRes.length()-1);
		} else
			return styles;
	}

	public Set<VisualStyle> getStyles() {
		return styles; 
	}
}
