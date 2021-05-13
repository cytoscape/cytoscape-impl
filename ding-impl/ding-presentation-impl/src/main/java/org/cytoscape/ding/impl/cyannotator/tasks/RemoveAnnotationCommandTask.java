package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.UUID;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

public class RemoveAnnotationCommandTask extends AbstractTask {
	
	private final AnnotationManager annotationManager;
	private final CyNetworkViewManager viewManager;

  @Tunable(context="nogui",
           required=true,
           description="The UUID or name of the annotation to be deleted")
  public String uuidOrName;

	public RemoveAnnotationCommandTask(AnnotationManager annotationManager, CyNetworkViewManager viewManager) {
		this.annotationManager = annotationManager;
		this.viewManager = viewManager;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Remove Annotation");

    // Get the UUID
    UUID aUUID = null;
    String name = null;
    try {
      aUUID = UUID.fromString(uuidOrName);
    } catch (IllegalArgumentException e) {
      name = uuidOrName.trim();  // Assume it's a name
    }

    // Get a list of all annotations, looking for the one with our UUID
    for (var view: viewManager.getNetworkViewSet()) {
      for (var annotation: annotationManager.getAnnotations(view)) {
        if ((aUUID != null && annotation.getUUID().equals(aUUID)) ||
            (name != null && annotation.getName().equals(name))) {
			    annotationManager.removeAnnotation(annotation);
          return;
        }
      }
    }
		
	}
}
