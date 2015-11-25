package org.cytoscape.ding.impl.cyannotator.annotations;

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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
// import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.dialogs.ShapeAnnotationDialog;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

@SuppressWarnings("serial")
public class ShapeAnnotationImpl extends AbstractAnnotation implements ShapeAnnotation {
	
	private ShapeType shapeType;
	private double borderWidth = 1.0;
	private Paint borderColor = Color.BLACK; // These are paint's so we can do gradients
	private Paint fillColor = null; // These are paint's so we can do gradients
	private double borderOpacity = 100.0;
	private double fillOpacity = 100.0;
	private	Shape shape = null;
	protected double shapeWidth = 0.0;
	protected double shapeHeight = 0.0;
	protected double factor = 1.0;

	public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, double width, double height) {
		super(cyAnnotator, view);
		shapeWidth = width;
		shapeHeight = height;
		shapeType = ShapeType.RECTANGLE;
		borderWidth = 1.0;
	}

	public ShapeAnnotationImpl(ShapeAnnotationImpl c, double width, double height) {
		super(c);
		shapeWidth = width;
		shapeHeight = height;
		shapeType = GraphicsUtilities.getShapeType(c.getShapeType());
		borderColor = c.getBorderColor();
		borderWidth = c.getBorderWidth();
		fillColor = c.getFillColor();
		shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, shapeWidth, shapeHeight);
	}

	public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, double x, double y, ShapeType shapeType,
			double width, double height, Paint fillColor, Paint edgeColor, float edgeThickness) {
		super(cyAnnotator, view, x, y, view.getZoom());

		this.shapeType = shapeType;
		this.fillColor = fillColor;
		setFillColor(fillColor);
		this.borderColor = edgeColor;
		this.borderWidth = edgeThickness;
		this.shapeWidth = width;
		this.shapeHeight = height;
		this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, shapeWidth, shapeHeight);

		setSize((int) (shapeWidth + borderWidth * 2 * getZoom()), (int) (shapeHeight + borderWidth * 2 * getZoom()));
	}

	public ShapeAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		super(cyAnnotator, view, argMap);

		this.fillColor = getColor(argMap, FILLCOLOR, null);
		setFillColor(fillColor);
		this.fillOpacity = getDouble(argMap, FILLOPACITY, 100.0);

		// If this is an old bounded text, we might not (yet) have a width or
		// height
		this.shapeWidth = getDouble(argMap, ShapeAnnotation.WIDTH, 100.0);
		this.shapeHeight = getDouble(argMap, ShapeAnnotation.HEIGHT, 100.0);

		this.borderWidth = getDouble(argMap, EDGETHICKNESS, 1.0);
		this.borderColor = getColor(argMap, EDGECOLOR, Color.BLACK);
		this.borderOpacity = getDouble(argMap, EDGEOPACITY, 100.0);

		this.shapeType = GraphicsUtilities.getShapeType(argMap, SHAPETYPE, ShapeType.RECTANGLE);

		if (this.shapeType != ShapeType.CUSTOM)
			this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, shapeWidth, shapeHeight);
		else if (argMap.containsKey(CUSTOMSHAPE))
			this.shape = GraphicsUtilities.deserializeShape(argMap.get(CUSTOMSHAPE));

		setSize((int) (shapeWidth + borderWidth * 2 * getZoom()), (int) (shapeHeight + borderWidth * 2 * getZoom()));
	}

	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, ShapeAnnotation.class.getName());

		if (this.fillColor != null)
			argMap.put(FILLCOLOR, convertColor(this.fillColor));

		argMap.put(FILLOPACITY, Double.toString(this.fillOpacity));

		if (this.borderColor != null)
			argMap.put(EDGECOLOR, convertColor(this.borderColor));

		argMap.put(EDGETHICKNESS, Double.toString(this.borderWidth));
		argMap.put(EDGEOPACITY, Double.toString(this.borderOpacity));
		argMap.put(SHAPETYPE, this.shapeType.name());
		argMap.put(ShapeAnnotation.WIDTH, Double.toString(this.shapeWidth));
		argMap.put(ShapeAnnotation.HEIGHT, Double.toString(this.shapeHeight));

		if (shapeType.equals(ShapeType.CUSTOM))
			argMap.put(CUSTOMSHAPE, GraphicsUtilities.serializeShape(shape));

		return argMap;
	}

	@Override
	public List<String> getSupportedShapes() {
		return GraphicsUtilities.getSupportedShapes();
	}

	@Override
	public void setZoom(double zoom) {
		float factor = (float) (zoom / getZoom());

		shapeWidth *= factor;
		shapeHeight *= factor;

		setSize((int) (shapeWidth + borderWidth * 2 * getZoom()), (int) (shapeHeight + borderWidth * 2 * getZoom()));
		super.setZoom(zoom);
	}

	@Override
	public void setSpecificZoom(double zoom) {
		float factor = (float) (zoom / getSpecificZoom());

		shapeWidth *= factor;
		shapeHeight *= factor;

		setSize((int) (shapeWidth + borderWidth * 2 * getZoom()), (int) (shapeHeight + borderWidth * 2 * getZoom()));
		super.setSpecificZoom(zoom);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}

	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	@Override
	public String getShapeType() {
		return shapeType.shapeName();
	}

	public ShapeType getShapeTypeInt() {
		return shapeType;
	}

	public void setShapeType(ShapeType type) {
		shapeType = type;
		
		if (shapeType != ShapeType.CUSTOM)
			this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, shapeWidth, shapeHeight);
	}

	@Override
	public void setShapeType(String type) {
		shapeType = getShapeFromString(type);
		
		if (shapeType != ShapeType.CUSTOM)
			this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, shapeWidth, shapeHeight);
	}

	@Override
	public double getBorderWidth() {
		return borderWidth;
	}

	@Override
	public void setBorderWidth(double width) {
		borderWidth = width;
	}

	@Override
	public Paint getBorderColor() {
		return borderColor;
	}

	@Override
	public double getBorderOpacity() {
		return borderOpacity;
	}

	@Override
	public Paint getFillColor() {
		return fillColor;
	}

	@Override
	public double getFillOpacity() {
		return fillOpacity;
	}

	@Override
	public void setBorderColor(Paint border) {
		borderColor = border;
	}

	@Override
	public void setBorderOpacity(double opacity) {
		borderOpacity = opacity;
	}

	@Override
	public void setFillColor(Paint fill) {
		fillColor = fill;
	}

	@Override
	public void setFillOpacity(double opacity) {
		fillOpacity = opacity;
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);

		int width = (int) (shapeWidth * scaleFactor / getZoom());
		int height = (int) (shapeHeight * scaleFactor / getZoom());


		double savedBorder = borderWidth;
		boolean selected = isSelected();
		borderWidth = borderWidth * scaleFactor / getZoom();
		setSelected(false);
		GraphicsUtilities.drawShape(g, (int) (x * scaleFactor), (int) (y * scaleFactor), width, height, this, false);
		setSelected(selected);
		borderWidth = savedBorder;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (canvas.isPrinting())
			GraphicsUtilities.drawShape(g, 0, 0, getWidth() - 1, getHeight() - 1, this, true);
		else
			GraphicsUtilities.drawShape(g, 0, 0, getWidth() - 1, getHeight() - 1, this, false);
	}

	@Override
	public void setSize(double width, double height) {
		shapeWidth = width;
		shapeHeight = height;
		setSize((int) (shapeWidth + borderWidth * 2 * getZoom()), (int) (shapeHeight + borderWidth * 2 * getZoom()));
	}

	@Override
	public void setSize(Dimension d) {
		setSize(d.getWidth(), d.getHeight());
	}

	@Override
	public void setCustomShape(Shape shape) {
		this.shapeType = ShapeType.CUSTOM;
		this.shape = shape;
	}

	@Override
	public Dimension adjustAspectRatio(Dimension d) {
		double ratio = d.getWidth() / d.getHeight();
		double aspectRatio = shapeWidth / shapeHeight;
		double width, height;
		
		if (aspectRatio >= ratio) {
			width = d.getWidth();
			height = width / aspectRatio;
		} else {
			height = d.getHeight();
			width = height * aspectRatio;
		}
		
		d.setSize(width, height);
		
		return d;
	}

	@Override
	public JDialog getModifyDialog() {
		return new ShapeAnnotationDialog(this);
	}

	private ShapeType getShapeFromString(String shapeName) {
		for (ShapeType type : ShapeType.values()) {
			if (type.shapeName().equals(shapeName))
				return type;
		}
		
		return ShapeType.RECTANGLE;
	}
}
