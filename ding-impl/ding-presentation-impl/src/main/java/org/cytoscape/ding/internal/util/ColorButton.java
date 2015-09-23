package org.cytoscape.ding.internal.util;

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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

@SuppressWarnings("serial")
public class ColorButton extends JButton {

	protected static final JColorChooser colorChooser = new JColorChooser();
	
	private Color color;
	private Color borderColor;

	public ColorButton(final Color color) {
		super(" ");
		putClientProperty("JButton.buttonType", "gradient"); // Aqua LAF only
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		borderColor = ColorUtil.getContrastingColor(getBackground());
		setIcon(new ColorIcon());
		setColor(color);
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open color chooser
				final JDialog dialog = JColorChooser.createDialog(
						ColorButton.this,
						"Colors",
						true,
						colorChooser, 
						new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								final Color c = colorChooser.getColor();
								ColorButton.this.setColor(c);
							}
						}, null);
				dialog.setVisible(true);
			}
		});
	}

	public void setColor(final Color color) {
		final Color oldColor = this.color;
		this.color = color;
		repaint();
		firePropertyChange("color", oldColor, color);
	}
	
	public Color getColor() {
		return color;
	}
	
	class ColorIcon implements Icon {

		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			return 44;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(borderColor);
			g.drawRect(x, y, w, h);
		}
	}
}
