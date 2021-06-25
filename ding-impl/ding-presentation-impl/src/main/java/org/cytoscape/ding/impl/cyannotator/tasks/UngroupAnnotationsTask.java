package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.cytoscape.command.StringToModel;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
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

public class UngroupAnnotationsTask extends AbstractTask {
	private final AnnotationManager annotationManager;
	private final CyNetworkViewManager viewManager;
	private DRenderingEngine re;
	private final RenderingEngineManager reManager;
	private Set<GroupAnnotation> groups;

	@Tunable(description = "Network View", longDescription = StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION, exampleStringValue = StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING, context = "nogui")
	public CyNetworkView view = null;

	@Tunable(context = "nogui", required = true, description = "The UUID or name of the group to be ungrouped")
	public String uuidOrName;

	public UngroupAnnotationsTask(DRenderingEngine re, DingAnnotation annotation) {
		this.re = re;
		this.viewManager = null;
		this.reManager = null;
		this.annotationManager = null;
		if (annotation instanceof GroupAnnotation)
			groups = Collections.singleton((GroupAnnotation) annotation);
	}

	public UngroupAnnotationsTask(DRenderingEngine re, Collection<GroupAnnotation> annotations) {
		this.re = re;
		this.viewManager = null;
		this.reManager = null;
		this.annotationManager = null;
		groups = annotations != null ? new LinkedHashSet<>(annotations) : Collections.emptySet();
	}

	public UngroupAnnotationsTask(AnnotationManager annotationManager, RenderingEngineManager reManager, CyNetworkViewManager viewManager) {
		this.annotationManager = annotationManager;
		this.viewManager = viewManager;
		this.reManager = reManager;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Ungroup Annotations");

		if (view != null && reManager != null) {
			re = (DRenderingEngine) reManager.getRenderingEngines(view).iterator().next();
			// Get the UUID
			UUID aUUID = null;
			String name = null;
			try {
				aUUID = UUID.fromString(uuidOrName);
			} catch (IllegalArgumentException e) {
				name = uuidOrName.trim(); // Assume it's a name
			}

			for (var annotation : annotationManager.getAnnotations(view)) {
				if ((aUUID != null && annotation.getUUID().equals(aUUID)) || (name != null && annotation.getName().equals(name))) {
					if (annotation instanceof GroupAnnotation) {
						groups = Collections.singleton((GroupAnnotation) annotation);
						break;
					}
				}
			}
			if (groups == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Unable to find group annotation: " + uuidOrName);
				return;
			}
		}

		if (re != null) {
			CyAnnotator annotator = re.getCyAnnotator();
			annotator.markUndoEdit("Ungroup Annotations");

			for (GroupAnnotation ga : groups) {
				GroupAnnotation parent = ((DingAnnotation) ga).getGroupParent();
				List<Annotation> members = ga.getMembers();

				for (Annotation a : members) {
					ga.removeMember(a);
					a.setSelected(true);
				}

				// move the annotations into the parent
				if (parent != null) {
					for (Annotation a : members) {
						parent.addMember(a);
					}
				}

				ga.removeAnnotation(); // this fires an event so it must go at the end
			}

			annotator.postUndoEdit();
		}
	}
}
