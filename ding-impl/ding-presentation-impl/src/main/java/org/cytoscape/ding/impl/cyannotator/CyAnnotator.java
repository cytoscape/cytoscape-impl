package org.cytoscape.ding.impl.cyannotator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
// import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
// import org.cytoscape.view.presentation.annotations.ArrowAnnotation;

import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasKeyListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseMotionListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseWheelListener;
import org.cytoscape.ding.impl.cyannotator.tasks.ReloadImagesTask;

import java.awt.Component;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Task;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;

import org.cytoscape.ding.impl.events.ViewportChangeListener;


public class CyAnnotator {
	private static final String ANNOTATION_ATTRIBUTE="__Annotations";

	private final DGraphView view;
	private final ArbitraryGraphicsCanvas foreGroundCanvas;
	private final ArbitraryGraphicsCanvas backGroundCanvas;
	private final InnerCanvas networkCanvas;
	private final AnnotationFactoryManager annotationFactoryManager; 
	private MyViewportChangeListener myViewportChangeListener=null;
	private ShapeAnnotationImpl resizing = null;
	private ArrowAnnotationImpl repositioning = null;
	private DingAnnotation moving = null;

	private Map<DingAnnotation, Map<String,String>> annotationMap = 
	        new HashMap<DingAnnotation, Map<String,String>>();

	private Set<DingAnnotation> selectedAnnotations = new HashSet<DingAnnotation>();

	private CanvasMouseMotionListener mouseMotionListener;
	private CanvasMouseListener mouseListener;
	private CanvasKeyListener keyListener;
	private CanvasMouseWheelListener mouseWheelListener;

	public CyAnnotator(DGraphView view, AnnotationFactoryManager annotationFactoryManager) {
		this.view = view;
		this.foreGroundCanvas = 
			(ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.backGroundCanvas = 
			(ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
		this.networkCanvas = view.getCanvas();
		this.annotationFactoryManager = annotationFactoryManager;
		initListeners();  
	}

	private void initListeners() {
		mouseListener = new CanvasMouseListener(this, view);
		mouseMotionListener = new CanvasMouseMotionListener(this, view);
		keyListener = new CanvasKeyListener(this, view);
		mouseWheelListener = new CanvasMouseWheelListener(this, view);
		
		foreGroundCanvas.addMouseListener(mouseListener);
		foreGroundCanvas.addMouseMotionListener(mouseMotionListener);
		foreGroundCanvas.addKeyListener(keyListener);
		foreGroundCanvas.setFocusable(true);

		//The created annotations resize (Their font changes), if we zoom in and out
		foreGroundCanvas.addMouseWheelListener(mouseWheelListener);

		//We also setup this class as a ViewportChangeListener to the current networkview
		myViewportChangeListener=new MyViewportChangeListener();
		view.addViewportChangeListener(myViewportChangeListener);
	}
	
	public void dispose() {
		// Bug #1178: Swing's focus subsystem is leaking foreGroundCanvas.
		// We need to remove all our listeners from that class to ensure we
		// don't leak anything further.
		foreGroundCanvas.removeMouseListener(mouseListener);
		foreGroundCanvas.removeMouseMotionListener(mouseMotionListener);
		foreGroundCanvas.removeKeyListener(keyListener);
		foreGroundCanvas.removeMouseWheelListener(mouseWheelListener);
		
		view.removeViewportChangeListener(myViewportChangeListener);
		
		foreGroundCanvas.dispose();
		backGroundCanvas.dispose();
	}

	public void loadAnnotations() {
		// Make sure we're on the EDT since we directly add annotations to the canvas
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> loadAnnotations());
			return;
		}

		// System.out.println("Loading annotations");
		CyNetwork network = view.getModel();
		// Now, see if this network has any existing annotations
		final CyTable networkAttributes = 
			network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);

		if (networkAttributes.getColumn(ANNOTATION_ATTRIBUTE) == null) {
			networkAttributes.createListColumn(ANNOTATION_ATTRIBUTE,
			                                   String.class,false,Collections.EMPTY_LIST);
		}

		List<String> annotations = network.getRow(network, CyNetwork.LOCAL_ATTRS).
		                                          getList(ANNOTATION_ATTRIBUTE,String.class);

		List<Map<String,String>> arrowList = 
		    new ArrayList<Map<String, String>>(); // Keep a list of arrows

		Map<DingAnnotation, Integer> zOrderMap = new HashMap<>();

