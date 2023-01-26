package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Shape;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
    name = c.getName() != null ? c.getName() : getDefaultName();

    if (shapeType == ShapeType.CUSTOM)
      shape = GraphicsUtilities.copyCustomShape(c.getShape(), width, height);
    else
      shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
  }

  public ShapeAnnotationImpl(
      DRenderingEngine re,
      double x,
      double y,
      double rotation,
      ShapeType shapeType,
      double width,
      double height,
      Paint fillColor,
      Paint edgeColor,
      float edgeThickness
  ) {
    super(re, x, y, rotation);

    this.shapeType = shapeType;
    this.fillColor = fillColor;
    this.borderColor = edgeColor;
    this.borderWidth = edgeThickness;
    this.width = width;
    this.height = height;
    this.shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
  }

  public ShapeAnnotationImpl(DRenderingEngine re, Map<String, String> argMap) {
    super(re, argMap);

    double zoom = getLegacyZoom(argMap);

    // If this is an old bounded text, we might not (yet) have a width or height
    width = ViewUtils.getDouble(argMap, ShapeAnnotation.WIDTH, 100.0) / zoom;
    height = ViewUtils.getDouble(argMap, ShapeAnnotation.HEIGHT, 100.0) / zoom;

    fillColor = ViewUtils.getColor(argMap, FILLCOLOR, null);
    fillOpacity = ViewUtils.getDouble(argMap, FILLOPACITY, 100.0);
    
    borderWidth = ViewUtils.getDouble(argMap, EDGETHICKNESS, 1.0) / zoom;
    borderColor = ViewUtils.getColor(argMap, EDGECOLOR, Color.BLACK);
    borderOpacity = ViewUtils.getDouble(argMap, EDGEOPACITY, 100.0);

    shapeType = GraphicsUtilities.getShapeType(argMap, SHAPETYPE, ShapeType.RECTANGLE);

    if (shapeType != ShapeType.CUSTOM)
      shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
    else if (argMap.containsKey(CUSTOMSHAPE))
      shape = GraphicsUtilities.deserializeShape(argMap.get(CUSTOMSHAPE));
  }

  @Override
  public Class<? extends Annotation> getType() {
    return ShapeAnnotation.class;
  }
  
  @Override
  public Map<String, String> getArgMap() {
    var argMap = super.getArgMap();
    argMap.put(TYPE, ShapeAnnotation.class.getName());

    if (fillColor != null)
      argMap.put(FILLCOLOR, ViewUtils.serialize(fillColor));

    argMap.put(FILLOPACITY, Double.toString(fillOpacity));

    if (borderColor != null)
      argMap.put(EDGECOLOR, ViewUtils.serialize(borderColor));

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
  
  /**
   * Width and height are not applied, only colors, shape, etc.
   */
  @Override
  public void setStyle(Map<String, String> argMap) {
    if (argMap != null) {
      double zoom = getLegacyZoom(argMap);

      setFillColor(ViewUtils.getColor(argMap, FILLCOLOR, null));
      setFillOpacity(ViewUtils.getDouble(argMap, FILLOPACITY, 100.0));
      
      setBorderWidth(ViewUtils.getDouble(argMap, EDGETHICKNESS, 1.0) / zoom);
      setBorderColor(ViewUtils.getColor(argMap, EDGECOLOR, Color.BLACK));
      setBorderOpacity(ViewUtils.getDouble(argMap, EDGEOPACITY, 100.0));

      setShapeType(GraphicsUtilities.getShapeType(argMap, SHAPETYPE, ShapeType.RECTANGLE));
    }
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

  public ShapeType getShapeTypeEnum() {
    return shapeType;
  }

  public void setShapeType(ShapeType type) {
    if (shapeType != type) {
      var oldValue = shapeType;
      shapeType = type;
  
      if (shapeType != ShapeType.CUSTOM)
        shape = GraphicsUtilities.getShape(shapeType.shapeName(), 0.0, 0.0, width, height);
  
      update();
      firePropertyChange("shapeType", oldValue, type);
    }
  }

  @Override
  public void setShapeType(String type) {
    var shapeType = getShapeFromString(type);

    if (!Objects.equals(this.shapeType, shapeType))
      setShapeType(shapeType);
  }

  @Override
  public double getBorderWidth() {
    return borderWidth;
  }

  @Override
  public void setBorderWidth(double width) {
    if (borderWidth != width) {
      var oldValue = borderWidth;
      borderWidth = width;
      update();
      firePropertyChange("borderWidth", oldValue, width);
    }
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
  public void setBorderColor(Paint color) {
    if (!Objects.equals(borderColor, color)) {
      var oldValue = borderColor;
      borderColor = color;
      update();
      firePropertyChange("borderColor", oldValue, color);
    }
  }

  @Override
  public void setBorderOpacity(double opacity) {
    if (borderOpacity != opacity) {
      var oldValue = borderOpacity;
      borderOpacity = opacity;
      update();
      firePropertyChange("borderOpacity", oldValue, opacity);
    }
  }

  @Override
  public void setFillColor(Paint color) {
    if (!Objects.equals(fillColor, color)) {
      var oldValue = fillColor;
      fillColor = color;
      update();
      firePropertyChange("fillColor", oldValue, color);
    }
  }

  @Override
  public void setFillOpacity(double opacity) {
    if (fillOpacity != opacity) {
      var oldValue = fillOpacity;
      fillOpacity = opacity;
      update();
      firePropertyChange("fillOpacity", oldValue, opacity);
    }
  }
  
  @Override
  public void setCustomShape(String stringShape) {
      Shape shape = GraphicsUtilities.deserializeShape(stringShape);
      setCustomShape(shape);
  }

  @Override
  public void setCustomShape(Shape shape) {
    if (!Objects.equals(this.shape, shape)) {
      var oldValue = this.shape;
      this.shapeType = ShapeType.CUSTOM;
      this.shape = shape;
      update();
      firePropertyChange("shape", oldValue, shape);
    }
  }

  @Override
  public void paint(Graphics g, boolean showSelection) {
	  super.paint(g, showSelection);
	  
	  if (borderOpacity == 0 && fillOpacity == 0) // not here as an optimization, avoids invisible artifacts when exporting PDF
		  return;

      GraphicsUtilities.drawShape(g, getX(), getY(), getWidth(), getHeight(), getRotation(), this, false);
  }

  private ShapeType getShapeFromString(String type) {
	// First look up by the enum name()...
    for (var st : ShapeType.values()) {
      if (st.name().equals(type))
        return st;
    }
    // Shape not found? Let's try the shapeName...
    for (var st : ShapeType.values()) {
    	if (st.shapeName().equalsIgnoreCase(type))
    		return st;
    }
    
    return ShapeType.RECTANGLE;
  }
}
