package org.cytoscape.welcome.internal;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BackgroundImagePanel extends JPanel {
	
	private static final long serialVersionUID = 3969531543044198032L;
	
	private final BufferedImage image;
	
	public BackgroundImagePanel(final BufferedImage image) {
		this.image = image;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;

		final int panelWidth = this.getWidth();
		final int panelHeight = this.getHeight();
		
		// Centering image
		final int imageW = image.getWidth();
		final int imageH = image.getHeight();
		
		int x = panelWidth/2 - imageW/2;
		int y = panelHeight/2 - imageH/2;
		
		g2D.setColor(getBackground());
		g2D.fillRect(0, 0, panelWidth, panelHeight);

		g2D.drawImage(image, null, x, y);
	}

}
