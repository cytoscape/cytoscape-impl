package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.DGraphView.Canvas;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * This class is meant to be extended by a class which
 * is meant to exist within the InternalFrameComponent class.
 * It provides the services required to draw onto it.
 *
 * Currently (9/7/06), two classes will extend DingCanves, org.cytoscape.ding.impl.InnerCanvas
 * and org.cytoscape.ding.impl.ArbitraryGraphicsCanvas.
 */
public abstract class DingCanvas extends JComponent {
	
	private static final long serialVersionUID = -789701521899087090L;

	/**
	 * ref to image we maintain
	 */
	protected Image m_img;

	/**
	 * ref to our background color
	 */
	protected Color m_backgroundColor;

	protected final Canvas canvasId;

	
	public DingCanvas(Canvas canvasId) {
		this.canvasId = canvasId;
	}
	
	public Canvas getCanvasId() {
		return canvasId;
	}
	
	@Override
	public Color getBackground() {
		return m_backgroundColor;
	}

	@Override
	public void setBackground(Color backgroundColor) {
		m_backgroundColor = backgroundColor;
	}

	/**
	 * Returns the image maintained by the canvas.
	 *
	 * @return Image
	 */
	public Image getImage() {
		return m_img;
	}

	/**
	 * Method used to print canvas without using image imposter.
	 */
	public void printNoImposter(Graphics g) {
		print(g);
	}
}
