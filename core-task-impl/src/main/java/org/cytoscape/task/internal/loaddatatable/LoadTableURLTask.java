package org.cytoscape.task.internal.loaddatatable;

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



import java.net.URL;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class LoadTableURLTask extends AbstractLoadTableTask {
	
	@Tunable(description = "Data Table URL:", params = "fileCategory=table;input=true")
	public URL url;

	public LoadTableURLTask(final CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	/**
	 * Executes Task.
	 */
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		loadTable(url.toString(), url.toURI(), true, taskMonitor);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Import Table";
	}
}
