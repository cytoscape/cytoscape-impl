package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.cytoscape.command.StringToModel;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
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

public class GroupAnnotationsTask extends AbstractTask implements ObservableTask {
  private final AnnotationManager annotationManager;
  private final CyNetworkViewManager viewManager;
	private DRenderingEngine re;
	private final RenderingEngineManager reManager;
	private Collection<DingAnnotation> annotations;
  private CyNetworkView savedView = null;
  private GroupAnnotation groupAnnotation;

  @Tunable(description="Network View",
           longDescription=StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
           exampleStringValue=StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
           context="nogui")
  public CyNetworkView view = null;

  @Tunable(description="Annotations to group",
           longDescription="The list of UUIDs or unique names for annotations "+
                           "to be part of this group.  The special keyword 'selected' may also be used.",
           exampleStringValue="",
           context="nogui")
  public String annotationlist = null;

	
	public GroupAnnotationsTask(DRenderingEngine re) {
		this(re, null);
	}
	
	public GroupAnnotationsTask(DRenderingEngine re, Collection<DingAnnotation> annotations) {
		this.re = re;
    this.reManager = null;
		this.annotations = annotations;
    this.annotationManager = null;
    this.viewManager = null;
	}

	public GroupAnnotationsTask(AnnotationManager annotationManager, RenderingEngineManager reManager, CyNetworkViewManager viewManager) {
    this.re = null;
    this.reManager = reManager;
		this.annotations = null;
    this.annotationManager = annotationManager;
    this.viewManager = viewManager;
  }
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Group Annotations");

    if (view != null && reManager != null) {
      re = (DRenderingEngine)reManager.getRenderingEngines(view).iterator().next();
      if (annotationlist == null)
        return;
      if (annotationlist.equalsIgnoreCase("selected"))
        annotations = null;
      else {
        annotations = new ArrayList<DingAnnotation>();
        for (String ann: annotationlist.split(",")) {
          DingAnnotation a = parseAnnotation(ann.trim());
          if (a != null)
            annotations.add(a);
          else {
            tm.showMessage(TaskMonitor.Level.WARN, "Unable to parse annotation: "+ann.trim());
          }
        }
      }
    }

		if (re != null) {
			CyAnnotator cyAnnotator = re.getCyAnnotator();
			
			Collection<DingAnnotation> selectedAnnotations;
			if(annotations == null) {
				selectedAnnotations = cyAnnotator.getAnnotationSelection().getSelectedAnnotations();
			} else {
				selectedAnnotations = annotations;
			}
			
			if(selectedAnnotations.isEmpty() || !AnnotationTree.hasSameParent(selectedAnnotations))
				return;
			
			cyAnnotator.markUndoEdit("Group Annotations");

			GroupAnnotation parent = selectedAnnotations.iterator().next().getGroupParent(); // may be null
			
			// remove the annotations from any existing groups
			for(DingAnnotation a : selectedAnnotations) {
				GroupAnnotation group = a.getGroupParent();
				if(group != null) {
					group.removeMember(a);
				}
			}
			
			GroupAnnotationImpl newGroup = new GroupAnnotationImpl(re, Collections.emptyMap());

			// Now, add all of the children--do not iterate AnnotationSelection directly or that can throw
			// ConcurrentModifcationExceptions
			for(DingAnnotation a : selectedAnnotations) {
				newGroup.addMember(a);
				a.setSelected(false);
			}
			
			if(parent != null)
				parent.addMember(newGroup);
			
			cyAnnotator.addAnnotation(newGroup);

			// Finally, set ourselves to be the selected components 
			newGroup.setSelected(true);
			newGroup.update();
      groupAnnotation = newGroup;
			
			cyAnnotator.postUndoEdit();
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
      return (R)groupAnnotation;
    }

    if (type.equals(String.class)) {
      String result;
      if (groupAnnotation == null) {
        result = "Nothing groupd";
      } else
        result = "Group annotation "+groupAnnotation.toString();
      return (R)result;
    }

    if (type.equals(JSONResult.class)) {
      JSONResult res = () -> {
        if (groupAnnotation == null) {
          return "{}";
        }
        return AnnotationJsonConverter.toJson((DingAnnotation)groupAnnotation);
      };
      return (R)res;
    }
    return null;
  }

  private DingAnnotation parseAnnotation(String ann) {
    List<Annotation> allAnnotations = annotationManager.getAnnotations(view);

    // First, try to see if it's a UUID
    try {
      UUID uuid = UUID.fromString(ann);
      for (Annotation a: allAnnotations) {
        if (a.getUUID().equals(uuid))
          return (DingAnnotation)a;
      }
    } catch (IllegalArgumentException e) {
      // Nope, try a name
      for (Annotation a: allAnnotations) {
        if (ann.equals(a.getName()))
          return (DingAnnotation)a;
      }
    }

    return null;
  }
}
