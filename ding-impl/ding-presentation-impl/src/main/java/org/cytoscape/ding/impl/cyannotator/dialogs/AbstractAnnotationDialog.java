package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.util.swing.LookAndFeelUtil;

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

@SuppressWarnings("serial")
public abstract class AbstractAnnotationDialog<T extends AbstractAnnotation> extends JDialog {

	protected JComponent controlPanel;
	protected JButton applyButton;
	protected JButton cancelButton;
	
	protected T annotation;
	protected final CyAnnotator cyAnnotator;    
	protected final DRenderingEngine re;    
	protected final Point2D startingLocation;
	protected final boolean create;
	
	/**
	 * Use this constructor when creating a new annotation.
	 */
	protected AbstractAnnotationDialog(String name, T annotation, DRenderingEngine re, Point2D start, Window owner) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.annotation = annotation;
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		this.startingLocation = start != null ? start : re.getComponentCenter();
		this.create = true;
		
		setName(name);
		init();
	}

	/**
	 * Use this constructor when modifying an existing annotation.
	 */
	protected AbstractAnnotationDialog(String name, T annotation, Window owner) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.annotation = annotation;
		this.cyAnnotator = annotation.getCyAnnotator();
		this.re = cyAnnotator.getRenderingEngine();
		this.startingLocation = null;
		this.create = false;
		
		setName(name);
		init();
	}
	
	/**
	 * Use this constructor when creating a new annotation, but you don't have the annmotation yet.
	 */
	public AbstractAnnotationDialog(String name, DRenderingEngine re, Point2D start, Window owner) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		this.startingLocation = start != null ? start : re.getComponentCenter();
		this.create = true;
		
		setName(name);
		init();
	}
	
	public T getAnnotation() {
		return annotation;
	}
	
	protected void init() {
		setTitle((create ? "Create " : "Modify ") + getName() + " Annotation");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		var buttonPanel = LookAndFeelUtil.createOkCancelPanel(getApplyButton(), getCancelButton());

		var contents = new JPanel();
		var layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		var hGroup = layout.createParallelGroup(LEADING, true);
		var vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		hGroup.addComponent(getControlPanel());
		vGroup.addComponent(getControlPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);

		hGroup.addComponent(buttonPanel);
		vGroup.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getApplyButton().getAction(), getCancelButton().getAction());
		getRootPane().setDefaultButton(getApplyButton());
		
		getContentPane().add(contents);

		pack();
	}
	
	protected JComponent getControlPanel() {
		if (controlPanel == null) {
			controlPanel = createControlPanel();
		}
		
		return controlPanel;
	}
	
	protected JButton getApplyButton() {
		if (applyButton == null) {
			applyButton = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					dispose();
					cyAnnotator.markUndoEdit((create ? "Create " : "Modify ") + getName() + " Annotation");
					
					apply();
					
					if (annotation != null) {
						cyAnnotator.addAnnotation(annotation);
						cyAnnotator.clearSelectedAnnotations();
						ViewUtils.selectAnnotation(re, annotation);
						cyAnnotator.postUndoEdit(); // TODO test
					}
				}
			});
		}
		
		return applyButton;
	}
	
	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					dispose();
				}
			});
		}
		
		return cancelButton;
	}
	
	protected abstract JComponent createControlPanel();
	
	protected abstract T getPreviewAnnotation();
	
	protected abstract int getPreviewWidth();
	protected abstract int getPreviewHeight();
	
	protected abstract void apply();
}
