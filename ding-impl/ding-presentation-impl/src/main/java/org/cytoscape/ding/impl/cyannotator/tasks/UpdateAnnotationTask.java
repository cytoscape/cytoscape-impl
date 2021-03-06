package org.cytoscape.ding.impl.cyannotator.tasks;

import static java.util.Collections.emptyMap;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.cytoscape.command.StringToModel;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.BoundedTextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
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

public class UpdateAnnotationTask extends AbstractTask implements ObservableTask {

	private final AnnotationManager annotationManager;
  private final CyNetworkViewManager viewManager;
  private final Class<? extends Annotation> type;
  private Annotation updatedAnnotation;

  @Tunable(context="nogui",
           required=true,
           description="The UUID or name of the annotation to be updated")
  public String uuidOrName;

  // Standard annotation values
  @ContainsTunables
  public StandardAnnotationTunables standardTunables = null;

  // Text annotation values
  @ContainsTunables
  public TextAnnotationTunables textTunables = null;

  // Shape annotation values
  @ContainsTunables
  public ShapeAnnotationTunables shapeTunables = null;

  // Image annotation values
  @ContainsTunables
  public ImageAnnotationTunables imageTunables = null;

  // Arrow annotation values
  // @ContainsTunables
  // public ArrowAnnotationTunables arrowTunables = null;

	public UpdateAnnotationTask(
      final Class<? extends Annotation> type,
			final AnnotationManager annotationManager,
			final CyNetworkViewManager viewManager
  ) {
    this.annotationManager = annotationManager;
    this.viewManager = viewManager;
    this.type = type;
		if (type.equals(ImageAnnotation.class)) {
       standardTunables = new StandardAnnotationTunables();
       shapeTunables = new ShapeAnnotationTunables();
       imageTunables = new ImageAnnotationTunables();
     } else if (type.equals(BoundedTextAnnotation.class)) {
       standardTunables = new StandardAnnotationTunables();
       textTunables = new TextAnnotationTunables();
       shapeTunables = new ShapeAnnotationTunables();
     } else if (type.equals(TextAnnotation.class)) {
       standardTunables = new StandardAnnotationTunables();
       textTunables = new TextAnnotationTunables();
     } else if (type.equals(ShapeAnnotation.class)) {
       standardTunables = new StandardAnnotationTunables();
       shapeTunables = new ShapeAnnotationTunables();
     } else if (type.equals(ArrowAnnotation.class)) {
     } else if (type.equals(GroupAnnotation.class)) {
       standardTunables = new StandardAnnotationTunables();
     }
  }

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Update Annotation");

    // Get the UUID
    UUID aUUID = null;
    String name = null;
    try {
      aUUID = UUID.fromString(uuidOrName);
    } catch (IllegalArgumentException e) {
      name = uuidOrName;
    }

    // Get a list of all annotations, looking for the one with our UUID
    for (var view: viewManager.getNetworkViewSet()) {
      for (var annotation: annotationManager.getAnnotations(view)) {
        if ((aUUID != null && annotation.getUUID().equals(aUUID)) ||
            (name != null && annotation.getName().equals(name))) {
          try {
			    updateAnnotation(tm, annotation);
          } catch (Exception e) { e.printStackTrace(); }
          updatedAnnotation = annotation;
          return;
        }
      }
    }

    tm.setStatusMessage("Can't find an annotation with UUID "+uuidOrName);

	}

  private void updateAnnotation(TaskMonitor tm, Annotation annotation) {
    
		if (type.equals(ImageAnnotation.class)) {
      standardTunables.update(tm, annotation);
      shapeTunables.update(tm, annotation);
      imageTunables.update(tm, annotation);
    } else if (type.equals(BoundedTextAnnotation.class)) {
      standardTunables.update(tm, annotation);
      shapeTunables.update(tm, annotation);
      textTunables.update(tm, annotation);
    } else if (type.equals(TextAnnotation.class)) {
      standardTunables.update(tm, annotation);
      textTunables.update(tm, annotation);
    } else if (type.equals(ShapeAnnotation.class)) {
      standardTunables.update(tm, annotation);
      shapeTunables.update(tm, annotation);
    } else if (type.equals(GroupAnnotation.class)) {
      
      standardTunables.update(tm, annotation);
      
    }
    
    annotation.update();
    
  }

  @Override
  public List<Class<?>> getResultClasses() {
    return Arrays.asList(JSONResult.class, String.class, Annotation.class);
  }

  @Override
	@SuppressWarnings({ "unchecked" })
  public <R> R getResults(Class<? extends R> type) {
    if (type.equals(Annotation.class)) {
      return (R)updatedAnnotation;
    }

    if (type.equals(String.class)) {
      String result;
      if (updatedAnnotation == null) {
        result = "Nothing added";
      } else
        result = "Updated annotation "+updatedAnnotation.toString();
      return (R)result;
    }

    if (type.equals(JSONResult.class)) {
      JSONResult res = () -> {
        if (updatedAnnotation == null) {
          return "{}";
        }
        return ((DingAnnotation)updatedAnnotation).toJSON();
      };
      return (R)res;
    }
    return null;
  }
}