		if (annotations != null) {
			for (String s: annotations) {
				Map<String, String> argMap = createArgMap(s);
				DingAnnotation annotation = null;
				String type = argMap.get("type");
				if (type == null)
					continue;

				if (type.equals("ARROW") || type.equals("org.cytoscape.view.presentation.annotations.ArrowAnnotation")) {
					arrowList.add(argMap);
					continue;
				}
	
				Annotation a = annotationFactoryManager.createAnnotation(type,view,argMap);
				if (a != null && a instanceof DingAnnotation) {
					annotation = (DingAnnotation)a;

					if (annotation.getCanvas() != null) {
						annotation.getCanvas().add(annotation.getComponent());
					} else {
						foreGroundCanvas.add(annotation.getComponent());
					}
				}

				int zOrder = 0;
				if (argMap.containsKey(Annotation.Z)) {
					zOrder = Integer.parseInt(argMap.get(Annotation.Z));
					zOrderMap.put(annotation, zOrder);
				}

				// Now that we've added the annotation, update
				// the group membership
				if (a != null && a instanceof GroupAnnotation) {
					GroupAnnotation g = (GroupAnnotation)a;
					for (Annotation child: g.getMembers()) {
						g.addMember(child);
					}
				}
			}

			// Now, handle all of our arrows
			for (Map<String,String> argMap: arrowList) {
				String type = argMap.get("type");
				Annotation annotation = annotationFactoryManager.createAnnotation(type,view,argMap);
				if (annotation instanceof ArrowAnnotationImpl) {
					ArrowAnnotationImpl arrow = (ArrowAnnotationImpl)annotation;
					int zOrder = 0;
					if (argMap.containsKey(Annotation.Z)) {
						zOrder = Integer.parseInt(argMap.get(Annotation.Z	));
						zOrderMap.put(arrow, zOrder);
					}
					arrow.getSource().addArrow(arrow);
					if (arrow.getCanvas() != null)
						arrow.getCanvas().add(arrow.getComponent());
					else
						foreGroundCanvas.add(arrow.getComponent());

					addAnnotation(arrow);
				}
			}

			// Now, handle our Z-Order.  This needs to be done after everything else is
			// added to make sure that we have the proper number of components
			for (DingAnnotation a: zOrderMap.keySet()) {
				if (a.getCanvas() != null)
					a.getCanvas().setComponentZOrder(a.getComponent(), zOrderMap.get(a));
				else
					foreGroundCanvas.setComponentZOrder(a.getComponent(), zOrderMap.get(a));
			}

		}
	}

	public DingAnnotation getAnnotation(UUID annotationID) {
		for (DingAnnotation a: annotationMap.keySet()) {
			if (a.getUUID().equals(annotationID))
				return a;
		}
		return null;
	}

	public void update() { view.updateView(); }

	public DGraphView getView() { return view; }

	/**
 	 * Find all of our annotations that are at this point.  Return the top annotation
 	 * (the one with the lowest Z value) if there are more than one.
 	 *
 	 * @param cnvs the Canvas we're looking at
 	 * @param x the x value of the point
 	 * @param y the y value of the point
 	 * @return the component
 	 */
	public DingAnnotation getComponentAt(ArbitraryGraphicsCanvas cnvs, int x, int y) {
		DingAnnotation top = null;
		for (DingAnnotation a: annotationMap.keySet()) {
			// System.out.println("Looking at component "+a.toString());
			if (a.getCanvas().equals(cnvs) && a.getComponent().contains(x, y)) {
				// System.out.println("Found component "+a.toString()+".  Z-order = "+cnvs.getComponentZOrder(a.getComponent()));
				if ((top == null) || 
				    (cnvs.getComponentZOrder(top.getComponent()) >
             cnvs.getComponentZOrder(a.getComponent()))) {
						top = a;
				}
			}
		}
		return top;
	}

	public DingAnnotation getAnnotationAt(Point2D position) {
		DingAnnotation a = getComponentAt(foreGroundCanvas, (int)position.getX(), (int)position.getY());
		if (a != null) {
			while (a.getGroupParent() != null) {
				a = (DingAnnotation)a.getGroupParent();
			}
			return a;
		}

		a = getComponentAt(backGroundCanvas, (int)position.getX(), (int)position.getY());
		if (a != null) {
			while (a.getGroupParent() != null)
				a = (DingAnnotation)a.getGroupParent();
		}
		return a;
	}

	public InnerCanvas getNetworkCanvas() {
		return networkCanvas;
	}

	public ArbitraryGraphicsCanvas getForeGroundCanvas() {
		return foreGroundCanvas;
	}

	public ArbitraryGraphicsCanvas getBackGroundCanvas() {
		return backGroundCanvas;
	}

	public void addAnnotation(Annotation annotation) {
		if (!(annotation instanceof DingAnnotation))
			return;
		DingAnnotation dingAnnotation = (DingAnnotation)annotation;
		annotationMap.put(dingAnnotation, dingAnnotation.getArgMap());
		updateNetworkAttributes(view.getModel());
	}

	public void removeAnnotation(Annotation annotation) {
		annotationMap.remove((DingAnnotation)annotation);
		updateNetworkAttributes(view.getModel());
	}

	public List<Annotation> getAnnotations() {
		if (annotationMap.keySet() != null && annotationMap.keySet().size() > 0)
			return new ArrayList<Annotation>(annotationMap.keySet());
		return null;
	}

	public void setSelectedAnnotation(final DingAnnotation a, final boolean selected) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setSelectedAnnotation(a, selected));
			return;
		}

		if (selected) {
			requestFocusInWindow(a);
			selectedAnnotations.add(a);
		} else
			selectedAnnotations.remove(a);
	}

	public void clearSelectedAnnotations() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> clearSelectedAnnotations());
			return;
		}

		boolean repaintForeGround = false;
		boolean repaintBackGround = false;
		for (DingAnnotation a: new ArrayList<DingAnnotation>(selectedAnnotations)) {
			setSelectedAnnotation(a, false);
			if (a.getCanvasName().equals(Annotation.FOREGROUND))
				repaintForeGround = true;
			else
				repaintBackGround = true;
		}
		if (repaintForeGround)
			foreGroundCanvas.repaint();
		if (repaintBackGround)
		backGroundCanvas.repaint();

	}

	public Set<DingAnnotation> getSelectedAnnotations() { return selectedAnnotations; }

	public void resizeShape(ShapeAnnotationImpl shape) {
		resizing = shape;
		if (resizing != null)
			requestFocusInWindow(resizing);
	}

	public ShapeAnnotationImpl getResizeShape() {
		return resizing;
	}

	public void positionArrow(ArrowAnnotationImpl arrow) {
		repositioning = arrow;
		if (repositioning != null)
			requestFocusInWindow(repositioning);
	}

	public ArrowAnnotationImpl getRepositioningArrow() {
		return repositioning;
	}

	public Task getReloadImagesTask() {
		return new ReloadImagesTask(this);
	}

	public void moveAnnotation(DingAnnotation annotation) {
		// Get the top-level group
		while ((annotation != null) && (annotation.getGroupParent() != null)) {
			annotation = (DingAnnotation)annotation.getGroupParent();
		}
		moving = annotation;
		if (moving != null)
			requestFocusInWindow(moving);
	}

	public DingAnnotation getMovingAnnotation() {
		return moving;
	}

	private void updateNetworkAttributes(CyNetwork network) {
		// Convert the annotation to a list
		List<Map<String,String>> networkAnnotations = new ArrayList<Map<String, String>>();
		for (DingAnnotation annotation: annotationMap.keySet()) {
			if (view.getModel().equals(network))
				networkAnnotations.add(annotationMap.get(annotation));
		}
		// Save it in the network attributes
		List<String>networkAnnotation = convertAnnotationMap(networkAnnotations);
		network.getRow(network, CyNetwork.LOCAL_ATTRS).set(ANNOTATION_ATTRIBUTE, networkAnnotation);
	}

	private List<String> convertAnnotationMap(List<Map<String, String>>networkAnnotations) {
		List<String> result = new ArrayList<String>();

		if (networkAnnotations == null || networkAnnotations.size() == 0) return result;

		for (Map<String,String> map: networkAnnotations) {
			String entry = "";
			for (String key: map.keySet()) {
				entry += "|"+key+"="+map.get(key);
			}
			result.add(entry.substring(1));
		}
		return result;
	}

	private Map<String, String> createArgMap(String mapstring) {
		Map<String, String> result = new HashMap<String, String>();
		String[] argList = mapstring.split("[|]");
		if (argList.length == 0) return result;

		for (int argIndex = 0; argIndex < argList.length; argIndex++) {
			String arg = argList[argIndex];
			String[] keyValue = arg.split("=");
			if (keyValue.length != 2) continue;
			result.put(keyValue[0], keyValue[1]);
		}
		return result;
	}

	private void requestFocusInWindow(final Annotation annotation) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> requestFocusInWindow(annotation));
			return;
		}
		if (annotation != null && annotation instanceof DingAnnotation) {
			((DingAnnotation)annotation).getCanvas().requestFocusInWindow();
		}
	}

