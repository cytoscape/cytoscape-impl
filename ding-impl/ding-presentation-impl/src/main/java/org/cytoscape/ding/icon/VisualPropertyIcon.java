package org.cytoscape.ding.icon;

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

import javax.swing.ImageIcon;

/**
 * 
 * Icon created from Shape object passed from rendering engine.<br>
 * 
 * This icon is scalable (vector image).
 * 
 * Actual paint method is defined in child classes.
 * 
 * This is an immutable object.
 * 
 */
public abstract class VisualPropertyIcon<T> extends ImageIcon {

	private static final long serialVersionUID = 3318566366920258692L;

	// Default Icon color
	public static final Color DEFAULT_ICON_COLOR = Color.DARK_GRAY;

	// Default icon size
	public static final int DEFAULT_ICON_SIZE = 32;

	final protected int height;
	final protected int width;

	final protected Color color;
	final protected T value;
	final protected String name;

	protected int leftPad = 20;
	protected int bottomPad = 0;

	/**
	 * Constructor with full parameter set.
	 * 
	 * @param shape
	 * @param width
	 * @param height
	 * @param name
	 * @param color
	 */
	public VisualPropertyIcon(final T value, final int width,
			final int height, final String name) {

		// Validate parameters
		if (value == null)
			throw new NullPointerException("Value parameter is null.");
		if (name == null)
			throw new NullPointerException("Name parameter is null.");
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException(
					"Width and height should be positive integers: (w, h) = ("
							+ width + ", " + height + ")");

		this.value = value;
		this.width = width;
		this.height = height;
		this.name = name;

		this.color = DEFAULT_ICON_COLOR;
	}

	
	public T getValue() {
		return value;
	}

	/**
	 * Get height of icon. This implements Icon interface.
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/**
	 * Get width of icon. This implements Icon interface.
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/**
	 * Get human-readable name of this icon.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get color of icon
	 * 
	 * @return Icon color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Insert space on the left.
	 * 
	 * @param pad
	 *            DOCUMENT ME!
	 */
	public void setLeftPadding(int pad) {
		this.leftPad = pad;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param pad
	 *            DOCUMENT ME!
	 */
	public void setBottomPadding(int pad) {
		this.bottomPad = pad;
	}
}
