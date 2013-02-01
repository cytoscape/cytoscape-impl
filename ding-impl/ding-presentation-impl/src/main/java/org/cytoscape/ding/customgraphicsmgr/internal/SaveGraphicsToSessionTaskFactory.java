package org.cytoscape.ding.customgraphicsmgr.internal;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveGraphicsToSessionTaskFactory extends AbstractTaskFactory {

	private final File location;
	private final CustomGraphicsManager manager;

	private final SessionAboutToBeSavedEvent e;

	SaveGraphicsToSessionTaskFactory(final File location, final CustomGraphicsManager manager,
			final SessionAboutToBeSavedEvent e) {
		this.manager = manager;
		this.location = location;
		this.e = e;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PersistImageTask(location, manager), new SaveGraphicsToSessionTask(location, e));
	}

}
