package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;

public abstract class AbstractDingAnnotationFactory<T extends Annotation> implements AnnotationFactory<T> {
	
	public static final int ICON_SIZE = 16;
	protected static final String NAMESPACE = "org.cytoscape.annotation.";
	
	protected final Class<T> type;
	private final Map<String, String> defArgs = new HashMap<>();
	protected final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	protected AbstractDingAnnotationFactory(Class<T> type, CyServiceRegistrar serviceRegistrar) {
		this.type = type;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public abstract JDialog createAnnotationDialog(DGraphView view, Point2D location); 
	
	@Override
	public Class<T> getType() {
		return type;
	}
	
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
