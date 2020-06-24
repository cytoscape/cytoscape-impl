package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Window;
import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;

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

@Deprecated
@SuppressWarnings("serial")
public class ShapeAnnotationDialog extends AbstractAnnotationDialog<ShapeAnnotationImpl> {

	private static final String NAME = "Shape";
	
	private static final int CREATE_WIDTH = 100;
	private static final int CREATE_HEIGHT = 100;
	
	private static final int PREVIEW_WIDTH = 500;
	private static final int PREVIEW_HEIGHT = 220;
	
	private ShapeAnnotationImpl preview;
		
	public ShapeAnnotationDialog(DRenderingEngine re, Point2D start, Window owner) {
		super(NAME, new ShapeAnnotationImpl(re, CREATE_WIDTH, CREATE_HEIGHT, false), re, start, owner);
	}

	public ShapeAnnotationDialog(ShapeAnnotationImpl annotation, Window owner) {
		super(NAME, annotation, owner);
	}
	
	@Override
	protected ShapeAnnotationEditor createControlPanel() {
		return null;
	}

	@Override
	protected ShapeAnnotationImpl getPreviewAnnotation() {
		if (preview == null) {
			preview = new ShapeAnnotationImpl(annotation, 150, 150, true);
			preview.setSize(152, 152);
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
		annotation.setShapeType(preview.getShapeType());
		annotation.setFillColor(preview.getFillColor());
		annotation.setFillOpacity(preview.getFillOpacity());
		annotation.setBorderColor(preview.getBorderColor());
		annotation.setBorderOpacity(preview.getBorderOpacity());
		annotation.setBorderWidth((int) preview.getBorderWidth());

		if (!create) {
			annotation.update();
			cyAnnotator.postUndoEdit();
			
			return;
		}

		var annotationLocation = re.getTransform().getNodeCoordinates(startingLocation);
		annotation.setLocation(annotationLocation.getX(), annotationLocation.getY());
		annotation.update();
	}
}
