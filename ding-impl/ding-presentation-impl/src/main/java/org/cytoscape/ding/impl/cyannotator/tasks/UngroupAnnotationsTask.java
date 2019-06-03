package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
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

public class UngroupAnnotationsTask extends AbstractTask {
	
	private final DRenderingEngine re;
	private Set<GroupAnnotation> groups;

	public UngroupAnnotationsTask(DRenderingEngine re, DingAnnotation annotation) {
		this.re = re;
		if (annotation instanceof GroupAnnotation)
			groups = Collections.singleton((GroupAnnotation) annotation);
	}
	
	public UngroupAnnotationsTask(DRenderingEngine re, Collection<GroupAnnotation> annotations) {
		this.re = re;
		groups = annotations != null ? new LinkedHashSet<>(annotations) : Collections.emptySet();
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Ungroup Annotations");
		
		if (re != null) {
			CyAnnotator annotator = re.getCyAnnotator();
			annotator.markUndoEdit("Ungroup Annotations");
			
			for(GroupAnnotation ga : groups) {
				GroupAnnotation parent = ((DingAnnotation)ga).getGroupParent();
				List<Annotation> members = ga.getMembers();
				
				for(Annotation a : members) {
					ga.removeMember(a);
					a.setSelected(true);
				}
				
				// move the annotations into the parent
				if(parent != null) {
					for(Annotation a : members) {
						parent.addMember(a);
					}
				}
				
				ga.removeAnnotation(); // this fires an event so it must go at the end
			}
			
			annotator.postUndoEdit();
		}
	}
}
