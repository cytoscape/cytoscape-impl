package org.cytoscape.ding.impl.cyannotator.dialogs;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class ShapeAnnotationDialog extends JDialog {

	private static final int PREVIEW_WIDTH = 500;
	private static final int PREVIEW_HEIGHT = 220;
	
	private ShapeAnnotationPanel shapeAnnotationPanel;
	private JButton applyButton;
	private JButton cancelButton;

	private final CyAnnotator cyAnnotator;    
	private final DGraphView view;    
	private final Point2D startingLocation;
	private final ShapeAnnotationImpl shapeAnnotation;
	private ShapeAnnotationImpl preview;
	private final boolean create;
		
	public ShapeAnnotationDialog(final DGraphView view, final Point2D start, final Window owner) {
		super(owner);
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = start;
		this.shapeAnnotation = new ShapeAnnotationImpl(cyAnnotator, view, 400, 400, owner);
		this.create = true;

		initComponents();		        
	}

	public ShapeAnnotationDialog(final ShapeAnnotationImpl mAnnotation, final Window owner) {
		super(owner);
		this.shapeAnnotation = mAnnotation;
		this.cyAnnotator = mAnnotation.getCyAnnotator();
		this.view = cyAnnotator.getView();
		this.create = false;
		this.startingLocation = null;

		initComponents();	
	}
    
	private void initComponents() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);
		setTitle(create ? "Create Shape Annotation" : "Modify Shape Annotation");
		
		// Create the preview panel
		preview = new ShapeAnnotationImpl(shapeAnnotation, 150, 150, getOwner());
		preview.setUsedForPreviews(true);
		preview.getComponent().setSize(152, 152);
		final PreviewPanel previewPanel = new PreviewPanel(preview);

		shapeAnnotationPanel = new ShapeAnnotationPanel(shapeAnnotation, previewPanel);

		applyButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyButtonActionPerformed(e);
			}
		});
		cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(applyButton, cancelButton);

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(shapeAnnotationPanel)
				.addComponent(previewPanel, DEFAULT_SIZE, PREVIEW_WIDTH, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(shapeAnnotationPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(previewPanel, DEFAULT_SIZE, PREVIEW_HEIGHT, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), applyButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(applyButton);
		
		getContentPane().add(contents);
		pack();
	}

	private void applyButtonActionPerformed(ActionEvent evt) {
		dispose();           
		
		shapeAnnotation.setShapeType(preview.getShapeType());
		shapeAnnotation.setFillColor(preview.getFillColor());
		shapeAnnotation.setFillOpacity(preview.getFillOpacity());
		shapeAnnotation.setBorderColor(preview.getBorderColor());
		shapeAnnotation.setBorderOpacity(preview.getBorderOpacity());
		shapeAnnotation.setBorderWidth((int)preview.getBorderWidth());

		if (!create) {
			shapeAnnotation.update(); 
			return;
		}

		shapeAnnotation.getComponent().setLocation((int)startingLocation.getX(), (int)startingLocation.getY());
		shapeAnnotation.addComponent(null);
		shapeAnnotation.update();

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

		// Set this shape to be resized
		cyAnnotator.resizeShape(shapeAnnotation);

		try {
			// Warp the mouse to the starting location (if supported)
			Point start = shapeAnnotation.getComponent().getLocationOnScreen();
			Robot robot = new Robot();
			robot.mouseMove((int)start.getX()+100, (int)start.getY()+100);
		} catch (Exception e) {}
	}
}
