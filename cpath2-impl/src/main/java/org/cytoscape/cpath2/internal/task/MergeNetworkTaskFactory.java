package org.cytoscape.cpath2.internal.task;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.cpath2.internal.CPathFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MergeNetworkTaskFactory extends AbstractTaskFactory {
	private final URL cpathURL;
	private final CyNetwork cyNetwork;
	private final CPathFactory factory;

	public MergeNetworkTaskFactory(URL cpathURL, CyNetwork cyNetwork, CPathFactory factory) {
		this.cpathURL = cpathURL;
		this.cyNetwork = cyNetwork;
		this.factory = factory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MergeNetworkTask(cpathURL, cyNetwork, factory));
	}

}
