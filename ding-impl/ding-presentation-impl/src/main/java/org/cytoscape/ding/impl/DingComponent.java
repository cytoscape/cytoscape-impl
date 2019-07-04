package org.cytoscape.ding.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class DingComponent {
	
	private int x;
	private int y;
	private int height;
	private int width;
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		setLocation(x, y);
		setSize(width, height);
	}
	
	public void setBounds(Rectangle r) {
		setBounds(r.x, r.y, r.width, r.height);
	}
	
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}
	
	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
	}
	
	public void setSize(int width, int height) {
		setWidth(width);
		setHeight(height);
	}
	
	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}

	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}
	
	public boolean contains(int x, int y) {
		return getBounds().contains(x, y);
	}
	
	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
	}

	public Point getLocation() {
		return new Point(getX(), getY());
	}
}