package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
public class BoundedTextAnnotationPanel extends JPanel {
	
	private final TextAnnotationPanel textAnnotationPanel;
	private final ShapeAnnotationPanel shapeAnnotationPanel;
	
	public BoundedTextAnnotationPanel(BoundedTextAnnotationImpl annotation, PreviewPanel previewPanel) {
		textAnnotationPanel = new TextAnnotationPanel(annotation, previewPanel);
		shapeAnnotationPanel = new ShapeAnnotationPanel(annotation, previewPanel);
		
		initComponents();
	}
	
	private void initComponents() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(textAnnotationPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(shapeAnnotationPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(textAnnotationPanel)
				.addComponent(shapeAnnotationPanel)
		);
	}
}
