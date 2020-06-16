package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Robot;
import java.awt.Window;
import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class ArrowAnnotationDialog extends AbstractAnnotationDialog<ArrowAnnotationImpl> {
	
	private static final String NAME = "Arrow";
	
	private static final int PREVIEW_WIDTH = 400;
	private static final int PREVIEW_HEIGHT = 120;
	
	private ArrowAnnotationImpl preview;
	private DingAnnotation source;
	
	public ArrowAnnotationDialog(DRenderingEngine re, Point2D start, Window owner) {
		super(NAME, new ArrowAnnotationImpl(re, false), re, start, owner);
		
		this.source = re.getPicker().getAnnotationAt(startingLocation);
	}

	public ArrowAnnotationDialog(ArrowAnnotationImpl annotation, Window owner) {
		super(NAME, annotation, owner);
	}
    
	@Override
	protected ArrowAnnotationPanel createControlPanel() {
		return new ArrowAnnotationPanel(annotation, getPreviewPanel());
	}

	@Override
	protected ArrowAnnotationImpl getPreviewAnnotation() {
		if (preview == null) {
			preview = new ArrowAnnotationImpl(re, true);
			preview.setSize(400.0, 100.0);
		}
		
		return preview;
	}
	
	@Override
	protected int getPreviewWidth() {
		return PREVIEW_WIDTH;
	}

	@Override
	protected int getPreviewHeight() {
		return PREVIEW_HEIGHT;
	}
	
	@Override
	protected void apply() {
		annotation.setLineColor(preview.getLineColor());
		annotation.setLineWidth(preview.getLineWidth());
		annotation.setArrowType(ArrowEnd.SOURCE, preview.getArrowType(ArrowEnd.SOURCE));
		annotation.setArrowColor(ArrowEnd.SOURCE, preview.getArrowColor(ArrowEnd.SOURCE));
		annotation.setArrowSize(ArrowEnd.SOURCE, preview.getArrowSize(ArrowEnd.SOURCE));
		annotation.setAnchorType(ArrowEnd.SOURCE, preview.getAnchorType(ArrowEnd.SOURCE));
		annotation.setArrowType(ArrowEnd.TARGET, preview.getArrowType(ArrowEnd.TARGET));
		annotation.setArrowColor(ArrowEnd.TARGET, preview.getArrowColor(ArrowEnd.TARGET));
		annotation.setArrowSize(ArrowEnd.TARGET, preview.getArrowSize(ArrowEnd.TARGET));
		annotation.setAnchorType(ArrowEnd.TARGET, preview.getAnchorType(ArrowEnd.TARGET));

		if (!create) {
			annotation.update();
			cyAnnotator.postUndoEdit();
			
			return;
		}

		annotation.setSource(source);
		annotation.update();
		cyAnnotator.addAnnotation(annotation);

		// Set this shape to be resized
		cyAnnotator.positionArrow(annotation);

		try {
			// Warp the mouse to the starting location (if supported)
			var start = re.getComponent().getLocationOnScreen();
			var robot = new Robot();
			robot.mouseMove((int) start.getX() + 100, (int) start.getY() + 100);
		} catch (Exception e) {
		}
	}
}
