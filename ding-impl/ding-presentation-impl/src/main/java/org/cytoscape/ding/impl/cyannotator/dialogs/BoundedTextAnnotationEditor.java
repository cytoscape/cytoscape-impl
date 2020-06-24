package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import javax.swing.GroupLayout;

import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
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
public class BoundedTextAnnotationEditor extends AbstractAnnotationEditor<BoundedTextAnnotation> {
	
	private TextAnnotationEditor textAnnotationPanel;
	private ShapeAnnotationEditor shapeAnnotationPanel;
	
	public BoundedTextAnnotationEditor(
			AnnotationFactory<BoundedTextAnnotation> factory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	public boolean accepts(Annotation annotation) {
		return annotation instanceof BoundedTextAnnotationImpl;
	}
	
	@Override
	public void setAnnotation(Annotation annotation) {
		super.setAnnotation(annotation);
		
		if (annotation instanceof TextAnnotation)
			getTextAnnotationPanel().setAnnotation(annotation);
		
		if (annotation instanceof ShapeAnnotation)
			getShapeAnnotationPanel().setAnnotation(annotation);
	}
	
	@Override
	protected void update() {
		getTextAnnotationPanel().update();
		getShapeAnnotationPanel().update();
	}

	@Override
	protected void apply() {
		if (!adjusting) {
			getTextAnnotationPanel().apply();
			getShapeAnnotationPanel().apply();
		}
	}
	
	@Override
	protected void init() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(getTextAnnotationPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getShapeAnnotationPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTextAnnotationPanel())
				.addComponent(getShapeAnnotationPanel())
		);
	}
	
	protected TextAnnotationEditor getTextAnnotationPanel() {
		if (textAnnotationPanel == null) {
			textAnnotationPanel = new TextAnnotationEditor(null, serviceRegistrar);
			textAnnotationPanel.init();
		}
		
		return textAnnotationPanel;
	}
	
	protected ShapeAnnotationEditor getShapeAnnotationPanel() {
		if (shapeAnnotationPanel == null) {
			shapeAnnotationPanel = new ShapeAnnotationEditor(null, serviceRegistrar);
			textAnnotationPanel.init();
		}
		
		return shapeAnnotationPanel;
	}
}
