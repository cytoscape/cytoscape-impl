package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.ui.AnnotationMediator;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskFactory; 
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

public class AddAnnotationTaskFactory implements NetworkViewLocationTaskFactory, TaskFactory {

	private final AnnotationFactory<?> annotationFactory;
	private final DingRenderer dingRenderer;
	private final AnnotationMediator annotationMediator;
  private final AnnotationManager annotationManager;

	public AddAnnotationTaskFactory (
			final AnnotationManager annotationManager,
			final AnnotationFactory<?> annotationFactory
  ) {
    this.annotationManager = annotationManager;
		this.annotationFactory = annotationFactory;
		this.dingRenderer = null;
		this.annotationMediator = null;
  }


	public AddAnnotationTaskFactory(
			AnnotationFactory<?> annotationFactory,
			DingRenderer dingRenderer,
			AnnotationMediator annotationMediator
	) {
		this.annotationFactory = annotationFactory;
		this.dingRenderer = dingRenderer;
		this.annotationMediator = annotationMediator;
    this.annotationManager = null;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AddAnnotationCommandTask(annotationManager, annotationFactory));
  }

  @Override
  public boolean isReady() { return true; }

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		var re = dingRenderer.getRenderingEngine(networkView);
		
		if (re == null)
			return null;
		
		var p = new Point((int) javaPt.getX(), (int) javaPt.getY());
		
		return new TaskIterator(new AddAnnotationTask(re, p, annotationFactory, annotationMediator));
	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		var re = dingRenderer.getRenderingEngine(networkView);
		
		return re != null;
	}
}
