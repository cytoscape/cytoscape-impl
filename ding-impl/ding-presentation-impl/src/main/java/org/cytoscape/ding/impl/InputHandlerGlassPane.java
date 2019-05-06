package org.cytoscape.ding.impl;

import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import static org.cytoscape.ding.impl.DRenderingEngine.Canvas.BACKGROUND_CANVAS;
import static org.cytoscape.ding.impl.DRenderingEngine.Canvas.FOREGROUND_CANVAS;
import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Set;

import javax.swing.JComponent;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.work.TaskManager;

@SuppressWarnings("serial")
public class InputHandlerGlassPane extends JComponent {
	
	private final CyServiceRegistrar registrar;
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;

	private final DingCanvas  backgroundCanvas;
	private final InnerCanvas networkCanvas;
	private final DingCanvas  foregroundCanvas;
	
	private Point2D addingEdgeStartPoint;

	
	public InputHandlerGlassPane(CyServiceRegistrar registrar, DRenderingEngine re) {
		this.registrar = registrar;
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		
		this.backgroundCanvas = re.getCanvas(BACKGROUND_CANVAS);
		this.networkCanvas    = re.getCanvas();
		this.foregroundCanvas = re.getCanvas(FOREGROUND_CANVAS);

		setFocusable(true);
		addKeyListener(new CanvasKeyListener());
        addMouseWheelListener(new CanvasMouseWheelListener());
		addMouseListener(new CanvasMouseListener());
//		addMouseMotionListener(new CanvasMouseMotionListener());
	}
	
	/**
	 * This method exposes the JComponent processMouseEvent so that the birds-eye-view can pass mouse events here.
	 */
	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
	}
	
	/**
	 * This method exposes the JComponent processMouseWheelEvent so that the birds-eye-view can pass mouse events here.
	 */
	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		super.processMouseWheelEvent(e);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// Draw selection rectangle here
		g.draw3DRect(0, 0, 20, 20, true);
		super.paintComponent(g);
	}
	
	private boolean annotationSelectionEnabled() {
		return re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION);
	}
	
	private boolean nodeSelectionEnabled() {
		return re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_NODE_SELECTION);
	}
	
	private boolean edgeSelectionEnabled() {
		return re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_EDGE_SELECTION);
	}
	
	
