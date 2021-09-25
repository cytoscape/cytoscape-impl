package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
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

public class ListAnnotationsTask extends AbstractTask implements ObservableTask {

	private final AnnotationManager annotationManager;
	private List<CyNetworkView> networkViews;
  private List<Annotation> annotations;
  private CyNetworkViewManager viewManager;

  @Tunable(description="Network View",
           longDescription=StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
           exampleStringValue=StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
           context="nogui")
  public CyNetworkView view = null;

	public ListAnnotationsTask(
			AnnotationManager annotationManager,
			CyNetworkViewManager viewManager
	) {
		this.annotationManager = annotationManager;
		this.viewManager = viewManager;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("List Annotations");

    if (view != null) {
      annotations = annotationManager.getAnnotations(view);
      return;
    }

    annotations = new ArrayList<>();
    for (var view: viewManager.getNetworkViewSet())
      annotations.addAll(annotationManager.getAnnotations(view));
	}

  @Override
  public List<Class<?>> getResultClasses() {
    return Arrays.asList(JSONResult.class, String.class);
  }

  @Override
	@SuppressWarnings({ "unchecked" })
  public <R> R getResults(Class<? extends R> type) {
    if (type.equals(String.class)) {
      if (annotations.size() == 0)
        return (R)"No annotations found for view";

      StringBuilder sb = new StringBuilder();
      for (Annotation a: annotations) {
        sb.append(a.toString()+"\n");
      }
      return (R)sb.toString();
    } else if (type.equals(JSONResult.class)) {
      if (annotations.size() == 0) {
        JSONResult res = () -> {
          return "[]";
        };
        return (R)res;
      }

      JSONResult res = () -> {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Annotation a: annotations)
          sb.append(AnnotationJsonConverter.toJson((DingAnnotation)a)+",");

        int len = sb.length();
        sb.replace(len-1,len,"]");
        return sb.toString();
      };
      return (R)res;
    }
    return null;
  }

}
