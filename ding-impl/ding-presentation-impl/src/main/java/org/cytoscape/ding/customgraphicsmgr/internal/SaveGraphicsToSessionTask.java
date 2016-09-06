package org.cytoscape.ding.customgraphicsmgr.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class SaveGraphicsToSessionTask implements Task {
	
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";
	
	private final File imageHomeDirectory;
	private final SessionAboutToBeSavedEvent e;
	
	SaveGraphicsToSessionTask(final File imageHomeDirectory, final SessionAboutToBeSavedEvent e) {
		this.imageHomeDirectory = imageHomeDirectory;
		this.e = e;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Add it to the apps list
		final List<File> fileList = new ArrayList<>();
		final String[] fileArray = imageHomeDirectory.list();
		
		for (final String file : fileArray)
			fileList.add(new File(imageHomeDirectory, file));

		e.addAppFiles(APP_NAME, fileList);
	}

	@Override
	public void cancel() {
	}
}
