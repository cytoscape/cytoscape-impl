package org.cytoscape.ding.impl.cyannotator.tasks;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class ImageAnnotationTunables extends AbstractAnnotationTunables {
  @Tunable(context="nogui", 
           required=true,
           description="The url of the image to be displayed")
  public String url = null;

  @Tunable(context="nogui", 
           description="The image contrast adjustment")
  public Integer contrast = null;

  @Tunable(context="nogui", 
           description="The image brightness adjustment")
  public Integer brightness = null;

  @Tunable(context="nogui", 
           description="The image opacity adjustment (0-100)")
  public Double opacity = null;

  /*
   * FIXME: How do we handle SVG?
  @Tunable(context="nogui", 
           description="The SVG to show as part of this image")
  public String svg = null;
  */

  public ImageAnnotationTunables () {
  }

  public Map<String, String> getArgMap(TaskMonitor tm) {
    var args = new HashMap<String, String>();
    putIfNotNull(tm, args, ImageAnnotation.URL, url);
    putIfNotNull(tm, args, ImageAnnotation.CONTRAST, contrast);
    putIfNotNull(tm, args, ImageAnnotation.LIGHTNESS, brightness);
    if(opacity != null )
    	putIfNotNull(tm, args, ImageAnnotation.OPACITY, opacity.floatValue()/100.0f);

    return args;
  }

  public void update(TaskMonitor tm, Annotation annotation) {
    ImageAnnotation iAnnotation = (ImageAnnotation)annotation;
    if (url != null) {
      try {
        iAnnotation.setImage(new URL(url));
      } catch (Exception e) {
        tm.setStatusMessage(e.getMessage());
      }
    }
    if (contrast != null) iAnnotation.setImageContrast(contrast);
    if (brightness != null) iAnnotation.setImageBrightness(brightness);
    if (opacity != null) iAnnotation.setImageOpacity(opacity.floatValue()/100.0f);
  }
}
