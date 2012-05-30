package org.cytoscape.ding.impl.cyannotator;

import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasKeyListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseMotionListener;
import org.cytoscape.ding.impl.cyannotator.listeners.CanvasMouseWheelListener;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.tasks.ReloadImagesTask;

import java.awt.Component;

import java.awt.Point;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private ShapeAnnotation resizing = null;
	private Annotation moving = null;

	private Map<Annotation, Map<String,String>> annotationMap = 
	        new HashMap<Annotation, Map<String,String>>();

	private Set<Annotation> selectedAnnotations = new HashSet<Annotation>();

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
		foreGroundCanvas.addMouseListener(new CanvasMouseListener(this, view));
		foreGroundCanvas.addMouseMotionListener(new CanvasMouseMotionListener(this, view));
		foreGroundCanvas.addKeyListener(new CanvasKeyListener(this, view));
		foreGroundCanvas.setFocusable(true);

		//The created annotations resize (Their font changes), if we zoom in and out
		foreGroundCanvas.addMouseWheelListener(new CanvasMouseWheelListener(this, view));

		//We also setup this class as a ViewportChangeListener to the current networkview
		myViewportChangeListener=new MyViewportChangeListener();
		view.addViewportChangeListener(myViewportChangeListener);
	}

	public void loadAnnotations() {
		// System.out.println("Loading annotations");
		CyNetwork network = view.getModel();
		// Now, see if this network has any existing annotations
		final CyTable networkAttributes = network.getDefaultNetworkTable();

		// This should be in the HIDDEN_ATTRS namespace, but we can't get to it
		// without a pointer to the CyNetworkTableManager
		if (networkAttributes.getColumn(ANNOTATION_ATTRIBUTE) == null) {
			networkAttributes.createListColumn(ANNOTATION_ATTRIBUTE,
			                                   String.class,false,Collections.EMPTY_LIST);
		}

		List<String> annotations = network.getRow(network).
		                                          getList(ANNOTATION_ATTRIBUTE,String.class);
		Map<Integer, Annotation> idMap = 
		    new HashMap<Integer, Annotation>(); // Keep a map of the original annotation ID's

		List<Map<String,String>> arrowList = 
		    new ArrayList<Map<String, String>>(); // Keep a list of arrows

		if (annotations != null) {
			for (String s: annotations) {
				Map<String, String> argMap = createArgMap(s);
				Annotation annotation = null;
				String type = argMap.get("type");
				if (type == null)
	                continue;
	
				annotation = annotationFactoryManager.getAnnotation(type,this,view,argMap);
	
				if (annotation != null) {
					foreGroundCanvas.add(annotation.getComponent());
				}
			}
		}
		
/*
		// OK, now we add the arrows, if we have any
		for (Map<String, String>argMap: arrowList) {
			// Create the arrow annotation
			ArrowAnnotation arrow = new ArrowAnnotation(this,view,argMap);
			// Find the source
			Integer source = arrow.getSource();
			Annotation annotation = idMap.get(source);
			if (annotation != null) {
				annotation.addArrow(arrow);
				arrow.setSource(annotation.getComponentNumber());
			}
		}
*/
	}

	public void update() { view.updateView(); }

	public Component getComponentAt(ArbitraryGraphicsCanvas cnvs, int x, int y) {
		return cnvs.getComponentAt(x, y);
	}

	public Annotation getAnnotation(Point2D position) {
		Component c = getComponentAt(foreGroundCanvas, (int)position.getX(), (int)position.getY());
		if (c != null && c instanceof Annotation) {
			return (Annotation)c;
		}

		c = getComponentAt(backGroundCanvas, (int)position.getX(), (int)position.getY());
		if (c != null && c instanceof Annotation) {
			return (Annotation)c;
		}

		return null;
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
		annotationMap.put(annotation, annotation.getArgMap());
		updateNetworkAttributes(view.getModel());
	}

	public void removeAnnotation(Annotation annotation) {
		annotationMap.remove(annotation);
		updateNetworkAttributes(view.getModel());
	}

	public Task getReloadImagesTask() {
		return new ReloadImagesTask(this);
	}

	public void setSelectedAnnotation(Annotation a, boolean selected) {
		if (selected) 
			selectedAnnotations.add(a);
		else
			selectedAnnotations.remove(a);
	}

	public Set getSelectedAnnotations() { return selectedAnnotations; }

	public void resizeShape(ShapeAnnotation shape) {
		resizing = shape;
	}

	public ShapeAnnotation getResizeShape() {
		return resizing;
	}

	public void moveAnnotation(Annotation annotation) {
		moving = annotation;
	}

	public Annotation getMovingAnnotation() {
		return moving;
	}

	private void updateNetworkAttributes(CyNetwork network) {
		// Convert the annotation to a list
		List<Map<String,String>> networkAnnotations = new ArrayList<Map<String, String>>();
		for (Annotation annotation: annotationMap.keySet()) {
			if (view.getModel().equals(network))
				networkAnnotations.add(annotationMap.get(annotation));
		}
		// Save it in the network attributes
		List<String>networkAnnotation = convertAnnotationMap(networkAnnotations);
		network.getRow(network).set(ANNOTATION_ATTRIBUTE, networkAnnotation);
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
				if(annotations[i] instanceof Annotation) {
					((Annotation)annotations[i]).setZoom(newZoom);
				}
			}

			annotations=backGroundCanvas.getComponents();
			for(int i=0;i<annotations.length;i++){
				if(annotations[i] instanceof Annotation) {
					((Annotation)annotations[i]).setZoom(newZoom);
				}
			}

			view.updateView();	
		}
	}
}