/*
	private String printCurrentAnnotations() {
		String result = "Foreground: \n";
		Component[] annotations=foreGroundCanvas.getComponents();
		for(int i=0;i<annotations.length;i++){
			if(annotations[i] instanceof Annotation) {
				result += "  "+((Annotation)annotations[i]).toString()+"\n";
			}
		}
		annotations=backGroundCanvas.getComponents();
		result += "Background: \n";
		for(int i=0;i<annotations.length;i++){
			if(annotations[i] instanceof Annotation) {
				result += "  "+((Annotation)annotations[i]).toString()+"\n";
			}
		}
		return result;
	}
*/

	class MyViewportChangeListener implements ViewportChangeListener {
		public void viewportChanged(int x, int y, double width, double height, double newZoom) {
			//We adjust the font size of all the created annotations if the  if there are changes in viewport
			Component[] annotations=foreGroundCanvas.getComponents();

			for(int i=0;i<annotations.length;i++){
				if(annotations[i] instanceof DingAnnotation) {
					((DingAnnotation)annotations[i]).setZoom(newZoom);
				}
			}

			annotations=backGroundCanvas.getComponents();
			for(int i=0;i<annotations.length;i++){
				if(annotations[i] instanceof DingAnnotation) {
					((DingAnnotation)annotations[i]).setZoom(newZoom);
				}
			}
		}
	}
}
