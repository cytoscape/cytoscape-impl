package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Shape;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.dialogs.ShapeAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class ShapeAnnotationImpl extends AbstractAnnotation implements ShapeAnnotation {
	
	private ShapeType shapeType;
	private double borderWidth = 1.0;
	private Paint borderColor = Color.BLACK; // These are paint's so we can do gradients
	private Paint fillColor; // These are paint's so we can do gradients
	private double borderOpacity = 100.0;
	private double fillOpacity = 100.0;
	private Shape shape;
	protected double factor = 1.0;

	public ShapeAnnotationImpl(DRenderingEngine re, double width, double height, boolean usedForPreviews) {
		super(re, usedForPreviews);
		setSize(width, height);
		shapeType = ShapeType.RECTANGLE;
		borderWidth = 1.0;
	}

	public ShapeAnnotationImpl(ShapeAnnotationImpl c, double width, double height, boolean usedForPreviews) {
		super(c, usedForPreviews);
		setSize(width, height);
		this.width = width;
		this.height = height;
		shapeType = GraphicsUtilities.getShapeType(c.getShapeType());
		borderColor = c.getBorderColor();
		borderWidth = c.getBorderWidth();
		fillColor = c.getFillColor();
		shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
		name = c.getName() != null ? c.getName() : getDefaultName();
	}

	public ShapeAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
			ShapeType shapeType,
			double width,
			double height,
			Paint fillColor,
			Paint edgeColor,
			float edgeThickness
	) {
		super(re, x, y);

		this.shapeType = shapeType;
		this.fillColor = fillColor;
		this.borderColor = edgeColor;
		this.borderWidth = edgeThickness;
		this.width = width;
		this.height = height;
		this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
	}

	public ShapeAnnotationImpl(DRenderingEngine re, Map<String,String> argMap) {
		super(re, argMap);

		this.fillColor = ViewUtils.getColor(argMap, FILLCOLOR, null);
		this.fillOpacity = ViewUtils.getDouble(argMap, FILLOPACITY, 100.0);
		
		double zoom = getLegacyZoom(argMap);

		// If this is an old bounded text, we might not (yet) have a width or height
		this.width  = ViewUtils.getDouble(argMap, ShapeAnnotation.WIDTH,  100.0) / zoom;
		this.height = ViewUtils.getDouble(argMap, ShapeAnnotation.HEIGHT, 100.0) / zoom;
		
		this.borderWidth = ViewUtils.getDouble(argMap, EDGETHICKNESS, 1.0) / zoom;
		
		this.borderColor = ViewUtils.getColor(argMap, EDGECOLOR, Color.BLACK);
		this.borderOpacity = ViewUtils.getDouble(argMap, EDGEOPACITY, 100.0);

		this.shapeType = GraphicsUtilities.getShapeType(argMap, SHAPETYPE, ShapeType.RECTANGLE);

		if (this.shapeType != ShapeType.CUSTOM)
			this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
		else if (argMap.containsKey(CUSTOMSHAPE))
			this.shape = GraphicsUtilities.deserializeShape(argMap.get(CUSTOMSHAPE));
	}

	@Override
	public Class<? extends Annotation> getType() {
		return ShapeAnnotation.class;
	}
	
	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, ShapeAnnotation.class.getName());

		if (fillColor != null)
			argMap.put(FILLCOLOR, ViewUtils.convertColor(fillColor));

		argMap.put(FILLOPACITY, Double.toString(fillOpacity));

		if (borderColor != null)
			argMap.put(EDGECOLOR, ViewUtils.convertColor(borderColor));

		argMap.put(EDGETHICKNESS, Double.toString(borderWidth));
		argMap.put(EDGEOPACITY, Double.toString(borderOpacity));
		
		if (shapeType != null) {
			argMap.put(SHAPETYPE, shapeType.name());
			if (shapeType.equals(ShapeType.CUSTOM) && shape != null)
				argMap.put(CUSTOMSHAPE, GraphicsUtilities.serializeShape(shape));
		}
		argMap.put(ShapeAnnotation.WIDTH,  Double.toString(width));
		argMap.put(ShapeAnnotation.HEIGHT, Double.toString(height));

		return argMap;
	}

	@Override
	public List<String> getSupportedShapes() {
		return GraphicsUtilities.getSupportedShapes();
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
			this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
		update();
	}

	@Override
	public void setShapeType(String type) {
		shapeType = getShapeFromString(type);
		setShapeType(shapeType);
	}

	@Override
	public double getBorderWidth() {
		return borderWidth;
	}

	@Override
	public void setBorderWidth(double width) {
		borderWidth = width;
		update();
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
		update();
	}

	@Override
	public void setBorderOpacity(double opacity) {
		borderOpacity = opacity;
		update();
	}

	@Override
	public void setFillColor(Paint fill) {
		fillColor = fill;
		update();
	}

	@Override
	public void setFillOpacity(double opacity) {
		fillOpacity = opacity;
		update();
	}

	@Override
	public void paint(Graphics g, boolean showSelection) {
		super.paint(g, showSelection);

		// MKTODO
//		if (canvas.isPrinting())
//			GraphicsUtilities.drawShape(g, getX(), getY(), getWidth() - 1, getHeight() - 1, this, true);
//		else
			GraphicsUtilities.drawShape(g, getX(), getY(), getWidth() - 1, getHeight() - 1, this, false);
	}

	@Override
	public void setCustomShape(Shape shape) {
		this.shapeType = ShapeType.CUSTOM;
		this.shape = shape;
		update();
	}

	@Override
	public JDialog getModifyDialog() {
		return new ShapeAnnotationDialog(this, ViewUtil.getActiveWindow(re));
	}

	private ShapeType getShapeFromString(String shapeName) {
		for (ShapeType type : ShapeType.values()) {
			if (type.shapeName().equals(shapeName))
				return type;
		}
		
		return ShapeType.RECTANGLE;
	}
}
