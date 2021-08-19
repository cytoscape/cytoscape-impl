package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class ShapeAnnotationTunables extends AbstractAnnotationTunables {

  @Tunable(context="nogui", 
           description="The height of this shape")
  public Double height = null;

  @Tunable(context="nogui", 
           description="The width of this shape")
  public Double width = null;

  @Tunable(context="nogui", 
           description="The fill color of this shape as an RGB hex string (not used for images)")
  public String fillColor = null;

  @Tunable(context="nogui", 
           description="The opacity of this shape")
  public Double opacity = null;

  @Tunable(context="nogui", 
           description="The border color of this shape as an RGB hex string")
  public String borderColor = null;

  @Tunable(context="nogui", 
           description="The opacity of the shape border")
  public Double borderOpacity = null;

  @Tunable(context="nogui", 
           description="The shape border thickness")
  public Double borderThickness = null;

  @Tunable(context="nogui", description="The type of the shape")
  public String type = null;

  @Tunable(context="nogui", description="If a custom shape, this is the text of the shape")
  public String customShape = null;

  public ShapeAnnotationTunables () {
  }

  public Map<String, String> getArgMap(TaskMonitor tm) {
    var args = new HashMap<String, String>();
    putIfNotNull(tm, args, ShapeAnnotation.HEIGHT, height);
    putIfNotNull(tm, args, ShapeAnnotation.WIDTH, width);
    putIfNotNull(tm, args, ShapeAnnotation.FILLCOLOR, getColor(fillColor));
    putIfNotNull(tm, args, ShapeAnnotation.EDGECOLOR, getColor(borderColor));

    putIfNotNull(tm, args, ShapeAnnotation.FILLOPACITY, opacity);
    putIfNotNull(tm, args, ShapeAnnotation.EDGEOPACITY, borderOpacity);
    putIfNotNull(tm, args, ShapeAnnotation.EDGETHICKNESS, borderThickness);

    if (type == null)
      type = "rectangle";

    putIfNotNull(tm, args, ShapeAnnotation.SHAPETYPE, type, getShapeTypes());
    // Handle custom shapes
    if (type.equalsIgnoreCase(ShapeAnnotation.ShapeType.CUSTOM.toString()))
      putIfNotNull(tm, args, ShapeAnnotation.CUSTOMSHAPE, customShape);

    return args;
  }

  public void update(TaskMonitor tm, Annotation ann) {
    Map<String, String> argMap = ann.getArgMap();
    ShapeAnnotation sAnn = (ShapeAnnotation)ann;

    if (width != null || height != null) {
      if (width == null)
        width = Double.valueOf(argMap.get(ShapeAnnotation.WIDTH));
      if (height == null )
        height = Double.valueOf(argMap.get(ShapeAnnotation.HEIGHT));

      sAnn.setSize(width, height);
    }

    if (type != null) { sAnn.setShapeType(type); }
    if (fillColor != null) { sAnn.setFillColor(ColorUtil.parseColor(fillColor)); }
    if (borderColor != null) { sAnn.setBorderColor(ColorUtil.parseColor(borderColor)); }
    if (opacity != null) { sAnn.setFillOpacity(opacity); }
    if (borderOpacity != null) { sAnn.setBorderOpacity(borderOpacity); }
    if (borderThickness != null) { sAnn.setBorderWidth(borderThickness); }
    /*
     * Need an API to set the custom shape as a string
     */
    if (sAnn.getShapeType().equalsIgnoreCase("custom"))
      if (customShape != null) { sAnn.setCustomShape(customShape); }
  }

  public List<String> getShapeTypes() {
    var types = new ArrayList<String>();
    for (var type: ShapeAnnotation.ShapeType.values()) {
      types.add(type.toString());
    }
    return types;
  }
}
