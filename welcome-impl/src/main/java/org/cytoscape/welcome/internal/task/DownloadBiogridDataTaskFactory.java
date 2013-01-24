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

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DownloadBiogridDataTaskFactory extends AbstractTaskFactory {

	private final JComboBox list;
	
	private DownloadBiogridDataTask task;
	
	private final File settingFile;
	
	DownloadBiogridDataTaskFactory(final JComboBox list, final CyApplicationConfiguration config) {
		settingFile = config.getConfigurationDirectoryLocation();
		this.list = list;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		task = new DownloadBiogridDataTask(settingFile, list);
		return new TaskIterator(task);
	}
	
	Map<String, URL> getMap() {
		return task.getSourceMap();
	}

}
