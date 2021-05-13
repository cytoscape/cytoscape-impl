package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collections;

import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class RemoveSelectedAnnotationsTaskFactory implements NetworkViewTaskFactory {

	private final DingRenderer dingRenderer;
	private final CyServiceRegistrar serviceRegistrar;
	
	public RemoveSelectedAnnotationsTaskFactory(DingRenderer dingRenderer, CyServiceRegistrar serviceRegistrar) {
		this.dingRenderer = dingRenderer;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskIterator createTaskIterator(CyNetworkView view) {
		var re = dingRenderer.getRenderingEngine(view);
		
		if (re == null)
			return null;
		
		var cyAnnotator = re.getCyAnnotator();
		var annotations = cyAnnotator != null
				? cyAnnotator.getAnnotationSelection().getSelectedAnnotations()
				: Collections.EMPTY_LIST;

		return new TaskIterator(new RemoveSelectedAnnotationsTask(re, annotations, serviceRegistrar));
	}

	@Override
	public boolean isReady(CyNetworkView view) {
		var re = dingRenderer.getRenderingEngine(view);
		
		if (re == null)
			return false;

		var cyAnnotator = re.getCyAnnotator();
		var annotations = cyAnnotator != null ? cyAnnotator.getAnnotationSelection().getSelectedAnnotations() : null;

		return annotations != null && !annotations.isEmpty();
	}
}
