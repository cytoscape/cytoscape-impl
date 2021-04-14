package org.cytoscape.browser.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

/**
 * This task factory wraps a {@link TableTaskFactory} in a simpler {@link TaskFactory}, because the one
 * provided by {@link DynamicTaskFactoryProvisioner} does not expose the method
 * {@link TableTaskFactory#isApplicable(CyTable)}, which we need here if we want to dynamically
 * show/hide buttons created from a {@link TableTaskFactory}.
 */
public class DynamicTableTaskFactory implements TaskFactory {

	protected final TableTaskFactory factory;
	protected final CyServiceRegistrar serviceRegistrar;

	public DynamicTableTaskFactory(TableTaskFactory factory, CyServiceRegistrar serviceRegistrar) {
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return factory.createTaskIterator(getCurrentTable());
	}
	
	@Override
	public boolean isReady() {
		return factory.isReady(getCurrentTable());
	}
	
	public boolean isReady(CyTable table) {
		return factory.isReady(table);
	}
	
	/**
	 * Just calls {@link TableTaskFactory#isApplicable(CyTable)} on the wrapped factory and pass the current table.
	 */
	public boolean isApplicable() {
		return factory.isApplicable(getCurrentTable());
	}
	
	public boolean isApplicable(CyTable table) {
		return factory.isApplicable(table);
	}
	
	protected CyTable getCurrentTable() {
		return serviceRegistrar.getService(CyApplicationManager.class).getCurrentTable();
	}
}
