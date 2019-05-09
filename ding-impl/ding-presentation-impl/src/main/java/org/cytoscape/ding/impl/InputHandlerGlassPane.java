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
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;
import static org.cytoscape.ding.internal.util.ViewUtil.isDragSelectionKeyDown;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ViewChangeEdit;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.ding.impl.InnerCanvas.Toggle;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnchorLocation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.tasks.AnnotationEdit;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class InputHandlerGlassPane extends JComponent {
	
	private static final Color SELECTION_RECT_BORDER_COLOR_1 = UIManager.getColor("Focus.color");
	private static final Color SELECTION_RECT_BORDER_COLOR_2 = new Color(255, 255, 255, 160);
	
	
	private final CyServiceRegistrar registrar;
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;
	private final PopupMenuHelper popupMenuHelper;
	private final Cursor moveCursor;

	private final DingCanvas  backgroundCanvas;
	private final InnerCanvas networkCanvas;
	private final DingCanvas  foregroundCanvas;
	
	private Point2D addingEdgeStartPoint;
	private Rectangle selectionRect;
//	private boolean button1NodeDrag;
	private ViewChangeEdit undoableEdit;

	
	public InputHandlerGlassPane(CyServiceRegistrar registrar, DRenderingEngine re) {
		this.registrar = registrar;
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		
		this.backgroundCanvas = re.getCanvas(BACKGROUND_CANVAS);
		this.networkCanvas    = re.getCanvas();
		this.foregroundCanvas = re.getCanvas(FOREGROUND_CANVAS);

		this.popupMenuHelper = new PopupMenuHelper(re, this, registrar);
		this.moveCursor = createMoveCursor();
		
		setFocusable(true);
		addKeyListener(new CanvasKeyListener());
        addMouseWheelListener(new CanvasMouseWheelListener());
        
		CanvasMouseListener mouseListener = new CanvasMouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		drawSelectionRectangle(g);
		// drawAddingEdge(g);
		// MKTODO draw "adding edge"
	}
	
	private void drawSelectionRectangle(Graphics graphics) {
		// Draw selection rectangle
		if(selectionRect != null) {
			Graphics2D g = (Graphics2D) graphics.create();
			// External border
			g.setColor(SELECTION_RECT_BORDER_COLOR_1);
			g.draw(selectionRect);
			// Internal border
			if (selectionRect.width > 4 && selectionRect.height > 4) {
				g.setColor(SELECTION_RECT_BORDER_COLOR_2);
				g.drawRect(
						selectionRect.x + 1,
						selectionRect.y + 1,
						selectionRect.width - 2,
						selectionRect.height - 2
				);
			}
		}
	}
	
	private static void drawAddingEdge(Graphics2D g, Point2D startPoint, Point2D endPoint) {
		if(startPoint == null || endPoint == null)
			return;

        double x1 = startPoint.getX();
        double y1 = startPoint.getY();
        double x2 = endPoint.getX();
        double y2 = endPoint.getY();
        double offset = 5;
        
        double lineLen = Math.sqrt((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
        if(lineLen == 0)
            lineLen = 1;

        y2 += ((y1 - y2) / lineLen) * offset;
        x2 += ((x1 - x2) / lineLen) * offset;

        Color saveColor = g.getColor();
        g.setColor(Color.BLACK);
        g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
        g.setColor(saveColor);
	}
	
	
	
	private class CanvasKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println("InputHandlerGlassPane.CanvasKeyListener.keyPressed()");
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
				re.pan(0, move);
			else if (code == VK_DOWN)
				re.pan(0, -move);
			else if (code == VK_LEFT)
				re.pan(-move, 0);
			else if (code == VK_RIGHT)
				re.pan(move, 0);
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
		
	}

	
	private class CanvasMouseListener extends MouseAdapter {

		private AnnotationEdit resizeUndoEdit;
		private AnnotationEdit movingUndoEdit;
		private Point mousePressedPoint;
		
		@Override
		public void mousePressed(MouseEvent e) {
			System.out.println("InputHandlerGlassPane.CanvasMouseListener.mousePressed() " + e.paramString());
			requestFocusInWindow(); // need to do this to receive key events
			
			if(isSingleRightClick(e)) {
				// Assuming we're not handling a special annotation-specific context menu
				showContextMenu(e.getPoint());
				return;
			}
			
			// We only care about left mouse button
			if(!isSingleLeftClick(e)) {
				return;
			}
			
			if(isDragSelectionKeyDown(e)) {
				selectionRect = new Rectangle(e.getX(), e.getY(), 0, 0);
				changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			
			mousePressedPoint = e.getPoint();
			
			// Annotations are on the top canvas, so we check 
			
			boolean go = true; // MKTODO this is a hack!
			if(annotationSelectionEnabled()) {
				go = mousePressedHandleAnnotations(e);
			}
			if(go) {
				mousePressedHandleSingleLeftClick(e);
			}
			
			networkCanvas.repaint();
		}
		
		// MKTODO its a hack for now for this to return boolean
		private boolean mousePressedHandleAnnotations(MouseEvent e) {
			DingAnnotation annotation = cyAnnotator.getAnnotationAt(e.getPoint());
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			
			if (!annotationSelection.isEmpty() && annotationSelection.overAnchor(e.getX(), e.getY()) != null) {
				AnchorLocation anchor = annotationSelection.overAnchor(e.getX(), e.getY());
				
				// save the distance between the anchor location and the mouse location
				double offsetX = e.getX() - annotationSelection.getX() - anchor.getX();
				double offsetY = e.getY() - annotationSelection.getY() - anchor.getY();
				
				changeCursor(getResizeCursor(anchor.getPosition()));
				annotationSelection.setResizing(true);
				annotationSelection.saveAnchor(anchor.getPosition(), offsetX, offsetY);
				annotationSelection.saveBounds();
				
				for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) 
					a.saveBounds();
				
				resizeUndoEdit = new AnnotationEdit("Resize Annotation", cyAnnotator, registrar);
				return false;
			} else if (annotation == null) {
				if (e.isShiftDown()) {
					// Remember where we did the mouse down. We may be doing a sweep select
//					mouseDown = new Point2D.Double(e.getX(), e.getY());
				}
				// Let the InnerCanvas handle this event
				return true;
			} else {
				boolean selected = annotation.isSelected();
				if (selected && e.isShiftDown()) {
					annotation.setSelected(false);
				} else {
					if (!selected && !e.isPopupTrigger() && !e.isShiftDown() && !((e.isControlDown() || e.isMetaDown()) && !e.isAltDown()))
						cyAnnotator.clearSelectedAnnotations();
					annotation.setSelected(true);
				}

				if (!annotationSelection.isEmpty()) {
					changeCursor(moveCursor);
					annotationSelection.setMoving(true);
					movingUndoEdit = new AnnotationEdit("Move Annotation", cyAnnotator, registrar);
				} else {
					annotationSelection.setMoving(false);
				}

				//We request focus in this window, so that we can move these selected Annotations around using arrow keys
				// annotation.getCanvas().requestFocusInWindow();

				//Repaint the canvas
				annotation.getCanvas().repaint();	

				// OK, now for all of our selected annotations, remember this mousePressed
				for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) {
					a.setOffset(e.getPoint());
				}

				// Let the network canvas know -- NOTE: this messes up double-click for some reason.
				return true;

				//We request focus in this window, so that we can move these selected Annotations around using arrow keys
//				annotation.getCanvas().requestFocusInWindow();
			}
		}
		
		private void mousePressedHandleSingleLeftClick(MouseEvent e) {
			
			
			View<CyNode> node = null;
			View<CyEdge> edge = null;
			HandleKey handle = null;
			
			Toggle nodeSelected = Toggle.NOCHANGE;
			Toggle edgeSelected = Toggle.NOCHANGE;
			
			// MKTODO move getPickedXXX to InnerCanvas ???
			
			if(nodeSelectionEnabled())
				node = re.getPickedNodeView(e.getPoint());
			
			if(edgeSelectionEnabled() && node == null && checkLOD(GraphRenderer.LOD_EDGE_ANCHORS))
				handle = re.getPickedEdgeHandle(e.getPoint());

			if(edgeSelectionEnabled() && node == null && handle == null)
				edge = re.getPickedEdgeView(e.getPoint());

			if(node != null)
			    nodeSelected = networkCanvas.toggleSelectedNode(node, e);

			if(handle != null)
				// MKTODO this creates an undo edit, figure out what to do with it
				networkCanvas.toggleChosenAnchor(handle, e);

			if(edge != null)
				edgeSelected = networkCanvas.toggleSelectedEdge(edge, e);

			if((node != null || edge != null) && !(e.isShiftDown() || isControlOrMetaDown(e)))
				re.getBendStore().unselectAllHandles();
			
			
			if (node == null && edge == null && handle == null) {
				changeCursor(moveCursor);
				// Save all node positions for panning
				undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.NODES, "Move", registrar);
				// MKTODO what???
//				lod.setDrawEdges(false);
			} else {
				maybeDeselectAll(e, node, edge, handle);
				
				if(node != null) {
					if(nodeSelected == Toggle.SELECT)
						re.select(Collections.singletonList(node), CyNode.class, true);
					else if(nodeSelected == Toggle.DESELECT)
						re.select(Collections.singletonList(node), CyNode.class, false);
				}
				if(edge != null) {
					if(edgeSelected == Toggle.SELECT)
						re.select(Collections.singletonList(edge), CyEdge.class, true);
					else if(edgeSelected == Toggle.DESELECT)
						re.select(Collections.singletonList(edge), CyEdge.class, false);
				}
			}
		}
		
		
		
		@Override
		public void mouseReleased(MouseEvent e) {
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
			System.out.println("InputHandlerGlassPane.CanvasMouseListener.mouseReleased() " + e.paramString());
			if(!isSingleLeftClick(e)) // We only care about left mouse button
				return;
			
			boolean go = mouseReleasedHandleAnnotations(e);
			if(go) {
				mouseReleasedSingleLeftClick(e);
			}
			
			mousePressedPoint = null;
		}
		
		private boolean mouseReleasedHandleAnnotations(MouseEvent e) {
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			boolean resizing = annotationSelection.isResizing();
			annotationSelection.setResizing(false);
			annotationSelection.setMoving(false);

			if(resizeUndoEdit != null) 
				resizeUndoEdit.post();
			if(movingUndoEdit != null)
				movingUndoEdit.post();
			
			if (mousePressedPoint != null) {
				double startX = Math.min(mousePressedPoint.getX(), e.getX());
				double startY = Math.min(mousePressedPoint.getY(), e.getY());
				double endX   = Math.max(mousePressedPoint.getX(), e.getX());
				double endY   = Math.max(mousePressedPoint.getY(), e.getY());
				// Assume we did a sweep select
				Rectangle2D sweepArea = new Rectangle2D.Double(startX, startY, endX-startX, endY-startY);
				List<DingAnnotation> annotations = cyAnnotator.getAnnotationsIn(sweepArea);
				for (DingAnnotation a: annotations) {
					a.setSelected(true);
				}
				return false;
			}

			DingAnnotation annotation = cyAnnotator.getAnnotationAt(e.getPoint());
			if (annotationSelection.isEmpty() || !re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
				// Let the InnerCanvas handle this event
				return true;
			} else if (annotation != null) {
				// OK, now for all of our selected annotations, clear the mousePressed
				for (DingAnnotation a: annotationSelection) {
					a.setOffset(null);
				}
			} else if (!annotationSelection.isEmpty()) {
				// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
				if (!e.isPopupTrigger() && !e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()) && !resizing)
					cyAnnotator.clearSelectedAnnotations();
				
				return true;
			} else {
				return true;
			}
			return false;
		}
		
		
		private void mouseReleasedSingleLeftClick(MouseEvent e) {
			if(selectionRect != null) {
				List<View<CyNode>> selectedNodes = null;
				List<View<CyEdge>> selectedEdges = null;

				// MKTODO these methods should not be side effecting
				if(nodeSelectionEnabled())
					selectedNodes = networkCanvas.getAndApplySelectedNodes(selectionRect);	
				if(edgeSelectionEnabled())
					selectedEdges = networkCanvas.getAndApplySelectedEdges(selectionRect);
				
				if(selectedNodes != null && !selectedNodes.isEmpty())
					re.select(selectedNodes, CyNode.class, true);
				if(selectedEdges != null && !selectedEdges.isEmpty())
					re.select(selectedEdges, CyEdge.class, true);
				
				selectionRect = null;
			} 
			
			// why???
//			else if(draggingCanvas) {
//				setDraggingCanvas(false);
//				
//				if (undoableEdit != null)
//					undoableEdit.post();
//
//				re.setViewportChanged();
//			} else {
				// MKTODO why do this, isn't this handled in mousePressedSingleLeftClick???
//				long chosenNode = -1;
//				long chosenEdge = -1;
//				HandleKey chosenAnchor = null;
//				
//				if (re.isNodeSelectionEnabled())
//					chosenNode = getChosenNode();
//	
//				if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
//					chosenAnchor = getChosenAnchor();
//	
//				if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && (chosenAnchor == null))
//					chosenEdge = getChosenEdge();
//				
//				maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
//				
//				re.setContentChanged();
//			}
	
			networkCanvas.repaint();
			if(undoableEdit != null)
				undoableEdit.post();
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// DONE probably
			if(cyAnnotator.getResizeShape() != null) {
				cyAnnotator.getResizeShape().contentChanged();
				cyAnnotator.resizeShape(null);
				cyAnnotator.postUndoEdit(); // markUndoEdit() is in the dialogs like ShapeAnnotationDialog
				return;
			}
			if(cyAnnotator.getRepositioningArrow() != null) {
				cyAnnotator.getRepositioningArrow().contentChanged();
				cyAnnotator.positionArrow(null);
				cyAnnotator.postUndoEdit(); // markUndoEdit() is in ArrowAnnotationDialog
				return;
			}

			if(annotationSelectionEnabled()) {
				DingAnnotation annotation = cyAnnotator.getAnnotationAt(e.getPoint());
				if(annotation != null && isDoubleLeftClick(e)) {
					editAnnotation(annotation, e.getPoint());
				}
				if(annotation == null && (!e.isPopupTrigger() && !e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()))) {
					cyAnnotator.clearSelectedAnnotations();
				}
			}
		}
		
		private void editAnnotation(DingAnnotation annotation, Point p) {
			invokeOnEDT(() -> {
				EditAnnotationTaskFactory tf = new EditAnnotationTaskFactory(registrar.getService(DingRenderer.class));
				DialogTaskManager dtm = cyAnnotator.getRegistrar().getService(DialogTaskManager.class);
				dtm.execute(tf.createTaskIterator(re.getViewModel(), annotation, p));
			});
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e) {
			System.out.println("InputHandlerGlassPane.CanvasMouseListener.mouseDragged() " + e.paramString());
			if(e.getButton() != MouseEvent.BUTTON1)
				return;
			
			boolean go = mouseDraggedHandleAnnotations(e);
			if(go) {
				mouseDraggedHandleSingleLeftClick(e);
			}
		}

		private boolean mouseDraggedHandleAnnotations(MouseEvent e) {
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			DingAnnotation a = cyAnnotator.getAnnotationAt(new Point(e.getX(), e.getY()));
			
			if (annotationSelection.isEmpty() || !re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
				return true;
			}

			if (annotationSelection.isResizing()) {
				// Resize
				annotationSelection.resizeAnnotationsRelative(e.getX(), e.getY());
				// For resize, we *don't* want to pass things to the network canvas
			} else if (a != null && !annotationSelection.isMoving()) {
				// cyAnnotator.moveAnnotation(a);
				// annotationSelection.moveSelection(e.getX(), e.getY());
				// If we're moving, we might have nodes or edges selected and will want to move them also
				// if (!view.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty())
					return true;
			} else if (a != null) {
				annotationSelection.moveSelection(e.getX(), e.getY());
				// If we're moving, we might have nodes or edges selected and will want to move them also
				if (!re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES).isEmpty()) //.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty())
					return true;
			} else if (annotationSelection.isMoving()) {
				annotationSelection.moveSelection(e.getX(), e.getY());
				// If we're moving, we might have nodes or edges selected and will want to move them also
				if (!re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES).isEmpty()) { //(!view.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty()) {
					return true;
				}
			} else {
				return true;
			}
			return false;
		}

		
		private void mouseDraggedHandleSingleLeftClick(MouseEvent e) {
			if(!isSingleLeftClick(e))
				return;
			
			if (!isDragSelectionKeyDown(e)) {
				double deltaX = e.getX() - mousePressedPoint.getX();
				double deltaY = e.getY() - mousePressedPoint.getY();
				mousePressedPoint = e.getPoint();
				re.pan(deltaX, deltaY);
				
			} else {
				if(undoableEdit == null)
					undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED, "Move", registrar);
				
				double[] ptBuff = {mousePressedPoint.getX(), mousePressedPoint.getY()};
				re.xformComponentToNodeCoords(ptBuff);
				final double oldX = ptBuff[0];
				final double oldY = ptBuff[1];
				mousePressedPoint = e.getPoint();
				ptBuff[0] = mousePressedPoint.getX();
				ptBuff[1] = mousePressedPoint.getY();
				re.xformComponentToNodeCoords(ptBuff);

				final double newX = ptBuff[0];
				final double newY = ptBuff[1];
				double deltaX = newX - oldX;
				double deltaY = newY - oldY;

				// If the shift key is down, then only move horizontally, vertically, or diagonally, depending on the slope.
				if (e.isShiftDown()) {
					final double slope = deltaY / deltaX;

					// slope of 2.41 ~ 67.5 degrees (halfway between 45 and 90)
					// slope of 0.41 ~ 22.5 degrees (halfway between 0 and 45)
					if ((slope > 2.41) || (slope < -2.41)) {
						deltaX = 0.0; // just move vertical
					} else if ((slope < 0.41) && (slope > -0.41)) {
						deltaY = 0.0; // just move horizontal
					} else {
						final double avg = (Math.abs(deltaX) + Math.abs(deltaY)) / 2.0;
						deltaX = (deltaX < 0) ? (-avg) : avg;
						deltaY = (deltaY < 0) ? (-avg) : avg;
					}
				}

				Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
				
				// MKTODO rename to 'handlesToMove'
				Set<HandleKey> anchorsToMove = re.getBendStore().getSelectedHandles();
				
				if (anchorsToMove.isEmpty()) { // If we are moving anchors of edges, no need to move nodes (bug #2360).
					for (View<CyNode> node : selectedNodes) {
						View<CyNode> mutableNode = re.getViewModel().getNodeView(node.getSUID());
						if(mutableNode != null) {
							NodeDetails nodeDetails = re.getNodeDetails();
							double oldXPos = nodeDetails.getXPosition(mutableNode);
							double oldYPos = nodeDetails.getYPosition(mutableNode);
							// MKTODO Should setting VPs be done using NodeDetails as well??
							mutableNode.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, oldXPos + deltaX);
							mutableNode.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, oldYPos + deltaY);
						}
				    }
				} else {
					for (HandleKey handleKey : anchorsToMove) {
						View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());

						if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
							Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
							View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getSUID());
							if(mutableEdgeView != null) {
								if( ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend ) {
									if( defaultBend instanceof BendImpl )
										mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
									else
										mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
								} else {
									Bend bend = re.getEdgeDetails().getBend(ev, true);
									mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
								}
							}
						}
						final Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
						//TODO: Refactor to fix this ordering problem.
						//This test is necessary because in some instances, an anchor can still be present in the selected
						//anchor list, even though the anchor has been removed. A better fix would be to remove the
						//anchor from that list before this code is ever reached. However, this is not currently possible
						//under the present API, so for now we just detect this situation and continue.
						if( bend.getAllHandles().isEmpty() )
							continue;
						final Handle handle = bend.getAllHandles().get(handleKey.getHandleIndex());
						final Point2D newPoint = handle.calculateHandleLocation(re.getViewModelSnapshot(), ev);
						
						float x = (float) newPoint.getX();
						float y = (float) newPoint.getY();
						
						re.getBendStore().moveHandle(handleKey, x + (float)deltaX, y + (float)deltaY);
					}
				}
				
				if (!selectedNodes.isEmpty() || !re.getBendStore().getSelectedHandles().isEmpty()) {
					re.setContentChanged();
				}
				if (!selectedNodes.isEmpty() && re.getBendStore().getSelectedHandles().isEmpty()) {
					networkCanvas.setHideEdges();
				}
			}
	
			if (selectionRect != null) {
				int x = Math.min(mousePressedPoint.x, e.getX());
				int y = Math.min(mousePressedPoint.y, e.getY());
				int w = Math.abs(mousePressedPoint.x - e.getX());
				int h = Math.abs(mousePressedPoint.y - e.getY());
				selectionRect.setBounds(x, y, w, h);
			}

			networkCanvas.repaint();
		}
		
		
		@Override
		public void mouseMoved(MouseEvent e) {
//			System.out.println("InputHandlerGlassPane.CanvasMouseListener.mouseMoved() " + e.paramString());
			boolean go = mouseMovedHandleAnnotations(e);
			if(go) {
				
			}
		}
		
		private boolean mouseMovedHandleAnnotations(MouseEvent e) {
			AbstractAnnotation resizeAnnotation = cyAnnotator.getResizeShape();
			// DingAnnotation moveAnnotation = cyAnnotator.getMovingAnnotation();
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			ArrowAnnotationImpl repositionAnnotation = cyAnnotator.getRepositioningArrow();
			
			if (resizeAnnotation == null && annotationSelection.isEmpty() && repositionAnnotation == null) {
				return true;
			}

			int mouseX = e.getX();
			int mouseY = e.getY();

			if (resizeAnnotation != null) {
				Rectangle2D initialBounds = cyAnnotator.getResizeBounds();
				Rectangle2D bounds = AnnotationSelection.resize(Position.SOUTH_EAST, initialBounds, mouseX, mouseY);
				// must call setLocation and setSize instead of setBounds because those methods are overridden
				resizeAnnotation.setLocation((int)bounds.getX(), (int)bounds.getY());
				resizeAnnotation.resizeAnnotation(bounds.getWidth(), bounds.getHeight());
				resizeAnnotation.update();
				resizeAnnotation.getCanvas().repaint();
			} else if (repositionAnnotation != null) {
				Point2D mousePoint = new Point2D.Double(mouseX, mouseY);

				// See what's under our mouse
				// Annotation?
				List<DingAnnotation> annotations = cyAnnotator.getAnnotationsAt(mousePoint);
				if (annotations.contains(repositionAnnotation))
					annotations.remove(repositionAnnotation);

				if (annotations.size() > 0) {
					repositionAnnotation.setTarget(annotations.get(0));

				// Node?
				} else if (overNode(mousePoint)) {
					CyNode overNode = getNodeAtLocation(mousePoint);
					repositionAnnotation.setTarget(overNode);

				// Nope, just set the point
				} else {
					repositionAnnotation.setTarget(mousePoint);
				}

				repositionAnnotation.update();
				repositionAnnotation.getCanvas().repaint();
			}
			return false;
		}
		
	}
	
	
	@Override
	public void processMouseEvent(MouseEvent e) {
		// expose processMouseEvent so that the birds-eye-view can pass mouse events here.
		super.processMouseEvent(e);
	}
	
	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		// expose processMouseWheelEvent so that the birds-eye-view can pass mouse wheel events here.
		super.processMouseWheelEvent(e);
	}
	
	
	private void showContextMenu(Point p) {
		// Node menu
		View<CyNode> nodeView = re.getPickedNodeView(p);
		if(nodeView != null) {
			popupMenuHelper.createNodeViewMenu(nodeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
			return;
		}
		// Edge menu
		View<CyEdge> edgeView = re.getPickedEdgeView(p);
		if(edgeView != null) {
			popupMenuHelper.createEdgeViewMenu(edgeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
			return;
		}
		// Network canvas menu
		double[] loc = { p.getX(), p.getY() };
		re.xformComponentToNodeCoords(loc);
		Point xformP = new Point();
		xformP.setLocation(loc[0], loc[1]); 
		popupMenuHelper.createNetworkViewMenu(p, xformP, PopupMenuHelper.ACTION_NEW);
	}
	
	
	// Utility methods below
	
	private static boolean isLeftClick(MouseEvent e) {
		boolean b = e.getButton() == MouseEvent.BUTTON1;
		if(LookAndFeelUtil.isMac()) {
			return !e.isControlDown() && b;
		}
		return b;
	}

	private static boolean isRightClick(MouseEvent e) {
		boolean b = e.getButton() == MouseEvent.BUTTON3; 
		if(!b && LookAndFeelUtil.isMac()) {
			// control - right click
			return e.isControlDown() && !e.isMetaDown() && (e.getButton() == MouseEvent.BUTTON1);
		}
		return b;
	}

	private static boolean isMiddleClick(MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON2; 
	}
	
	private static boolean isSingleClick(MouseEvent e) {
		return e.getClickCount() == 1;
	}
	
	private static boolean isDoubleClick(MouseEvent e) {
		return e.getClickCount() == 2;
	}
	
	private static boolean isSingleLeftClick(MouseEvent e) {
		return isLeftClick(e) && isSingleClick(e);
	}
	
	private static boolean isSingleRightClick(MouseEvent e) {
		return isRightClick(e) && isSingleClick(e);
	}
	
	private static boolean isDoubleLeftClick(MouseEvent e) {
		return isLeftClick(e) && isDoubleClick(e);
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
	
	
	private boolean checkLOD(int flag) {
		return (networkCanvas.getLastRenderDetail() & flag) != 0;
	}

	private boolean overNode(Point2D mousePoint) {
		return re.getPickedNodeView(mousePoint) != null;
	}

	private CyNode getNodeAtLocation(Point2D mousePoint) {
		return re.getPickedNodeView(mousePoint).getModel();
	}
	
	
	private void maybeDeselectAll(MouseEvent e, View<CyNode> chosenNode, View<CyEdge> chosenEdge, HandleKey chosenAnchor) {
		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if ((!e.isShiftDown() && !(e.isControlDown() && !e.isAltDown()) && !e.isMetaDown()) // If shift is down never unselect.
		    && ((chosenNode == null && chosenEdge == null && chosenAnchor == null) // Mouse missed all.
		       // Not [we hit something but it was already selected].
		       || !(((chosenNode != null) && re.isNodeSelected(chosenNode)) || (chosenAnchor != null) || ((chosenEdge != null) && re.isEdgeSelected(chosenEdge)) ))) {
			
			Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
			Collection<View<CyEdge>> selectedEdges = re.getViewModelSnapshot().getTrackedEdges(CyNetworkViewConfig.SELECTED_EDGES);
			// de-select
			re.select(selectedNodes, CyNode.class, false);
			re.select(selectedEdges, CyEdge.class, false);
		}
	}
	
	private void changeCursor(Cursor cursor) {
		String componentName = "__CyNetworkView_" + re.getViewModel().getSUID(); // see ViewUtil.createUniqueKey(CyNetworkView)
		Container parent = this;
		while(parent != null) {
			if(componentName.equals(parent.getName())) {
				parent.setCursor(cursor);
				break;
			}
			parent = parent.getParent();
		}
	}
	
	private Cursor createMoveCursor() {
		Cursor moveCursor;
		if (LookAndFeelUtil.isMac()) {
			Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(24, 24);
			Image image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			Graphics graphics = image.getGraphics();

			String icon = IconManager.ICON_ARROWS;
			JLabel label = new JLabel();
			label.setBounds(0, 0, size.width, size.height);
			label.setText(icon);
			label.setFont(registrar.getService(IconManager.class).getIconFont(14));
			label.paint(graphics);
			graphics.dispose();
			moveCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "custom:" + (int) icon.charAt(0));
		} else {
			moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
			if(moveCursor == null) {
				moveCursor = new Cursor(Cursor.MOVE_CURSOR);
			}
		}
		return moveCursor;
	}
	
	private static Cursor getResizeCursor(Position anchor) {
		switch(anchor) {
			case NORTH_EAST: return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
			case NORTH:      return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			case NORTH_WEST: return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
			case WEST:       return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			case SOUTH_WEST: return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
			case SOUTH:      return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			case SOUTH_EAST: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			case EAST:       return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			default:         return null;
		}
	}
	
}
