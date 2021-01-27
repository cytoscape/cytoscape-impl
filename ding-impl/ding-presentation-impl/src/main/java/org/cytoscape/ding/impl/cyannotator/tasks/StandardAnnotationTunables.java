package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;

public class StandardAnnotationTunables extends AbstractAnnotationTunables {

  static final String[] CANVASES = {Annotation.FOREGROUND, Annotation.BACKGROUND};

  @Tunable(context="nogui", 
           description="The new name for this annotation, which will appear to users in the Annotations Panel")
  public String newName = null;

  @Tunable(context="nogui", 
           required=true,
           description="The X location for the annotation, in network view units")
  public Double x = null;

  @Tunable(context="nogui", 
           required=true,
           description="The Y location for the annotation, in network view units")
  public Double y = null;

  @Tunable(context="nogui", 
           description="The Z order for the annotation")
  public Double z = null;

  @Tunable(context="nogui", description="The angle (in degrees) of the annotation")
  public Double angle = null;

  @Tunable(context="nogui", description="The canvas layer this annotation should be on")
  public String canvas = null;

  public StandardAnnotationTunables () {
  }

  public Map<String, String> getArgMap(TaskMonitor tm) {
    var args = new HashMap<String, String>();
    putIfNotNull(tm, args, Annotation.X, x);
    putIfNotNull(tm, args, Annotation.Y, y);
    putIfNotNull(tm, args, Annotation.Z, z);
    putIfNotNull(tm, args, Annotation.NAME, newName);
    putIfNotNull(tm, args, Annotation.ROTATION, angle);
    putIfNotNull(tm, args, Annotation.CANVAS, canvas, Arrays.asList(CANVASES));
    return args;
  }

  public void update(TaskMonitor tm, Annotation annotation) {
    Map<String, String> argMap = annotation.getArgMap();
    if (x != null || y != null) {
      if (x == null)
        x = Double.valueOf(argMap.get(Annotation.X));
      if (y == null )
        y = Double.valueOf(argMap.get(Annotation.Y));

      annotation.moveAnnotation(new Point2D.Double(x, y));
    }

    // FIXME: No API for z??

    if (canvas != null)
      annotation.setCanvas(canvas);

    if (newName != null)
      annotation.setName(newName);

    if (angle != null)
      annotation.setRotation(angle);
  }
}
