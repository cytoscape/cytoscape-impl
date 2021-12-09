package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

public class RemoveSelectedAnnotationsTask extends AbstractTask {
	
	private final DRenderingEngine re;
	private final Collection<? extends Annotation> annotations;
	private final CyServiceRegistrar serviceRegistrar;

	public RemoveSelectedAnnotationsTask(DRenderingEngine re, Collection<? extends Annotation> annotations, CyServiceRegistrar serviceRegistrar) {
		this.re = re;
		this.annotations = annotations;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Remove Annotations");
		tm.setProgress(-1);

		if (re != null && annotations != null && !annotations.isEmpty()) {
			tm.setStatusMessage("Deleting " + annotations.size() + " annotation(s)...");
			
			var annotator = re.getCyAnnotator();
			annotator.markUndoEdit("Remove Annotations");
			
			Collection<? extends Annotation> newList = annotations;

			do {
				newList = remove(newList);
			} while (!newList.isEmpty());
			
			annotator.postUndoEdit();
		}
	}

	private Collection<? extends Annotation> remove(Collection<? extends Annotation> list) {
		// Save the groups from the annotations to be removed
		var groups = getGroups(list);
		
		// Remove the annotations
		serviceRegistrar.getService(AnnotationManager.class).removeAnnotations(list);

		// Check if there are groups with one or no members
		var iter = groups.iterator();
		
		while (iter.hasNext()) {
			var g = iter.next();
			var members = g.getMembers();

			if (members != null) {
				if (members.size() > 1)
					iter.remove();
				else
					members.forEach(a -> g.removeMember(a)); // Ungroup before removing the useless group
			}
		}
		
		return groups;
	}
	
	private Set<GroupAnnotation> getGroups(Collection<? extends Annotation> list) {
		var groups = new HashSet<GroupAnnotation>();
		
		list.forEach(a -> {
			if (a instanceof DingAnnotation) {
				var g = ((DingAnnotation) a).getGroupParent();
			
				if (g != null)
					groups.add(g);
			}
		});
		
		return groups;
	}
}
