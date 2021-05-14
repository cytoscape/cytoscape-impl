package org.cytoscape.task.internal.session;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class OpenSessionTaskFactoryImpl extends AbstractTaskFactory implements OpenSessionTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public OpenSessionTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public synchronized TaskIterator createTaskIterator() {
		return new TaskIterator(2, new OpenSessionTask(serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		return createTaskIterator(file, false);
	}

	@Override
	public TaskIterator createTaskIterator(File file, boolean confirm) {
		if (confirm) {
			return new TaskIterator(2, new OpenSessionTask(file, serviceRegistrar));
		} else {
			final Map<String, Object> m = new HashMap<>();
			m.put("file", file);
			m.put("loadSession", true);
	
			return serviceRegistrar.getService(TunableSetter.class).createTaskIterator(createTaskIterator(), m);
		}
	}
}
