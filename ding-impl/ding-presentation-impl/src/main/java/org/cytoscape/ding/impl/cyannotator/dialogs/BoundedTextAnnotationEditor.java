package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.JSeparator;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

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
public class BoundedTextAnnotationEditor extends AbstractAnnotationEditor<BoundedTextAnnotation> {
	
	private TextAnnotationEditor textAnnotationEditor;
	private ShapeAnnotationEditor shapeAnnotationEditor;
	
	public BoundedTextAnnotationEditor(
			AnnotationFactory<BoundedTextAnnotation> factory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	public void setAnnotation(BoundedTextAnnotation annotation) {
		if (!Objects.equals(this.annotation, annotation)) {
			this.annotation = annotation;
			
			if (annotation instanceof TextAnnotation)
				getTextAnnotationEditor().setAnnotation((TextAnnotation) annotation);
			
			if (annotation instanceof ShapeAnnotation)
				getShapeAnnotationEditor().setAnnotation(annotation);
		}
	}
	
	@Override
	protected void update() {
		// Do not call doUpdate() on the actual editors, because they have to set their 'adjusting' flags themselves!
		getTextAnnotationEditor().update();
		getShapeAnnotationEditor().update();
	}
	
	@Override
	protected void doUpdate() {
		// Nothing to do here...
	}

	@Override
	protected void apply() {
		// Its Ok to post two UNDO edits, only one of them will actually make a change.
		getTextAnnotationEditor().apply();
		getShapeAnnotationEditor().apply();
	}
	
	@Override
	public void apply(BoundedTextAnnotation annotation) {
		// TODO does this even get called?
		if (annotation instanceof TextAnnotation)
			getTextAnnotationEditor().apply((TextAnnotation) annotation);
		
		getShapeAnnotationEditor().apply(annotation);
	}
	
	@Override
	protected void init() {
		var sep = new JSeparator();
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addGap(0, 20, Short.MAX_VALUE)
				.addComponent(getTextAnnotationEditor(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getShapeAnnotationEditor(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTextAnnotationEditor(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getShapeAnnotationEditor(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	protected TextAnnotationEditor getTextAnnotationEditor() {
		if (textAnnotationEditor == null) {
			textAnnotationEditor = new TextAnnotationEditor(null, false, serviceRegistrar);
			textAnnotationEditor.init();
		}
		
		return textAnnotationEditor;
	}
	
	protected ShapeAnnotationEditor getShapeAnnotationEditor() {
		if (shapeAnnotationEditor == null) {
			shapeAnnotationEditor = new ShapeAnnotationEditor(null, serviceRegistrar);
			shapeAnnotationEditor.init();
		}
		
		return shapeAnnotationEditor;
	}
}
