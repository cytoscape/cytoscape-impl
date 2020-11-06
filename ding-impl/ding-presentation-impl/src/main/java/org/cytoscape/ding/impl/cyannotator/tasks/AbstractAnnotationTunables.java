package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.work.TaskMonitor;

public class AbstractAnnotationTunables {
  public void putIfNotNull(TaskMonitor tm, Map<String,String> map, String key, Object value) {
    if (value == null) return;
    map.put(key, value.toString());
  }

  public void putIfNotNull(TaskMonitor tm, Map<String,String> map, String key, Object value, List<String>matches) {
    if (value == null) return;
    for (var str: matches) {
      if (str.equalsIgnoreCase(value.toString())) {
        map.put(key, value.toString());
        return;
      }
    }
    if (tm == null) return;
    tm.setStatusMessage(value.toString()+" is not a valid option for "+key);
    tm.setStatusMessage("Valid options are: "+matches.toString());
  }

  public String getColor(String color) {
    if (color == null) return null;
    int clr = ColorUtil.parseColor(color).getRGB();
    return Integer.toString(clr);
  }
}
