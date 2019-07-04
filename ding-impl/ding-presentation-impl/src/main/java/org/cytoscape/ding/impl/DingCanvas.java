package org.cytoscape.ding.impl;

import java.awt.Graphics;
import java.awt.Image;

import org.cytoscape.ding.impl.DRenderingEngine.Canvas;

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
public abstract class DingCanvas extends DingComponent {
	
	private final Canvas canvasId;
	
	public DingCanvas(Canvas canvasId) {
		this.canvasId = canvasId;
	}
	
	public Canvas getCanvasId() {
		return canvasId;
	}
	
	public abstract Image getImage();
	
	public abstract void paint(Graphics g);
	
	public abstract void print(Graphics g);
	
	public void printNoImposter(Graphics g) {
		print(g);
	}
	
}
