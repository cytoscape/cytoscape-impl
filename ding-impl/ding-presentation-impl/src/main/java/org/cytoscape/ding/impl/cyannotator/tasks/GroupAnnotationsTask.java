package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.AbstractTask;
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

public class GroupAnnotationsTask extends AbstractTask {

	private final DRenderingEngine re;
	private Collection<DingAnnotation> annotations;
	
	public GroupAnnotationsTask(DRenderingEngine re) {
		this(re, null);
	}
	
	public GroupAnnotationsTask(DRenderingEngine re, Collection<DingAnnotation> annotations) {
		this.re = re;
		this.annotations = annotations;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Group Annotations");
		
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
			newGroup.addComponent(null); // Need to add this first so we can update things appropriately

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
			
			cyAnnotator.postUndoEdit();
		}
	}
}