//	// MKTODO move this somewhere else
//	private static void drawAddingEdge(Graphics2D g, Point2D startPoint, Point2D endPoint) {
//		if(startPoint == null || endPoint == null)
//			return;
//
//        double x1 = startPoint.getX();
//        double y1 = startPoint.getY();
//        double x2 = endPoint.getX();
//        double y2 = endPoint.getY();
//        double offset = 5;
//        
//        double lineLen = Math.sqrt((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
//        if(lineLen == 0)
//            lineLen = 1;
//
//        y2 += ((y1 - y2) / lineLen) * offset;
//        x2 += ((x1 - x2) / lineLen) * offset;
//
//        Color saveColor = g.getColor();
//        g.setColor(Color.BLACK);
//        g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
//        g.setColor(saveColor);
//	}
	
	
	
	private class CanvasKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			
			if(code == VK_UP || code == VK_DOWN || code == VK_LEFT || code == VK_RIGHT) {
				if(annotationSelectionEnabled()) {
					moveAnnotations(e);
				}
				if(isControlOrMetaDown(e)) {
					panCanvas(e);
				} else if(nodeSelectionEnabled()) {
					moveNodesAndHandles(e);
				}
				
			} else if(code == VK_ESCAPE) {
				cancelAddingEdge();
				if(annotationSelectionEnabled()) {
					cancelAnnotations();
				}
				
			} else if(code == VK_BACK_SPACE) {
				deleteSelectedNodesAndEdges();
			}
			
			networkCanvas.repaint();
		}
		
		@Override
		public void keyReleased(KeyEvent e) { 
			int code = e.getKeyCode();
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();

			if(annotationSelectionEnabled() && annotationSelection.count() > 0 && code == VK_DELETE) {
				Set<DingAnnotation> selectedAnnotations = annotationSelection.getSelectedAnnotations();
				for (DingAnnotation ann: selectedAnnotations)
					ann.removeAnnotation();
			} 
		}
		
		private void moveAnnotations(KeyEvent e) {
			//Some annotations have been double clicked and selected
			int code = e.getKeyCode();
			final int move = 2;
			
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			
			for (DingAnnotation annotation : annotationSelection) {
				Component c = annotation.getComponent();
				int x = c.getX(), y = c.getY();
				if (annotation instanceof ShapeAnnotationImpl && e.isShiftDown()) {
					ShapeAnnotationImpl sa = (ShapeAnnotationImpl)annotation;
					int width = c.getWidth(), height = c.getHeight();
					int borderWidth = (int)sa.getBorderWidth(); // We need to take this into account
					if (code == VK_UP) {
						height -= move*2; width -= borderWidth*2;
					} else if (code == VK_DOWN) {
						height += move; width -= borderWidth*2;
					} else if (code == VK_LEFT) {
						width -= move*2; height -= borderWidth*2;
					} else if (code == VK_RIGHT) {
						width += move; height -= borderWidth*2;
					}
					// Adjust the size of the selected annotations
					sa.setSize((double)width, (double)height);
				} else {
					if (code == VK_UP)
						y-=move;
					else if (code == VK_DOWN)
						y+=move;
					else if (code == VK_LEFT)
						x-=move;
					else if (code == VK_RIGHT)
						x+=move;

					//Adjust the locations of the selected annotations
					annotation.getComponent().setLocation(x,y);
				}
				annotation.update();
				annotation.getCanvas().repaint();	
			}
		}
		
		private void cancelAnnotations() {
			if (cyAnnotator.getResizeShape() != null) {
				cyAnnotator.getResizeShape().contentChanged();
				cyAnnotator.resizeShape(null);
				cyAnnotator.postUndoEdit();
			} else if(cyAnnotator.getRepositioningArrow() != null) {
				cyAnnotator.getRepositioningArrow().contentChanged();
				cyAnnotator.positionArrow(null);
				cyAnnotator.postUndoEdit();
			}
		}
		
		
		private void panCanvas(KeyEvent k) {
			int code = k.getKeyCode();
			final float move = k.isShiftDown() ? 15.0f : 1.0f;

			if(code == VK_UP)
				networkCanvas.pan(0, move);
			else if (code == VK_DOWN)
				networkCanvas.pan(0, -move);
			else if (code == VK_LEFT)
				networkCanvas.pan(-move, 0);
			else if (code == VK_RIGHT)
				networkCanvas.pan(move, 0);
		}
		

		private void moveNodesAndHandles(KeyEvent k) {
			final int code = k.getKeyCode();
			final float move = k.isShiftDown() ? 15.0f : 1.0f;
			
			Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
			for (View<CyNode> node : selectedNodes) {
				double xPos = re.getNodeDetails().getXPosition(node);
				double yPos = re.getNodeDetails().getYPosition(node);

				if (code == VK_UP) {
					yPos -= move;
				} else if (code == VK_DOWN) {
					yPos += move;
				} else if (code == VK_LEFT) {
					xPos -= move;
				} else if (code == VK_RIGHT) {
					xPos += move;
				}

				// MKTODO better way of doing this???
				View<CyNode> mutableNodeView = re.getViewModel().getNodeView(node.getSUID());
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPos);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPos);
			}

			Set<HandleKey> handlesToMove = re.getBendStore().getSelectedHandles();
			for (HandleKey handleKey : handlesToMove) {
				View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());

				// MKTODO this code is copy-pasted in a few places, clean it up
				if(!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
					Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
					View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getSUID());
					if(mutableEdgeView != null) {
						if(ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
							mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
						} else {
							Bend bend = re.getEdgeDetails().getBend(ev, true);
							mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
						}
					}
				}
				
				Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
				Handle handle = bend.getAllHandles().get(handleKey.getHandleIndex());
				Point2D newPoint = handle.calculateHandleLocation(re.getViewModel(),ev);
				
				float x = (float) newPoint.getX();
				float y = (float) newPoint.getY();

				if (code == VK_UP) {
					re.getBendStore().moveHandle(handleKey, x, y - move);
				} else if (code == VK_DOWN) {
					re.getBendStore().moveHandle(handleKey, x, y + move);
				} else if (code == VK_LEFT) {
					re.getBendStore().moveHandle(handleKey, x - move, y);
				} else if (code == VK_RIGHT) {
					re.getBendStore().moveHandle(handleKey, x + move, y);
				}
			}
		}
		
		private void cancelAddingEdge() {
			AddEdgeStateMonitor.reset(re.getViewModelSnapshot());
		}
		
		private void deleteSelectedNodesAndEdges() {
			final TaskManager<?, ?> taskManager = registrar.getService(TaskManager.class);
			NetworkTaskFactory taskFactory = registrar.getService(DeleteSelectedNodesAndEdgesTaskFactory.class);
			taskManager.execute(taskFactory.createTaskIterator(re.getViewModel().getModel()));
		}
	}
	
	
	private class CanvasMouseWheelListener implements MouseWheelListener {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			networkCanvas.adjustZoom(e.getWheelRotation());
		}
		
//		private void updateAddingEdgePoint() {
//			//This code updates the source point so that it is better related to the selected node.
//	        //TODO: Center the source point on the selected node perfectly.
//	        if(addingEdgeStartPoint != null) {
//	        	View<CyNode> nodeView = mousePressedDelegator.getPickedNodeView();
//	
//	            AddEdgeStateMonitor.setSourceNode(re.getViewModel(), nodeView);
//	            double[] coords = new double[2];
//	            coords[0] = re.getNodeDetails().getXPosition(nodeView);
//	            coords[1] = re.getNodeDetails().getYPosition(nodeView);
//	            ensureInitialized();
//	            re.xformNodeToComponentCoords(coords);
//	
//	            Point sourceP = new Point();
//	            sourceP.setLocation(coords[0], coords[1]);
//	            AddEdgeStateMonitor.setSourcePoint(re.getViewModel(), sourceP);
//	        }
//		}
	}

	
	private class CanvasMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			requestFocusInWindow(); // need to do this to receive key events
		}
		
	}


}
