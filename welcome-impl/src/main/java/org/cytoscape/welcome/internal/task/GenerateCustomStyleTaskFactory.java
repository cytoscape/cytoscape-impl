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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class GenerateCustomStyleTaskFactory extends AbstractTaskFactory {

	private final CyServiceRegistrar registrar;
	private final CyApplicationManager applicationManager;

	private final VisualStyleBuilder builder;
	private final VisualMappingManager vmm;

	public GenerateCustomStyleTaskFactory(
			final CyServiceRegistrar registrar,
			final CyApplicationManager applicationManager, final VisualStyleBuilder builder,
			final VisualMappingManager vmm) {
		this.registrar = registrar;
		this.applicationManager = applicationManager;
		this.builder = builder;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GenerateCustomStyleTask(registrar, applicationManager,
				builder, vmm));
	}

}
