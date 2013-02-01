package org.cytoscape.group.data.internal;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
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
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.group.data.CyGroupAggregationManager;

class CyGroupSettingsTaskFactory extends AbstractTaskFactory {
	CyGroupAggregationManager cyAggManager;
	CyApplicationManager cyApplicationManager;
	CyGroupSettingsImpl settings;

	public CyGroupSettingsTaskFactory(CyGroupAggregationManager aggMgr, 
	                                  CyApplicationManager appManager,
	                                  CyGroupSettingsImpl settings) {
		this.settings = settings;
		this.cyAggManager = aggMgr;
		this.cyApplicationManager = appManager;
	}

	public CyGroupSettingsImpl getSettings() { return settings; }

	public TaskIterator createTaskIterator() {
		CyGroupSettingsTask task = new CyGroupSettingsTask(cyAggManager, 
		                                                   cyApplicationManager, 
		                                                   settings, null);
		return new TaskIterator(task);
	}
}
