package org.cytoscape.task.internal.session;

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



import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.create.NewSessionTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


public class NewSessionTaskFactoryImpl extends AbstractTaskFactory implements NewSessionTaskFactory {

	private final CySessionManager mgr;
	private final TunableSetter tunableSetter;
	private final CyEventHelper eventHelper;
	
	public NewSessionTaskFactoryImpl(CySessionManager mgr, TunableSetter tunableSetter, CyEventHelper eventHelper) {
		this.mgr = mgr;
		this.tunableSetter = tunableSetter;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new NewSessionTask(mgr, eventHelper));
	}

	@Override
	public TaskIterator createTaskIterator(boolean destroyCurrentSession) {
		final Map<String, Object> m = new HashMap<>();
		m.put("destroyCurrentSession", destroyCurrentSession);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
