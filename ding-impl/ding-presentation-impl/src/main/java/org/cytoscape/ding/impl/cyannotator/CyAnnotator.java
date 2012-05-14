package org.cytoscape.ding.impl.cyannotator;

import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;

import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.events.ViewportChangeListener;


public class CyAnnotator {

	private MyViewportChangeListener myViewportChangeListener=null;

	private static boolean DRAG_VAL=false;
	private static boolean annotationEnlarge=false;
	private static boolean drawShape=false;

	private static final String ANNOTATION_ATTRIBUTE="__Annotations";

	private Map<Annotation, Map<String,String>> annotationMap = 
	        new HashMap<Annotation, Map<String,String>>();

	private List<Annotation> selectedAnnotations=new ArrayList<Annotation>();
	private double prevZoom=1;
	private ShapeAnnotation newShape=null;
	private ShapeAnnotation createShape=null;

	private final DGraphView view;
	private final ArbitraryGraphicsCanvas foreGroundCanvas;
	private final InnerCanvas networkCanvas;
	private final AnnotationFactoryManager annotationFactoryManager; 

	public CyAnnotator(DGraphView view, AnnotationFactoryManager annotationFactoryManager) {
		this.view = view;
		this.foreGroundCanvas = 
			(ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.networkCanvas = view.getCanvas();
		this.annotationFactoryManager = annotationFactoryManager;
		initListeners();  
	}
	
	private void initListeners() {
		foreGroundCanvas.addMouseListener(new ForegroundMouseListener());
		foreGroundCanvas.addMouseMotionListener(new ForegroundMouseMotionListener());
		foreGroundCanvas.addKeyListener(new ForegroundKeyListener());
		foreGroundCanvas.setFocusable(true);

		//Set up the foreGroundCanvas as a dropTarget, so that we can drag and drop JPanels, created Annotations onto it.
		//We also set it up as a DragSource, so that we can drag created Annotations
		// TODO: This should be replaced with a drag of the component
		DropTargetComponent dtarget=new DropTargetComponent();
		DragSourceComponent dsource=new DragSourceComponent();

		//The created annotations resize (Their font changes), if we zoom in and out
		foreGroundCanvas.addMouseWheelListener(new MyMouseWheelListener());

		//We also setup this class as a ViewportChangeListener to the current networkview
		myViewportChangeListener=new MyViewportChangeListener();
		view.addViewportChangeListener(myViewportChangeListener);
	}


	public void loadAnnotations() {
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
					idMap.put(annotation.getComponentNumber(), annotation);
					foreGroundCanvas.add(annotation);
					annotation.setComponentNumber(foreGroundCanvas.getComponentCount()-1);
				}
			}
		}
		
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
	}

	public Component getComponentAt(int x, int y) {
		return foreGroundCanvas.getComponentAt(x, y);
	}

	public void modifyComponentLocation(int x, int y, int number) {
		foreGroundCanvas.modifyComponentLocation(x, y, number);
	}

	public InnerCanvas getNetworkCanvas() {
		return networkCanvas;
	}

	public ArbitraryGraphicsCanvas getForeGroundCanvas() {
		return foreGroundCanvas;
	}

	public void addAnnotation(Annotation annotation) {
		annotationMap.put(annotation, annotation.getArgMap());
		updateNetworkAttributes(annotation.getNetwork());
	}

	public void removeAnnotation(Annotation annotation) {
		annotationMap.remove(annotation);
		updateNetworkAttributes(annotation.getNetwork());
	}

	private void updateNetworkAttributes(CyNetwork network) {
		// Convert the annotation to a list
		List<Map<String,String>> networkAnnotations = new ArrayList<Map<String, String>>();
		for (Annotation annotation: annotationMap.keySet()) {
			if (annotation.getNetwork().equals(network))
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

	class MyViewportChangeListener implements ViewportChangeListener {
		public void viewportChanged(int x, int y, double width, double height, double newZoom) {
			//We adjust the font size of all the created annotations if the  if there are changes in viewport
			Component[] annotations=foreGroundCanvas.getComponents();

			for(int i=0;i<annotations.length;i++){
				if(annotations[i] instanceof Annotation)
					((Annotation)annotations[i]).adjustZoom(newZoom);
			}

			view.updateView();	
		}
	}

	public void startDrawShape(ShapeAnnotation createShape, int x, int y) {
		drawShape=true;
		this.createShape=createShape;

		//createShape will have all the properties associated with the shape to be drawn
		//Create a shapeAnnotattion based on these properties and add it to foreGroundCanvas
		newShape= new ShapeAnnotation(this,view, x, y, createShape.getShapeType(), 
		                              10, 10,
		                              createShape.getFillColor(), 
		                              createShape.getEdgeColor(), createShape.getEdgeThickness());
		foreGroundCanvas.add(newShape);
	}


	private class DragSourceComponent extends DragSourceAdapter implements DragGestureListener {
		//Add the foreGroundCanvas as DraggableComponent
		DragSource dragSource;

		DragSourceComponent() {
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer( foreGroundCanvas, 
			                                               DnDConstants.ACTION_COPY_OR_MOVE, this);
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			Component annotation = getComponentAt((int)(dge.getDragOrigin().getX()), 
			                                      (int)(dge.getDragOrigin().getY()));

			//Add the component number of the annotation being dragged in the form of string to transfer information
			if(annotation!=null){
				Transferable t = new StringSelection(new Integer(((Annotation)annotation).getComponentNumber()).toString());
				dragSource.startDrag (dge, DragSource.DefaultCopyDrop, t, this);
			}
		}
	}

	private class DropTargetComponent implements DropTargetListener {
	
		// Add the foreGroundCanvas as a drop Target
		public DropTargetComponent()
		{
			new DropTarget(foreGroundCanvas, this);
		}

		public void dragEnter(DropTargetDragEvent evt){}

		public void dragOver(DropTargetDragEvent evt){
			try
			{
				Transferable t = evt.getTransferable();
	
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					String s = (String)t.getTransferData(DataFlavor.stringFlavor);
					//Get hold of the transfer information and complete the drop
					//Based on that information popup appropriate JFrames to create those Annotatons
					Component annotation=(foreGroundCanvas.getComponent(Integer.parseInt(s)));
					if (annotation instanceof Annotation) {
						Annotation textAnnotation = (Annotation)annotation;
						if(!textAnnotation.getDrawArrow()){
							//The drop has been done to move an annotation to a new location
							textAnnotation.setLocation((int)evt.getLocation().getX(),(int)evt.getLocation().getY());
							//This will modify the initial location of this annotation stored in an array in 	
							//Very important. Without it you won't be able to handle change in viewports
							modifyComponentLocation(textAnnotation.getX(), 
							                        textAnnotation.getY(), 
							                        textAnnotation.getComponentNumber());
						 }
					}
	
					//Repaint the whole network
					view.updateView();	
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}		  
		
		}

		public void dragExit(DropTargetEvent evt){}

		public void dropActionChanged(DropTargetDragEvent evt){}

		public void drop(DropTargetDropEvent evt) {

			try {
				Transferable t = evt.getTransferable();
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					String s = (String)t.getTransferData(DataFlavor.stringFlavor);
					evt.getDropTargetContext().dropComplete(true);
	
					//Get hold of the transfer information and complete the drop
					//Based on that information popup appropriate JFrames to create those Annotatons
					Component annotation=(foreGroundCanvas.getComponent(Integer.parseInt(s)));
					if (annotation instanceof Annotation) {
						Annotation textAnnotation = (Annotation)annotation;
						if(textAnnotation.getDrawArrow()){
							//The drop has been done to create a new Arrow from an Annotation
							textAnnotation.setDrawArrow(false);
							textAnnotation.setArrowPoints((int)evt.getLocation().getX(),(int)evt.getLocation().getY());
							textAnnotation.setArrowDrawn(true);
						} else {
							//The drop has been done to move an annotation to a new location
							textAnnotation.setLocation((int)evt.getLocation().getX(),(int)evt.getLocation().getY());
							//This will modify the initial location of this annotation stored in an array in foreGroundCanvas
							//Very important. Without it you won't be able to handle change in viewports
							modifyComponentLocation(annotation.getX(), 
							                        annotation.getY(), 
							                        textAnnotation.getComponentNumber());
						}
						// Update our attributes
						textAnnotation.updateAnnotationAttributes();
					}
					//Repaint the whole network
					view.updateView();	
				} else {
					networkCanvas.getDropTarget().drop(evt);
				}
			} catch (Exception e) {
				e.printStackTrace();
				evt.rejectDrop();
			}
		}
	}

	class MyMouseWheelListener implements MouseWheelListener{

		//To handle zooming in and out
		public void mouseWheelMoved(MouseWheelEvent e) {

			int notches = e.getWheelRotation();
			double factor = 1.0;

			// scroll up, zoom in
			if (notches < 0)
					factor = 1.1;
			else
					factor = 0.9;
	
			if(annotationEnlarge){
				//If some annotations are selected
				for (Annotation annotation: selectedAnnotations) {
					annotation.adjustSpecificZoom( prevZoom * factor  );
				}
	
				//In that case only increase the size (Change font in some cases) 
				//for those specific annotations
				prevZoom*=factor;
			} else {
				networkCanvas.mouseWheelMoved(e);
			}
			view.updateView();	
		}
	}

	//Returns a boolean value, whether this is a Mac Platform or not

	private boolean isMacPlatform() {

		String MAC_OS_ID = "mac";
		String os = System.getProperty("os.name");

		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}

	private final class ForegroundMouseListener implements MouseListener {

		public void mousePressed(MouseEvent e) {
			Component comp = getComponentAt(e.getX(), e.getY());
			Annotation newOne = null;
	
			if (comp instanceof Annotation && comp!=null) {
				newOne = (Annotation)comp;
	
				//We might drag this annotation
				DRAG_VAL=true;
	
				//We have right clicked on the Annotation, show a popup
				if( (e.getButton() == MouseEvent.BUTTON3) || ( isMacPlatform()  && e.isControlDown()) ) {
					newOne.showChangePopup(e);
				}
			} else {
				//Let the InnerCanvas handle this event
				networkCanvas.processMouseEvent(e);
			}
		}

		public void mouseReleased(MouseEvent e) {
			//We might have finished dragging this Annotation
			DRAG_VAL=false;
	
			//Let the InnerCanvas handle this event
			networkCanvas.processMouseEvent(e);
		}

		public void mouseClicked(MouseEvent e) {
			Component comp = getComponentAt(e.getX(), e.getY());
			Annotation newOne = null;
			if(comp instanceof Annotation)
				newOne = (Annotation)comp;
	
			if(e.getClickCount()==2 && newOne!=null && (newOne.isPointOnArrow(e.getX(), e.getY()) == -1)){
				//We have doubled clicked on an Annotation
				annotationEnlarge=true;
	
				//Add this Annotation to the list of selected Annotations
				selectedAnnotations.add(newOne);
	
				//This preVZoom value will help in resizing the selected Annotations
				prevZoom=networkCanvas.getScaleFactor();
	
				newOne.setTempZoom(prevZoom);
				newOne.setSelected(true);
	
				//We request focus in this window, so that we can move these selected Annotations around using arrow keys
				foreGroundCanvas.requestFocusInWindow();
	
				//Repaint the whole network. The selected annotations will have a yellow outline now
				view.updateView();	
			} else if(drawShape) {
				drawShape=false;
	
				//We have finished drawing a shapeAnnotation
				//We set the otherCorner of that Annotation
				newShape.setOtherCorner(e.getX(), e.getY());
	
				newShape.adjustCorners();

				view.updateView();	
			} else if(newOne==null) {
				//Handle the case where we have clicked on a node
				//We have clicked somewhere else on the network, de-select all the selected Annotations
				annotationEnlarge=false;
	
				if(!selectedAnnotations.isEmpty()) {
					for (Annotation annotation: selectedAnnotations)
						annotation.setSelected(false);
					selectedAnnotations.clear();
				}
				view.updateView();	
			} else {
				//Let the InnerCanvas handle this event
				networkCanvas.processMouseEvent(e);
			}
		}

		public void mouseEntered(MouseEvent e) {
			networkCanvas.processMouseEvent(e);
		}

		public void mouseExited(MouseEvent e) {
			networkCanvas.processMouseEvent(e);
		}
	}

	private class ForegroundMouseMotionListener implements MouseMotionListener{

		public void mouseDragged(MouseEvent e) {
			//If we are not dragging an Annotation then let the InnerCanvas handle this event
			if(!DRAG_VAL)
				networkCanvas.mouseDragged(e);
		}

		public void mouseMoved(MouseEvent e) {
			if(drawShape){
				//We are drawing a shape
				newShape.setOtherCorner(e.getX(), e.getY());
				view.updateView();	
			} else
				networkCanvas.mouseMoved(e);
		}
	}

	private class ForegroundKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();

			if(annotationEnlarge && 
			   ((code == KeyEvent.VK_UP) || 
			    (code == KeyEvent.VK_DOWN) || 
			    (code == KeyEvent.VK_LEFT)|| 
			    (code == KeyEvent.VK_RIGHT) ) )
			{
				//Some annotations have been double clicked and selected
				int move=2;
				for (Annotation annotation: selectedAnnotations) {
					int x=annotation.getX(), y=annotation.getY();
					if (code == KeyEvent.VK_UP)
						y-=move;
					else if (code == KeyEvent.VK_DOWN)
						y+=move;
					else if (code == KeyEvent.VK_LEFT)
						x-=move;
					else if (code == KeyEvent.VK_RIGHT)
						x+=move;

					//Adjust the locations of the selected annotations
					annotation.setLocation(x,y);
					modifyComponentLocation(annotation.getX(), annotation.getY(), 
					                        annotation.getComponentNumber());
				}
				view.updateView();	
			}
			networkCanvas.keyPressed(e);
		}

		public void keyReleased(KeyEvent e) { }

		public void keyTyped(KeyEvent e) { }
	}
}
