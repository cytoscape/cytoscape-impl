package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

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

@SuppressWarnings("serial")
public class ArrowAnnotationDialog extends JDialog {
	
	private static final int PREVIEW_WIDTH = 400;
	private static final int PREVIEW_HEIGHT = 120;
	
	private JButton applyButton;
	private JButton cancelButton;

	private final CyAnnotator cyAnnotator;    
	private final DGraphView view;    
	private final ArrowAnnotationImpl annotation;
	private ArrowAnnotationImpl preview;
	private DingAnnotation source;
	private final boolean create;
		
	public ArrowAnnotationDialog(final DGraphView view, final Point2D start, final Window owner) {
		super(owner);
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
		this.annotation = new ArrowAnnotationImpl(cyAnnotator, view, owner);
		this.source = cyAnnotator.getAnnotationAt(start);
		this.create = true;

		initComponents();
	}

	public ArrowAnnotationDialog(final ArrowAnnotationImpl annotation, final Window owner) {
		super(owner);
		this.annotation = annotation;
		this.cyAnnotator = annotation.getCyAnnotator();
		this.view = cyAnnotator.getView();
		this.create = false;

		initComponents();
	}
    
	private void initComponents() {
		setTitle(create ? "Create Arrow Annotation" : "Modify Arrow Annotation");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);

		// Create the preview panel
		preview = new ArrowAnnotationImpl(cyAnnotator, view, getOwner());
		preview.setUsedForPreviews(true);
		((ArrowAnnotationImpl) preview).setSize(400.0, 100.0);
		
		final PreviewPanel previewPanel = new PreviewPanel(preview);
		final JPanel arrowAnnotationPanel = new ArrowAnnotationPanel(annotation, previewPanel);

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
				.addComponent(arrowAnnotationPanel)
				.addComponent(previewPanel, DEFAULT_SIZE, PREVIEW_WIDTH, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(arrowAnnotationPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
			return;
		}

		annotation.addComponent(null);
		annotation.setSource(this.source);
		annotation.update();

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

		// Set this shape to be resized
		cyAnnotator.positionArrow(annotation);

		try {
			// Warp the mouse to the starting location (if supported)
			Point start = annotation.getComponent().getLocationOnScreen();
			Robot robot = new Robot();
			robot.mouseMove((int) start.getX() + 100, (int) start.getY() + 100);
		} catch (Exception e) {
		}
	}
}
