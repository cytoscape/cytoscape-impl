package org.cytoscape.cg.internal.util;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * 
 * Icon created from Shape object passed from rendering engine.<br>
 * 
 * This icon is scalable (vector image).
 * Actual paint method is defined in child classes.
 * This is an immutable object.
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

	protected int leftPad;
	protected int bottomPad;

	public VisualPropertyIcon(T value, int width, int height, String name) {
		// Validate parameters
		if (value == null)
			throw new IllegalArgumentException("'value' must not be null.");
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException(
					"Width and height should be positive integers: (w, h) = (" + width + ", " + height + ")");

		this.value = value;
		this.width = width;
		this.height = height;
		this.name = name;

		this.color = DEFAULT_ICON_COLOR;
	}

	public T getValue() {
		return value;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	@Override
	public int getIconWidth() {
		return width;
	}
	
	/**
	 * Get human-readable name of this icon. May be null.
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
	 */
	public void setLeftPadding(int pad) {
		this.leftPad = pad;
	}

	public void setBottomPadding(int pad) {
		this.bottomPad = pad;
	}
}
