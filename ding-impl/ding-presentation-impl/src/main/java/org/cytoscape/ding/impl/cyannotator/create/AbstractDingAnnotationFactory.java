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
