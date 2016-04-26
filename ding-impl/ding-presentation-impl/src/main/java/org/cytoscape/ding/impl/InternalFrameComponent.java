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
public class InternalFrameComponent extends JComponent implements Printable {
	private final static long serialVersionUID = 1213747102972998L;
	
	/**
	 * z-order enumeration
	 * 
	 * TODO: this breaks resize-handle on Mac OS X.
	 * 	Need to do some research on this layring order.
	 * 
	 */
	private enum ZOrder {
		BACKGROUND_PANE,
		NETWORK_PANE,
		FOREGROUND_PANE;
		int layer() {
			if (this == BACKGROUND_PANE)
				return 10;

			if (this == NETWORK_PANE)
				return 20;

			if (this == FOREGROUND_PANE)
				return 30;

			return 0;
		}
	}

	/**
	 * ref to the JInternalFrame's JLayeredPane
	 */
	private JLayeredPane layeredPane;

	/**
	 * ref to background canvas
	 */
	private DingCanvas backgroundCanvas;

	/**
	 * ref to network canvas
	 */
	private DingCanvas networkCanvas;

	/**
	 * ref to foreground canvas
	 */
	private DingCanvas foregroundCanvas;

	/**
	 * ref to the graph view
	 */
	private DGraphView dGraphView;

	/**
	 * Constructor.
	 *
	 * @param layeredPane JLayedPane
	 * @param dGraphView dGraphView
	 */
	public InternalFrameComponent(JLayeredPane layeredPane, DGraphView dGraphView) {
		// init members
		this.layeredPane = layeredPane;
		this.backgroundCanvas = dGraphView.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS);
		this.networkCanvas = dGraphView.getCanvas(DGraphView.Canvas.NETWORK_CANVAS);
		this.foregroundCanvas = dGraphView.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS);
		this.dGraphView = dGraphView;

		// set default ordering
		initLayeredPane();
	}

	/**
	 * Our implementation of Component setBounds().  If we don't do this, the
	 * individual canvas do not get rendered.
	 *
	 * @param x int
	 * @param y int
	 * @param width int
	 * @param height int
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		dGraphView.setBounds(x, y, width, height);
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
		
		// This is a work-around, otherwise we lose backgroundCanvas color
		// networkCanvas.setBackground(backgroundCanvas.getBackground());
		
		networkCanvas.print(g);
		foregroundCanvas.print(g);
	}

	/**
	 * Places the canvas on the layeredPane in the following manner:
	 * top - bottom: foreground, network, background
	 */
	private void initLayeredPane() {
		// foreground followed by network followed by background
		layeredPane.add(backgroundCanvas, Integer.valueOf(ZOrder.BACKGROUND_PANE.layer()));
		layeredPane.add(networkCanvas, Integer.valueOf(ZOrder.NETWORK_PANE.layer()));
		layeredPane.add(foregroundCanvas, Integer.valueOf(ZOrder.FOREGROUND_PANE.layer()));
	}
}
