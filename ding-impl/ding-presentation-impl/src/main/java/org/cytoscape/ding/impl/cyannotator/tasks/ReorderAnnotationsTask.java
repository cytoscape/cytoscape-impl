package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator.ReorderType;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
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

public class ReorderAnnotationsTask extends AbstractNetworkViewTask {

	private final List<DingAnnotation> annotations;
	private final String canvasName;
	private final Integer offset;

	public ReorderAnnotationsTask(
			CyNetworkView view,
			Collection<DingAnnotation> annotations,
			String canvasName,
			Integer offset
	) {
		super(view);
		this.annotations = annotations != null ? new ArrayList<>(annotations) : Collections.emptyList();
		this.canvasName = canvasName;
		this.offset = offset;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (annotations.isEmpty())
			return;
		
		if (canvasName == null && (offset == null || offset == 0))
			return;
		
		if (annotations.size() > 1)
			sort(annotations);
		
		if (canvasName != null)
			changeCanvas(offset);
		else if (offset != null)
			reorder(offset);
		
		if (view instanceof DGraphView) {
			ReorderType type = canvasName != null ? ReorderType.CANVAS : ReorderType.Z_INDEX;
			((DGraphView) view).getCyAnnotator().annotationsReordered(type);
		}
	}

	private void changeCanvas(Integer offset) {
		for (int i = annotations.size() - 1; i >= 0; i--)
			annotations.get(i).changeCanvas(canvasName);
		
		if (offset != null && offset != 0) {
			if (annotations.size() > 1)
				sort(annotations);
			
			reorder(offset);
		}
	}

	private void reorder(Integer offset) {
		offset = getRealOffset(annotations.get(0), offset);
		
		if (offset != null && offset != 0) {
			for (DingAnnotation a : annotations)
				reorder(a, offset);
		}
	}

	protected void reorder(DingAnnotation annotation, Integer offset) {
		if (offset == null)
			return;
		
		final JComponent canvas = annotation.getCanvas();
		int z = getNewZOrder(annotation, offset);
		
		if (annotation instanceof GroupAnnotation) {
			int zz = z;
			
			for (Annotation a : ((GroupAnnotation) annotation).getMembers()) {
				DingAnnotation da = (DingAnnotation) a;
				zz++;
				int total = da.getCanvas().getComponentCount();
				zz = Math.min(zz, total - 1);
				zz = Math.max(zz, 0);
				da.getCanvas().setComponentZOrder(da.getComponent(), zz);
			}
		}
		
		canvas.setComponentZOrder(annotation.getComponent(), z);
		canvas.repaint();
		annotation.contentChanged(); // We need to do this to update the Bird's Eye View
	}
	
	private Integer getRealOffset(DingAnnotation annotation, Integer offset) {
		if (offset == null)
			return null;
		
		final JComponent canvas = annotation.getCanvas();
		final int total = canvas.getComponentCount();
		
		offset = Math.min(offset, total);
		offset = Math.max(offset, -total);
		
		final int z = getNewZOrder(annotation, offset);
		offset = z - canvas.getComponentZOrder(annotation.getComponent());
		
		return offset;
	}
	
	private int getNewZOrder(DingAnnotation annotation, int offset) {
		final JComponent canvas = annotation.getCanvas();
		int z = canvas.getComponentZOrder(annotation.getComponent());
		
		if (isOffsetValid(offset, annotation)) {
			z = z + offset;
			
			if (z < 0)
				z = 0;
			else if (z > canvas.getComponentCount() - 1)
				z = canvas.getComponentCount() - 1;
		}
		
		return z;
	}
	
	private void sort(List<DingAnnotation> list) {
		Collections.sort(list, (a1, a2) -> {
			if (a1 instanceof DingAnnotation && a2 instanceof DingAnnotation) {
				JComponent canvas1 = ((DingAnnotation) a1).getCanvas();
				JComponent canvas2 = ((DingAnnotation) a2).getCanvas();
				int z1 = canvas1.getComponentZOrder(((DingAnnotation) a1).getComponent());
				int z2 = canvas2.getComponentZOrder(((DingAnnotation) a2).getComponent());
				
				if (offset != null && offset < 0)
					return Integer.compare(z1, z2);
				
				return Integer.compare(z2, z1);
			}
			
			return 0;
		});
	}
	
	protected static boolean isOffsetValid(Integer offset, DingAnnotation annotation) {
		if (offset == null)
			return false;
		
		final JComponent canvas = annotation.getCanvas();
		final int z = canvas.getComponentZOrder(annotation.getComponent());
		
		return (offset < 0 && z > 0) || (offset > 0 && z < canvas.getComponentCount() - 1);
	}
}
