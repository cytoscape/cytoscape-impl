package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class GroupAnnotationsTask extends AbstractNetworkViewTask {

	
	private Collection<DingAnnotation> annotations;
	
	/**
	 * Group the selected annotations, if any.
	 */
	public GroupAnnotationsTask(CyNetworkView view) {
		super(view);
	}
	
	/**
	 * Group the passed annotations.
	 */
	public GroupAnnotationsTask(CyNetworkView view, Collection<DingAnnotation> annotations) {
		super(view);
		this.annotations = annotations;
	}
	

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if(annotations.isEmpty() || !AnnotationTree.hasSameParent(annotations))
			return;
		
		if (view instanceof DGraphView) {
			DGraphView dView = (DGraphView) view;
			
			CyAnnotator cyAnnotator = dView.getCyAnnotator();
			
			Collection<DingAnnotation> selectedAnnotations;
			if(annotations == null)
				selectedAnnotations = cyAnnotator.getAnnotationSelection().getSelectedAnnotations();
			else
				selectedAnnotations = annotations;
			
			cyAnnotator.markUndoEdit("Group Annotations");

			GroupAnnotation parent = annotations.iterator().next().getGroupParent(); // may be null
			
			// remove the annotations from any existing groups
			for(DingAnnotation a : selectedAnnotations) {
				GroupAnnotation group = a.getGroupParent();
				if(group != null) {
					group.removeMember(a);
				}
			}
			
			GroupAnnotationImpl newGroup = new GroupAnnotationImpl(dView, Collections.emptyMap());
			newGroup.addComponent(null); // Need to add this first so we can update things appropriately

			// Now, add all of the children--do not iterate AnnotationSelection directly or that can throw
			// ConcurrentModifcationExceptions
			for(DingAnnotation a : selectedAnnotations) {
				newGroup.addMember(a);
				a.setSelected(false);
			};
			
			if(parent != null)
				parent.addMember(newGroup);
			
			cyAnnotator.addAnnotation(newGroup);

			// Finally, set ourselves to be the selected components 
			newGroup.setSelected(true);
			newGroup.update();
			
			cyAnnotator.postUndoEdit();
		}
	}
}
