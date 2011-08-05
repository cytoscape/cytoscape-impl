package org.cytoscape.ding;

import java.util.EventListener;

public interface GraphViewChangeListener
  extends EventListener {

  /**
   * Invoked when a GraphPerspective to which this
   * GraphPerspectiveChangeListener listens changes.
   */
  public void graphViewChanged ( GraphViewChangeEvent event );

} // interface GraphPerspectiveChangeListener
