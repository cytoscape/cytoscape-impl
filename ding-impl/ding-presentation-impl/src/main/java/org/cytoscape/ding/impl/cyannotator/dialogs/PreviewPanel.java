package org.cytoscape.ding.impl.cyannotator.dialogs;

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


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.TitledBorder;

import java.awt.Component;

import org.cytoscape.ding.impl.cyannotator.api.Annotation;

public class PreviewPanel extends javax.swing.JPanel {
	Annotation mAnnotation;

	public PreviewPanel(Annotation annotation, int width, int height) {
		this.mAnnotation=annotation;
		JComponent c = mAnnotation.getComponent();
		mAnnotation.setUsedForPreviews(true);
		
		// System.out.println("Panel size = "+width+"x"+height);
		// System.out.println("Annotation size = "+c.getWidth()+"x"+c.getHeight());
		c.setLocation((width-c.getWidth())/2+5,(height-c.getHeight())/2+5);
		c.setOpaque(false);

		// Get the background paint for this view
		Paint backgroundPaint = mAnnotation.getCyAnnotator().getView().getBackgroundPaint();
		setBackground((Color)backgroundPaint); // Set our background to match
		c.setBackground(new Color(255,255,255,0)); // Make our background transparent

		// Border it
		TitledBorder title = BorderFactory.createTitledBorder("Preview");
		setBorder(title);
		setLayout(null);

		// Create the preview panel
		add(c);
		setBounds(10,10,width,height);
		repaint();
	}

	public Annotation getPreviewAnnotation() {
		return mAnnotation;
	}
}
