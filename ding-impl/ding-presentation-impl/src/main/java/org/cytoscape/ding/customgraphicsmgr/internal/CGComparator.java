package org.cytoscape.ding.customgraphicsmgr.internal;

import java.util.Comparator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public class CGComparator implements Comparator<CyCustomGraphics> {
  public int compare(CyCustomGraphics o1, CyCustomGraphics o2) {
    String class1 = o1.getClass().getCanonicalName();
    String class2 = o2.getClass().getCanonicalName();
    if (!class1.equals(class2))
      return class1.compareTo(class2);

    return o1.getDisplayName().compareTo(o2.getDisplayName());
  }

  public boolean equals(Object obj) { return false; }
}
