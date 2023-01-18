package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.cg.util.ImageCustomGraphicsSelector;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Provides a way to create ImageAnnotations
 */
@SuppressWarnings("serial")
public class LoadImageDialog extends AbstractAnnotationDialog<ImageAnnotationImpl> {

	private static final String NAME = "Image";
	private static final String UNDO_LABEL = "Create Image Annotation";
	
	private ImageCustomGraphicsSelector imageSelector;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public LoadImageDialog(
			DRenderingEngine re,
			Point2D start,
			Window owner,
			CyServiceRegistrar serviceRegistrar
	) {
		super(NAME, re, start, owner);
		
		this.serviceRegistrar = serviceRegistrar;
		
		setTitle("Select an Image");
		setResizable(true);
		
		getControlPanel().add(getImageSelector(), BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				// Let the user load new images right away if the selector is empty
				if (getImageSelector().isEmpty())
					getImageSelector().loadNewImages();
			}
		});
		
		pack();
	}
	
	@Override
	protected JComponent createControlPanel() {
		var panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));

		return panel;
	}

	@Override
	protected ImageAnnotationImpl getPreviewAnnotation() {
		return null;
	}
	
	@Override
	protected int getPreviewWidth() {
		return 0;
	}

	@Override
	protected int getPreviewHeight() {
		return 0;
	}
	
	@Override
	protected void apply() {
		try {
			var cg = getImageSelector().getSelectedImage();
			
			if (cg != null)
				annotation = createAnnotation(cg);
			
			if (annotation != null) {
				var nodePoint = re.getTransform().getNodeCoordinates(startingLocation);
				var w = annotation.getWidth();
				var h = annotation.getHeight();
				
				annotation.setLocation(nodePoint.getX() - w / 2.0, nodePoint.getY() - h / 2.0);
				annotation.update();
				
				cyAnnotator.clearSelectedAnnotations();
				ViewUtils.selectAnnotation(re, annotation);
			}
		} catch (Exception ex) {
			logger.warn("Unable to load the selected image", ex);
		}
	}

	@Override
	protected JButton getApplyButton() {
		if (applyButton == null) {
			applyButton = new JButton(new AbstractAction("Insert") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					apply();
					dispose();
				}
			});
		}
		
		return applyButton;
	}
	
	private ImageCustomGraphicsSelector getImageSelector() {
		if (imageSelector == null) {
			imageSelector = new ImageCustomGraphicsSelector(serviceRegistrar);
			imageSelector.addActionListener(evt -> getApplyButton().doClick());	
		}
		
		return imageSelector;
	}
	
	private ImageAnnotationImpl createAnnotation(CyCustomGraphics<?> cg) {
		ImageAnnotationImpl annotation = null;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		if (cg instanceof SVGCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(SVGCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					re.getZoom(),
					cgManager
			);
		} else if (cg instanceof BitmapCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(BitmapCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					0d, // rotation
					re.getZoom(),
					cgManager
			);
		}
		
		return annotation;
	}
}
