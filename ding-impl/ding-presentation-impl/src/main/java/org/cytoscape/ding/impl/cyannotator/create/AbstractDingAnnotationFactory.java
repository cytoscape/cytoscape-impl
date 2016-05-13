package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Window;
import java.awt.geom.Point2D;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;

public abstract class AbstractDingAnnotationFactory<T extends Annotation> implements AnnotationFactory<T> {
	
	protected final CyServiceRegistrar serviceRegistrar;

	protected AbstractDingAnnotationFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public abstract JDialog createAnnotationDialog(DGraphView view, Point2D location); 
	
	protected Window getActiveWindow() {
		Window window = null;
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		final JMenuBar menuBar = swingApplication.getJMenuBar();
		
		if (menuBar != null)
			window = SwingUtilities.getWindowAncestor(menuBar);
		
		if (window == null)
			window = swingApplication.getJFrame();
		
		return window;
	}
}
