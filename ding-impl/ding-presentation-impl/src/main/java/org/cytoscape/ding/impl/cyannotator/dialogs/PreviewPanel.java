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

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;

@SuppressWarnings("serial")
public class PreviewPanel extends JPanel {
	
	private final DingAnnotation annotation;

	public PreviewPanel(DingAnnotation annotation) {
		this.annotation = annotation;
		
		annotation.setUsedForPreviews(true);
		final Color background = (Color) annotation.getCyAnnotator().getView().getBackgroundPaint();
		
		final JComponent c = annotation.getComponent();
		c.setOpaque(false);
		c.setBackground(new Color(255, 255, 255, 0)); // Make the component background transparent

		final JLabel label = new JLabel("Preview:");
		
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		panel.setBackground(background); // Set our background to match
		
		{
			final GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(10, 20, Short.MAX_VALUE)
					.addComponent(c, c.getWidth(), c.getWidth(), PREFERRED_SIZE)
					.addGap(10, 20, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(10, 20, Short.MAX_VALUE)
					.addComponent(c, c.getHeight(), c.getHeight(), PREFERRED_SIZE)
					.addGap(10, 20, Short.MAX_VALUE)
			);
		}
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(label)
				.addComponent(panel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(panel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		add(c);
		repaint();
	}

	public DingAnnotation getAnnotation() {
		return annotation;
	}
}
