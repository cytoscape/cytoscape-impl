package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Window;

import javax.swing.JPanel;

import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;

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
public class ImageAnnotationDialog extends AbstractAnnotationDialog<ImageAnnotationImpl> {

	private static final String NAME = "Image";
	
	private static final int PREVIEW_WIDTH = 500;
	private static final int PREVIEW_HEIGHT = 350;
	
	private ImageAnnotationImpl preview;
		
	public ImageAnnotationDialog(ImageAnnotationImpl annotation, Window owner) {
		super(NAME, annotation, owner);
	}
	
	@Override
	protected JPanel createControlPanel() {
		return null;
	}

	@Override
	protected ImageAnnotationImpl getPreviewAnnotation() {
		if (preview == null) {
			var img = annotation.getImage();
			double width = (double) img.getWidth(this);
			double height = (double) img.getHeight(this);
			double scale = (Math.max(width, height)) / (PREVIEW_HEIGHT - 50);

			preview = new ImageAnnotationImpl(annotation, true);
			preview.setImage(img);
			preview.setSize(width / scale, height / scale);
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
		annotation.setBorderColor(preview.getBorderColor());
		annotation.setBorderOpacity(preview.getBorderOpacity());
		annotation.setBorderWidth((int) preview.getBorderWidth());
		annotation.setImageOpacity(preview.getImageOpacity());
		annotation.setImageBrightness(preview.getImageBrightness());
		annotation.setImageContrast(preview.getImageContrast());

		if (!create) {
			annotation.update();
			cyAnnotator.postUndoEdit();
			
			return;
		}

		annotation.setImage(preview.getImageURL());
		annotation.setLocation((int) startingLocation.getX(), (int) startingLocation.getY());
	}
}
