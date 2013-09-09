package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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

import java.io.InputStream;

import org.cytoscape.biopax.internal.util.VisualStyleUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.work.TaskIterator;

public class BioPaxReaderTaskFactory extends AbstractInputStreamTaskFactory {

	private final CyServices cyServices;
	private final VisualStyleUtil visualStyleUtil;

	public BioPaxReaderTaskFactory(CyFileFilter filter, CyServices cyServices, VisualStyleUtil visualStyleUtil)
	{
		super(filter);
		this.cyServices = cyServices;
		this.visualStyleUtil = visualStyleUtil;
	}
	

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {
		if(inputName == null)
			inputName = "BioPAX_Network"; //default name fallback
		
		return new TaskIterator(
			new BioPaxReaderTask(is, inputName, cyServices, visualStyleUtil)
		);
	}

}
