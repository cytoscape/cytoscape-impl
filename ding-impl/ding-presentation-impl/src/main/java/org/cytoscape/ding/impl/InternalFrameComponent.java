package org.cytoscape.ding.impl;

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


// imports

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;


/**
 * This class manages the JLayeredPane that resides in
 * each internal frame of cytoscape.  Its intended to be the
 * class which encapsulates the multiple canvases that are created
 * by the DGraphView class.
 */
@SuppressWarnings("serial")
public class InternalFrameComponent extends JComponent implements Printable {

	private final DRenderingEngine re;

	private final DingCanvas backgroundCanvas;
	private final DingCanvas networkCanvas;
	private final DingCanvas foregroundCanvas;


	public InternalFrameComponent(JLayeredPane layeredPane, DRenderingEngine re) {
		this.backgroundCanvas = re.getCanvas(DRenderingEngine.Canvas.BACKGROUND_CANVAS);
		this.networkCanvas = re.getCanvas(DRenderingEngine.Canvas.NETWORK_CANVAS);
		this.foregroundCanvas = re.getCanvas(DRenderingEngine.Canvas.FOREGROUND_CANVAS);
		this.re = re;

		// Must pass Integer object too call correct add() overload
		layeredPane.add(backgroundCanvas, Integer.valueOf(10));
		layeredPane.add(networkCanvas,    Integer.valueOf(20));
		layeredPane.add(foregroundCanvas, Integer.valueOf(30));
	}

	/**
	 * Our implementation of Component setBounds().  If we don't do this, the
	 * individual canvas do not get rendered.
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		re.setBounds(x, y, width, height);
	}

	/**
	 * Our implementation of the Printable interface.
	 *
	 * @param graphics Graphics (context into which the page is drawn)
	 * @param pageFormat PageFormat (size and orientation of the page being drawn)
	 * @param pageIndex int (the zero based index of the page being drawn)
	 *
	 * @return PAGE_EXISTS if teh page is rendered or NO_SUCH_PAGE if pageIndex specifies non-existent page
	 * @throws PrinterException
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
	    throws PrinterException {
		if (pageIndex == 0) {
			((Graphics2D) graphics).translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			
			// Apply a scale factor to the image to make sure the whole image on the screen 
			// will fit to the printable area of the paper
			double image_scale = Math.min(pageFormat.getImageableWidth() / this.getWidth(),
			                              pageFormat.getImageableHeight() / this.getHeight());

			if (image_scale < 1.0d) {
				((Graphics2D) graphics).scale(image_scale, image_scale);
			}

			//TODO look at whether we should be clipping like this
			graphics.clipRect(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
			backgroundCanvas.print(graphics);
			networkCanvas.print(graphics);
			foregroundCanvas.print(graphics);

			return PAGE_EXISTS;
		} else
			return NO_SUCH_PAGE;
	}

	/**
	 * This method is used by freehep lib to export network as graphics.
	 */
	public void print(Graphics g) {
		backgroundCanvas.print(g);
		networkCanvas.print(g);
		foregroundCanvas.print(g);
	}
}
