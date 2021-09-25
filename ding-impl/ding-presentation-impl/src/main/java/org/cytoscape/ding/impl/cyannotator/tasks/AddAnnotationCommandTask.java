package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.BoundedTextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

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

public class AddAnnotationCommandTask extends AbstractTask implements ObservableTask {

	private final AnnotationManager annotationManager;
  private final AnnotationFactory<?> annotationFactory;
  private Annotation newAnnotation;

  @Tunable(description="Network View",
           longDescription=StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
           exampleStringValue=StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
           required=true,
           context="nogui")
  public CyNetworkView view = null;

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

	public AddAnnotationCommandTask(
			final AnnotationManager annotationManager,
			final AnnotationFactory<?> annotationFactory
  ) {
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;

    try {
		if (annotationFactory instanceof AbstractDingAnnotationFactory) {
      if (annotationFactory instanceof ImageAnnotationFactory) {
         standardTunables = new StandardAnnotationTunables();
         shapeTunables = new ShapeAnnotationTunables();
         imageTunables = new ImageAnnotationTunables();
       } else if (annotationFactory instanceof BoundedTextAnnotationFactory) {
         standardTunables = new StandardAnnotationTunables();
         textTunables = new TextAnnotationTunables();
         shapeTunables = new ShapeAnnotationTunables();
       } else if (annotationFactory instanceof TextAnnotationFactory) {
         standardTunables = new StandardAnnotationTunables();
         textTunables = new TextAnnotationTunables();
       } else if (annotationFactory instanceof ShapeAnnotationFactory) {
         standardTunables = new StandardAnnotationTunables();
         shapeTunables = new ShapeAnnotationTunables();
       } else if (annotationFactory instanceof ArrowAnnotationFactory) {
       }
    }
    } catch (Exception e) { e.printStackTrace(); }
  }

  @Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Add Annotation");

    if (view == null) {
      tm.setStatusMessage("No view selected");
      return;
    }

    try {
    if (annotationFactory instanceof ImageAnnotationFactory) {
      var args = standardTunables.getArgMap(tm);
      args.putAll(shapeTunables.getArgMap(tm));
      args.putAll(imageTunables.getArgMap(tm));
      newAnnotation = ((ImageAnnotationFactory)annotationFactory).createAnnotation(ImageAnnotation.class, view, args);
    } else if (annotationFactory instanceof BoundedTextAnnotationFactory) {
      var args = standardTunables.getArgMap(tm);
      args.putAll(shapeTunables.getArgMap(tm));
      args.putAll(textTunables.getArgMap(tm));
      newAnnotation = ((BoundedTextAnnotationFactory)annotationFactory).createAnnotation(BoundedTextAnnotation.class, view, args);
    } else if (annotationFactory instanceof TextAnnotationFactory) {
      var args = standardTunables.getArgMap(tm);
      args.putAll(textTunables.getArgMap(tm));
      newAnnotation = ((TextAnnotationFactory)annotationFactory).createAnnotation(TextAnnotation.class, view, args);
    } else if (annotationFactory instanceof ShapeAnnotationFactory) {
      var args = standardTunables.getArgMap(tm);
      args.putAll(shapeTunables.getArgMap(tm));
      newAnnotation = ((ShapeAnnotationFactory)annotationFactory).createAnnotation(ShapeAnnotation.class, view, args);
    } else if (annotationFactory instanceof ArrowAnnotationFactory) {
    }

    annotationManager.addAnnotation(newAnnotation);
    } catch (Exception e) {
      e.printStackTrace();
    }
	}

  @Override
  public List<Class<?>> getResultClasses() {
    return Arrays.asList(JSONResult.class, String.class, Annotation.class);
  }

  @Override
	@SuppressWarnings({ "unchecked" })
  public <R> R getResults(Class<? extends R> type) {
    if (type.equals(Annotation.class)) {
      return (R)newAnnotation;
    }

    if (type.equals(String.class)) {
      String result;
      if (newAnnotation == null) {
        result = "Nothing added";
      } else
        result = "Created annotation "+newAnnotation.toString();
      return (R)result;
    }

    if (type.equals(JSONResult.class)) {
      JSONResult res = () -> {
        if (newAnnotation == null) {
          return "{}";
        }
        return AnnotationJsonConverter.toJson((DingAnnotation)newAnnotation);
      };
      return (R)res;
    }
    return null;
  }
}
