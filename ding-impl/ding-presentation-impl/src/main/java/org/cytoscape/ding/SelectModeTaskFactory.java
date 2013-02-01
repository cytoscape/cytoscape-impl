package org.cytoscape.ding;

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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SelectModeTaskFactory extends AbstractTaskFactory {

	private CyApplicationManager applicationManagerServiceRef;
	private String actionName;
	
	public SelectModeTaskFactory(String actionName, CyApplicationManager applicationManagerServiceRef){
	
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.actionName = actionName;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new SelectModeTask(actionName, applicationManagerServiceRef));
	}
}
