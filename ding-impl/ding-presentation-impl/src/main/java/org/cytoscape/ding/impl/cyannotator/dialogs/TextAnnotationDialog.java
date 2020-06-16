package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Window;
import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;

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
public class TextAnnotationDialog extends AbstractAnnotationDialog<TextAnnotationImpl> {

	private static final String NAME = "Text";
	
	private static final int PREVIEW_WIDTH = 500;
	private static final int PREVIEW_HEIGHT = 200;
	
	private TextAnnotationImpl preview;

	public TextAnnotationDialog(DRenderingEngine re, Point2D start, Window owner) {
		super(NAME, new TextAnnotationImpl(re, false), re, start, owner);
	}

	public TextAnnotationDialog(TextAnnotationImpl annotation, Window owner) {
		super(NAME, annotation, owner);
	}
	
	@Override
	protected TextAnnotationPanel createControlPanel() {
		return new TextAnnotationPanel(annotation, getPreviewPanel());
	}

	@Override
	protected TextAnnotationImpl getPreviewAnnotation() {
		if (preview == null) {
			preview = new TextAnnotationImpl(re, true);
			preview.setSize(PREVIEW_WIDTH - 10, PREVIEW_HEIGHT - 10);
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
		var cp = (TextAnnotationPanel) getControlPanel();
		
		annotation.setFont(cp.getNewFont());
		annotation.setTextColor(cp.getTextColor());
		annotation.setText(cp.getText());
		
		if (!create) {
			annotation.update();
			cyAnnotator.postUndoEdit();
			
			return;
		}

		// Apply
		var nodePoint = re.getTransform().getNodeCoordinates(startingLocation);
		annotation.setLocation(nodePoint.getX(), nodePoint.getY());
		// We need to have bounds or it won't render
		annotation.setBounds(annotation.getBounds());
		annotation.update();
	}
}
