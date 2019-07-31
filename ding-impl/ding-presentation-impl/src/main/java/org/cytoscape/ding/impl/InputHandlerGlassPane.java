package org.cytoscape.ding.impl;

import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import static org.cytoscape.ding.internal.util.ViewUtil.getResizeCursor;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.ding.internal.util.ViewUtil.isAdditiveSelect;
import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;
import static org.cytoscape.ding.internal.util.ViewUtil.isDoubleLeftClick;
import static org.cytoscape.ding.internal.util.ViewUtil.isDragSelectionKeyDown;
import static org.cytoscape.ding.internal.util.ViewUtil.isLeftClick;
import static org.cytoscape.ding.internal.util.ViewUtil.isSingleLeftClick;
import static org.cytoscape.ding.internal.util.ViewUtil.isSingleRightClick;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnchorLocation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTask;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.ding.impl.undo.AnnotationEdit;
import org.cytoscape.ding.impl.undo.CompositeCyEdit;
import org.cytoscape.ding.impl.undo.ViewChangeEdit;
import org.cytoscape.ding.internal.util.CoalesceTimer;
import org.cytoscape.ding.internal.util.OrderedMouseAdapter;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class InputHandlerGlassPane extends JComponent {
	
	private final CyServiceRegistrar registrar;
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;
	private final OrderedMouseAdapter orderedMouseAdapter;
	
	public InputHandlerGlassPane(CyServiceRegistrar registrar, DRenderingEngine re) {
		this.registrar = registrar;
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		
		// Order matters, some listeners use MouseEvent.consume() to prevent subsequent listeners from running
		this.orderedMouseAdapter = new OrderedMouseAdapter(
        	new FocusRequestListener(),
        	new ContextMenuListener(),
        	new DoubleClickListener(),
        	new AddEdgeListener(),
        	new TooltipListener(),
        	new SelecionClickAndDragListener(),
        	new AddAnnotationListener(),
        	new SelectionLassoListener(),
        	new SelectionRectangleListener(),
        	new PanListener() // panning only happens if no node/edge/annotation/handle is selected, so it needs to go after
        );
        
		addMouseListener(orderedMouseAdapter);
		addMouseMotionListener(orderedMouseAdapter);
		
		addKeyListener(new CanvasKeyListener());
        addMouseWheelListener(new CanvasMouseWheelListener());
        
        setFocusable(true); // key listener needs the focus
	}
	
	private <T> T get(Class<T> t) {
		return orderedMouseAdapter.get(t);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		get(AddEdgeListener.class).drawAddingEdge(g);
		get(SelectionRectangleListener.class).drawSelectionRectangle(g);
		get(SelectionLassoListener.class).drawSelectionLasso(g);
	}
	
	@Override
	public void processMouseEvent(MouseEvent e) {
		// expose so the birds-eye-view can pass mouse events here.
		super.processMouseEvent(e);
	}
	
	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		// expose so the birds-eye-view can pass mouse wheel events here.
		super.processMouseWheelEvent(e);
	}
	
	// Called by the context menu Task, see AddEdgeBeginTask 
	public void beginAddingEdge(View<CyNode> nodeView) {
		get(AddEdgeListener.class).beginAddingEdge(nodeView);
	}

	// Called by the Annotation panel
	public void beginClickToAddAnnotation(AnnotationFactory<? extends Annotation> annotationFactory, Runnable mousePressedCallback)	 {
		get(AddAnnotationListener.class).beginClickToAddAnnotation(annotationFactory, mousePressedCallback);
	}
	
	
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
			
			re.updateView();
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
			
			for(DingAnnotation a : cyAnnotator.getAnnotationSelection()) {
				double[] coords = {a.getX(), a.getY()};
				re.getTransform().xformNodeToImageCoords(coords);
				
				if(code == VK_UP)
					coords[1] -= move;
				else if(code == VK_DOWN)
					coords[1] += move;
				else if(code == VK_LEFT)
					coords[0] -= move;
				else if(code == VK_RIGHT)
					coords[0] += move;

				re.getTransform().xformImageToNodeCoords(coords);
				a.setLocation(coords[0], coords[1]);
				
				a.update();
				re.updateView();
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
			get(AddEdgeListener.class).reset();
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
			re.zoom(e.getWheelRotation());
		}
	}

	
	private class FocusRequestListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			requestFocusInWindow(); // need to do this to receive key events
		}
	}
	
	
	private class ContextMenuListener extends MouseAdapter {
		
		private final PopupMenuHelper popupMenuHelper = new PopupMenuHelper(re, InputHandlerGlassPane.this, registrar);
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(isSingleRightClick(e)) {
				showContextMenu(e.getPoint());
				e.consume();
			}
		}
		
		private void showContextMenu(Point p) {
			// Node menu
			View<CyNode> nodeView = re.getPicker().getNodeAt(p);
			if(nodeView != null) {
				popupMenuHelper.createNodeViewMenu(nodeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
				return;
			}
			// Edge menu
			View<CyEdge> edgeView = re.getPicker().getEdgeAt(p);
			if(edgeView != null) {
				popupMenuHelper.createEdgeViewMenu(edgeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
				return;
			}
			// Network canvas menu
			double[] loc = { p.getX(), p.getY() };
			re.getTransform().xformImageToNodeCoords(loc);
			Point xformP = new Point();
			xformP.setLocation(loc[0], loc[1]); 
			popupMenuHelper.createNetworkViewMenu(p, xformP, PopupMenuHelper.ACTION_NEW);
		}
	}


	private class DoubleClickListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(annotationSelectionEnabled() && isDoubleLeftClick(e)) {
				DingAnnotation annotation = re.getPicker().getAnnotationAt(e.getPoint());
				if(annotation != null) {
					editAnnotation(annotation, e.getPoint());
				}
			}
		}
		
		private void editAnnotation(DingAnnotation annotation, Point p) {
			invokeOnEDT(() -> {
				EditAnnotationTaskFactory taskFactory = new EditAnnotationTaskFactory(registrar.getService(DingRenderer.class));
				DialogTaskManager tm = registrar.getService(DialogTaskManager.class);
				tm.execute(taskFactory.createTaskIterator(re.getViewModel(), annotation, p));
			});
		}
	}
	
	
	private class AddEdgeListener extends MouseAdapter {
		
		private final double[] coords = new double[2];
		
		// Store the start and end points in node coordinates because they
		// don't need to be adjusted when the user zooms or pans the canvas.
		private View<CyNode> sourceNode;
		private Point2D startPoint;
		private Point2D endPoint;
		
		public void beginAddingEdge(View<CyNode> nodeView) {
			this.sourceNode = nodeView;
			double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			this.startPoint = new Point2D.Double(x, y);
			this.endPoint   = new Point2D.Double(x, y);
		}
		
		// Called by the key listener when user hits escape
		public void reset() {
			this.sourceNode = null;
			this.startPoint = null;
			this.endPoint = null;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(endPoint != null) {
				Point2D mousePoint = e.getPoint();
				coords[0] = mousePoint.getX();
				coords[1] = mousePoint.getY();
				re.getTransform().xformImageToNodeCoords(coords);
				endPoint.setLocation(coords[0], coords[1]);
				repaint();
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(startPoint == null || endPoint == null)
				return;
			coords[0] = endPoint.getX();
			coords[1] = endPoint.getY();
			re.getTransform().xformNodeToImageCoords(coords);
			Point2D ep = new Point2D.Double(coords[0], coords[1]);

			View<CyNode> targetNode = re.getPicker().getNodeAt(ep);
			if(targetNode != null) {
				createEdge(sourceNode, targetNode);
				reset();
				e.consume();
			}
		}
		
		public void drawAddingEdge(Graphics graphics) {
			if(startPoint == null || endPoint == null)
				return;

			coords[0] = startPoint.getX();
			coords[1] = startPoint.getY();
			re.getTransform().xformNodeToImageCoords(coords);
	        double x1 = coords[0];
	        double y1 = coords[1];
	        
	        coords[0] = endPoint.getX();
			coords[1] = endPoint.getY();
			re.getTransform().xformNodeToImageCoords(coords);
	        double x2 = coords[0];
	        double y2 = coords[1];
	        
	        double offset = 5;
	        
	        double lineLen = Math.sqrt((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
	        if(lineLen == 0)
	            lineLen = 1;

	        y2 += ((y1 - y2) / lineLen) * offset;
	        x2 += ((x1 - x2) / lineLen) * offset;

	        Graphics2D g = (Graphics2D) graphics.create();
	        g.setColor(Color.BLACK);
	        g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
		}
		
		private void createEdge(View<CyNode> sourceNodeView, View<CyNode> targetNodeView) {
			CyNetworkView netView = re.getViewModel();
			AddEdgeTask addEdgeTask = new AddEdgeTask(registrar, netView, sourceNodeView, targetNodeView);
			DialogTaskManager taskManager = registrar.getService(DialogTaskManager.class);
			taskManager.execute(new TaskIterator(addEdgeTask));
		}
	}
	
	
	private class TooltipListener extends MouseAdapter {
		
		private CoalesceTimer delayTimer = new CoalesceTimer(60, 1);
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// This event gets called a lot as the user moves the mouse over the canvas.
			// Use a CoalesceTimer to debounce the event to avoid calling getPickedNodeView() constantly.
			delayTimer.coalesce(() -> showTooltip(e));
		}
		
		private void showTooltip(MouseEvent e) {
			View<CyNode> node = re.getPicker().getNodeAt(e.getPoint());
			String text = node == null ? null : re.getNodeDetails().getTooltipText(node);
			ViewUtil.invokeOnEDT(() -> {
				setToolTipText(text);
				ToolTipManager.sharedInstance().mouseMoved(e);
			});
		}
	}
	
	
	private class SelecionClickAndDragListener extends MouseAdapter {

		private Point mousePressedPoint;
		private boolean deselectAllOnRelease;
		private boolean hit;
		
		private AnnotationEdit annotationResizeEdit;
		private AnnotationEdit annotationMovingEdit;
		private ViewChangeEdit removeHandleEdit;
		private ViewChangeEdit addHandleEdit;
		private ViewChangeEdit moveNodesEdit;
		
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(!isSingleLeftClick(e))
				return;
			
			annotationResizeEdit = null;
			annotationMovingEdit = null;
			removeHandleEdit = null;
			addHandleEdit = null;
			moveNodesEdit = null;
			
			deselectAllOnRelease = false;
			mousePressedPoint = e.getPoint();
			
			hit = mousePressedCheckHit(e);
			if(hit) {
				re.updateView();
				e.consume(); // no selection rectangle or lasso
			} else if(!isAdditiveSelect(e)) {
				deselectAllOnRelease = true;
			}
		}
		
		private boolean mousePressedCheckHit(MouseEvent e) {
			NetworkPicker picker = re.getPicker();
			
			if(annotationSelectionEnabled()) {
				AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
				annotationSelection.setOffset(e.getPoint());
				
				AnchorLocation anchor = annotationSelection.overAnchor(e.getX(), e.getY());
				if(!annotationSelection.isEmpty() && anchor != null) {
					mousePressedHandleAnnotationAnchor(anchor, e);
					return true;
				}
				
				DingAnnotation annotation = picker.getAnnotationAt(CanvasID.FOREGROUND, e.getPoint());
				if(annotation != null) {
					Toggle select = mousePressedHandleAnnotation(annotation, e);
					if(select != Toggle.NOCHANGE && !isAdditiveSelect(e)) {
						deselectAllNodesAndEdges();
					}
					annotationSelection.setOffset(e.getPoint());
					return true;
				}
			}
			
			if(nodeSelectionEnabled()) {
				View<CyNode> node = picker.getNodeAt(e.getPoint());
				if(node != null) {
					Toggle select = toggleNodeSelection(node, e);
					if(select != Toggle.NOCHANGE && !isAdditiveSelect(e)) {
						deselectAll();
					}
					toggleSelection(node, CyNode.class, select);
					return true;
				}
			}
			
			if(edgeSelectionEnabled() && isLODEnabled(RenderDetailFlags.LOD_EDGE_ANCHORS)) {
				HandleKey handle = picker.getHandleAt(e.getPoint());
				if(handle != null) {
					toggleChosenAnchor(handle, e);
					return true;
				}
			}
			
			if(edgeSelectionEnabled()) {
				View<CyEdge> edge = picker.getEdgeAt(e.getPoint());
				if(edge != null) {
					if(e.isAltDown() && isLODEnabled(RenderDetailFlags.LOD_EDGE_ANCHORS))
						addHandle(edge, e);
					Toggle select = toggleSelectedEdge(edge, e);
					if(select != Toggle.NOCHANGE && !isAdditiveSelect(e)) {
						deselectAll();
					}
					toggleSelection(edge, CyEdge.class, select);
					return true;
				}
			}
			
			if(annotationSelectionEnabled()) {
				DingAnnotation annotation = picker.getAnnotationAt(CanvasID.BACKGROUND, e.getPoint());
				if(annotation != null) {
					mousePressedHandleAnnotation(annotation, e);
					return true;
				}
			}
			
			// Missed everything
			return false;
		}
		
		private void mousePressedHandleAnnotationAnchor(AnchorLocation anchor, MouseEvent e) {
			annotationResizeEdit = new AnnotationEdit("Resize Annotation", cyAnnotator, registrar);
			
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			// save the distance between the anchor location and the mouse location
			double offsetX = e.getX() - annotationSelection.getX() - anchor.getX();
			double offsetY = e.getY() - annotationSelection.getY() - anchor.getY();
			
			changeCursor(getResizeCursor(anchor.getPosition()));
			
			annotationSelection.setResizing(true);
			annotationSelection.saveAnchor(anchor.getPosition(), offsetX, offsetY);
			annotationSelection.saveBounds();
		}
		
		private Toggle mousePressedHandleAnnotation(DingAnnotation annotation, MouseEvent e) {
			Toggle toggle = Toggle.NOCHANGE;
			boolean wasSelected = annotation.isSelected();
			if(wasSelected && isAdditiveSelect(e)) {
				annotation.setSelected(false);
				toggle = Toggle.DESELECT;
			} else {
				if(!wasSelected && !isAdditiveSelect(e)) {
					cyAnnotator.clearSelectedAnnotations();
				}
				if(!wasSelected) {
					annotation.setSelected(true);
					toggle = Toggle.SELECT;
				}
			}

			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			if(!annotationSelection.isEmpty()) {
				annotationMovingEdit = new AnnotationEdit("Move Annotation", cyAnnotator, registrar);
			} 

			if(toggle != Toggle.NOCHANGE)
				re.updateView();
			
			return toggle;
		}
		
		private Toggle toggleNodeSelection(View<CyNode> nodeView, MouseEvent e) {
			boolean wasSelected = re.getNodeDetails().isSelected(nodeView);
			if(wasSelected && isAdditiveSelect(e)) {
				return Toggle.DESELECT;
			} else if(!wasSelected) {
				return Toggle.SELECT;
			}
			return Toggle.NOCHANGE;
		}
		
		private void toggleChosenAnchor(HandleKey chosenAnchor, MouseEvent e) {
			final long edge = chosenAnchor.getEdgeSuid();
			View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(edge);
			
			// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
			if(e.isAltDown()) { // Remove handle
				removeHandleEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Remove Edge Handle", registrar);
				setBendAsLockedValue(ev);
				re.getBendStore().removeHandle(chosenAnchor);
			} else {
				boolean selected = re.getBendStore().isHandleSelected(chosenAnchor);
				// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
				if(selected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
					re.getBendStore().unselectHandle(chosenAnchor);
				} else if(!selected) {
					if(!e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown())) {
						re.getBendStore().unselectAllHandles();
					}
					re.getBendStore().selectHandle(chosenAnchor);
				}
			}
			re.setContentChanged();	
		}
		
		private void setBendAsLockedValue(View<CyEdge> ev) {
			Bend bend = null;
			if(!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
				Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
				if(re.getEdgeDetails().getBend(ev) == defaultBend) {
					bend = new BendImpl((BendImpl) defaultBend);
				} else {
					bend = new BendImpl((BendImpl) re.getEdgeDetails().getBend(ev));
				}
			}
			if(bend != null) {
				View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getModel());
				if(mutableEdgeView != null) {
					mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);
				}
			}
		}
		
		private Toggle toggleSelectedEdge(View<CyEdge> edgeView, MouseEvent e) {
			boolean wasSelected = re.getEdgeDetails().isSelected(edgeView);
			Toggle toggle = Toggle.NOCHANGE;
			// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
			if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
				toggle = Toggle.DESELECT;
			} else if (!wasSelected) {
				toggle = Toggle.SELECT;

				if (isLODEnabled(RenderDetailFlags.LOD_EDGE_ANCHORS)) {
					double[] ptBuff = {e.getX(), e.getY()};
					re.getTransform().xformImageToNodeCoords(ptBuff);
					HandleKey hit = re.getBendStore().pickHandle((float) ptBuff[0], (float) ptBuff[1]);
					if(hit != null) {
						re.getBendStore().selectHandle(hit);
					}
				}
			}

			re.setContentChanged();
			return toggle;
		}
		
		private void addHandle(View<CyEdge> edgeView, MouseEvent e) {
			re.getBendStore().unselectAllHandles();
			double[] ptBuff = {e.getX(), e.getY()};
			re.getTransform().xformImageToNodeCoords(ptBuff);
			// Store current handle list
			addHandleEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", registrar);
			
			Point2D newHandlePoint = new Point2D.Float((float) ptBuff[0], (float) ptBuff[1]);
			Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
			
			if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
				View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(edgeView.getSUID());
				if(mutableEdgeView != null) {
					if (defaultBend instanceof BendImpl)
						mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl) defaultBend));
					else
						mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
				}
			}
			
			HandleKey handleKey = re.getBendStore().addHandle(edgeView, newHandlePoint);
			re.getBendStore().selectHandle(handleKey);
		}
		
		
		@Override
		public void mouseReleased(MouseEvent e) {
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(!isLeftClick(e)) // We only care about left mouse button
				return;
			
			if(annotationSelectionEnabled()) {
				AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
				annotationSelection.setResizing(false);
				annotationSelection.setOffset(null);
			}
			
			mousePressedPoint = null;
			re.updateView();
			
			if(annotationMovingEdit != null && moveNodesEdit != null) {
				CompositeCyEdit compositeEdit = new CompositeCyEdit("Move", registrar);
				compositeEdit.add(annotationMovingEdit, moveNodesEdit);
				compositeEdit.post();
			} else if(annotationResizeEdit != null) {
				annotationResizeEdit.post();
			} else if(annotationMovingEdit != null) {
				annotationMovingEdit.post();
			} else if(removeHandleEdit != null) {
				removeHandleEdit.post();
			} else if(addHandleEdit != null) {
				addHandleEdit.post();
			} else if(moveNodesEdit != null) {
				moveNodesEdit.post();
			}
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// this method does not fire if there was a drag
			if(!isSingleLeftClick(e)) // We only care about left mouse button
				return;
			if(deselectAllOnRelease) {
				deselectAll();
			}
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(!hit || !isSingleLeftClick(e))
				return;
			if(get(SelectionLassoListener.class).isDragging() || get(SelectionRectangleListener.class).isDragging())
				return;
			
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			if(!annotationSelection.isEmpty()) {
				if(annotationSelection.isResizing()) {
					annotationSelection.resizeAnnotationsRelative(e.getX(), e.getY());
					return;
				} else {
					annotationSelection.moveSelection(e.getX(), e.getY());
					annotationSelection.setOffset(e.getPoint());
				}
			}
			
			mouseDraggedHandleNodesAndEdges(e);
			re.updateView();
		}

		private void mouseDraggedHandleNodesAndEdges(MouseEvent e) {
			if(moveNodesEdit == null)
				moveNodesEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED, "Move", registrar);
			
			double[] ptBuff = {mousePressedPoint.getX(), mousePressedPoint.getY()};
			re.getTransform().xformImageToNodeCoords(ptBuff);
			double oldX = ptBuff[0];
			double oldY = ptBuff[1];
			mousePressedPoint = e.getPoint();
			ptBuff[0] = mousePressedPoint.getX();
			ptBuff[1] = mousePressedPoint.getY();
			re.getTransform().xformImageToNodeCoords(ptBuff);
			double newX = ptBuff[0];
			double newY = ptBuff[1];
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
//			if (!selectedNodes.isEmpty() && re.getBendStore().getSelectedHandles().isEmpty()) {
//				networkCanvas.setHideEdges();
//			}
		}
	}
	
	
	private class AddAnnotationListener extends MouseAdapter {
		
		private AnnotationFactory<?> annotationFactory = null;
		private Runnable mousePressedCallback = null;
		
		public void beginClickToAddAnnotation(AnnotationFactory<? extends Annotation> annotationFactory, Runnable mousePressedCallback) {
			this.annotationFactory = annotationFactory;
			this.mousePressedCallback = mousePressedCallback;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(annotationFactory != null && isSingleLeftClick(e)) {
				if(mousePressedCallback != null) {
					mousePressedCallback.run();
				}
				createAnnotation(annotationFactory, e.getPoint());
			}
			annotationFactory = null;
			mousePressedCallback = null;
		}
		
		private void createAnnotation(AnnotationFactory<? extends Annotation> f, Point point) {
			if(!(f instanceof AbstractDingAnnotationFactory))  // For now, only DING annotations are supported!
				return;
			TaskIterator iterator = new TaskIterator(new AddAnnotationTask(re, point, f));
			registrar.getService(DialogTaskManager.class).execute(iterator);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// This handles when you first add an annotation to the canvas and it auto-resizes
			// This operation is initiated by the various annotation dialogs
			
			AbstractAnnotation resizeAnnotation = cyAnnotator.getResizeShape();
			AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
			ArrowAnnotationImpl repositionAnnotation = cyAnnotator.getRepositioningArrow();
			
			if (resizeAnnotation == null && annotationSelection.isEmpty() && repositionAnnotation == null) {
				return;
			}

			int mouseX = e.getX();
			int mouseY = e.getY();

			if (resizeAnnotation != null) {
				Rectangle2D initialBounds = cyAnnotator.getResizeBounds();
				Rectangle2D bounds = AnnotationSelection.resize(Position.SOUTH_EAST, initialBounds, mouseX, mouseY);
				// must call setLocation and setSize instead of setBounds because those methods are overridden
				resizeAnnotation.setLocation((int)bounds.getX(), (int)bounds.getY());
				resizeAnnotation.setSize(bounds.getWidth(), bounds.getHeight());
				resizeAnnotation.update();
				re.updateView();
			} else if (repositionAnnotation != null) {
				Point2D mousePoint = new Point2D.Double(mouseX, mouseY);

				// See what's under our mouse
				// Annotation?
				List<DingAnnotation> annotations = re.getPicker().getAnnotationsAt(mousePoint);
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
				re.updateView();
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(cyAnnotator.getResizeShape() != null) {
				cyAnnotator.getResizeShape().contentChanged();
				cyAnnotator.resizeShape(null);
				cyAnnotator.postUndoEdit(); // markUndoEdit() is in the dialogs like ShapeAnnotationDialog
			} else if(cyAnnotator.getRepositioningArrow() != null) {
				cyAnnotator.getRepositioningArrow().contentChanged();
				cyAnnotator.positionArrow(null);
				cyAnnotator.postUndoEdit(); // markUndoEdit() is in ArrowAnnotationDialog
			}
		}
		
	}
	
	
	private class SelectionLassoListener extends MouseAdapter {
		
		private final Color SELECTION_RECT_BORDER_COLOR_1 = UIManager.getColor("Focus.color");

		private GeneralPath selectionLasso;
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isShiftDown() && isControlOrMetaDown(e)) { // Temporary
				get(AddEdgeListener.class).reset();
				selectionLasso = new GeneralPath();
				selectionLasso.moveTo(e.getX(), e.getY());
				e.consume();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(selectionLasso != null) {
				selectionLasso.lineTo(e.getX(), e.getY());
				repaint();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(selectionLasso != null) {
				selectionLasso.closePath();
				
				List<DingAnnotation> annotations = Collections.emptyList();
				List<HandleKey> handles  = Collections.emptyList();
				List<View<CyNode>> nodes = Collections.emptyList();
				List<View<CyEdge>> edges = Collections.emptyList();

				if(annotationSelectionEnabled()) {
					annotations = re.getPicker().getAnnotationsInPath(selectionLasso);
				}
				if(nodeSelectionEnabled()) {
					nodes = re.getPicker().getNodesInPath(selectionLasso);
				}
				if(edgeSelectionEnabled()) {
					// MKTODO
					edges   = re.getPicker().getEdgesInPath(selectionLasso);
					handles = re.getPicker().getHandlesInPath(selectionLasso);
				}
				
				// Select
				if(!nodes.isEmpty())
					select(nodes, CyNode.class, true);
				if(!edges.isEmpty())
					select(edges, CyEdge.class, true);
				for(HandleKey handle : handles)
					re.getBendStore().selectHandle(handle);
				for(DingAnnotation a : annotations)
					a.setSelected(true);
			}
			selectionLasso = null;
			repaint(); // repaint the glass pane
		}
		
		public boolean isDragging() {
			return selectionLasso != null;
		}
		
		public void drawSelectionLasso(Graphics graphics) {
			if(selectionLasso != null) {
				Graphics2D g = (Graphics2D) graphics.create();
				g.setColor(SELECTION_RECT_BORDER_COLOR_1);
				GeneralPath path = new GeneralPath(selectionLasso);
				path.closePath();
				g.setStroke(new BasicStroke(2));
				g.draw(path);
			}
		}
	}

	
	private class SelectionRectangleListener extends MouseAdapter {
		
		private final Color SELECTION_RECT_BORDER_COLOR_1 = UIManager.getColor("Focus.color");
		private final Color SELECTION_RECT_BORDER_COLOR_2 = new Color(255, 255, 255, 160);

		private Rectangle selectionRect;
		private Point mousePressedPoint;
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(isDragSelectionKeyDown(e)) {
				get(AddEdgeListener.class).reset();
				mousePressedPoint = e.getPoint();
				selectionRect = new Rectangle(e.getX(), e.getY(), 0, 0);
				changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				e.consume();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(selectionRect != null) {
				int x = Math.min(mousePressedPoint.x, e.getX());
				int y = Math.min(mousePressedPoint.y, e.getY());
				int w = Math.abs(mousePressedPoint.x - e.getX());
				int h = Math.abs(mousePressedPoint.y - e.getY());
				selectionRect.setBounds(x, y, w, h);
				repaint(); // repaint the glass pane
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(selectionRect != null) {
				List<DingAnnotation> annotations = Collections.emptyList();
				List<HandleKey> handles  = Collections.emptyList();
				List<View<CyNode>> nodes = Collections.emptyList();
				List<View<CyEdge>> edges = Collections.emptyList();

				if(annotationSelectionEnabled()) {
					annotations = re.getPicker().getAnnotationsInRectangle(selectionRect);
				}
				if(nodeSelectionEnabled()) {
					nodes = re.getPicker().getNodesInRectangle(selectionRect);
				}
				if(edgeSelectionEnabled()) {
					edges = re.getPicker().getEdgesInRectangle(selectionRect);
					handles = re.getPicker().getHandlesInRectangle(selectionRect);
				}
				
				// Select
				if(!nodes.isEmpty())
					select(nodes, CyNode.class, true);
				if(!edges.isEmpty())
					select(edges, CyEdge.class, true);
				for(HandleKey handle : handles)
					re.getBendStore().selectHandle(handle);
				for(DingAnnotation a : annotations)
					a.setSelected(true);
			}
			selectionRect = null;
			mousePressedPoint = null;
			repaint(); // repaint the glass pane
		}
		
		public boolean isDragging() {
			return selectionRect != null;
		}
		
		public void drawSelectionRectangle(Graphics graphics) {
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
	}
	
	
	private class PanListener extends MouseAdapter {
		
		private final double[] coords = new double[2];
		private final Cursor panCursor = createPanCursor();
		
		private Point mousePressedPoint;
		private ViewChangeEdit undoPanEdit;
		
		@Override
		public void mousePressed(MouseEvent e) {
			changeCursor(panCursor);
			mousePressedPoint = e.getPoint();
			e.consume();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(mousePressedPoint != null) {
				if(undoPanEdit == null) {
					// Save state on start of drag, that way we don't post an undo edit if the user just clicks.
					// Pass null, don't save node state, just the center location of the canvas.
					undoPanEdit = new ViewChangeEdit(re, null, "Pan", registrar); 
				}
				
				// MKTODO does holding SHIFT matter??
				coords[0] = mousePressedPoint.getX();
				coords[1] = mousePressedPoint.getY();
				re.getTransform().xformImageToNodeCoords(coords);
				double oldX = coords[0];
				double oldY = coords[1];
				
				coords[0] = e.getX();
				coords[1] = e.getY();
				re.getTransform().xformImageToNodeCoords(coords);
				double newX = coords[0];
				double newY = coords[1];
				
				mousePressedPoint = e.getPoint();
				
				double deltaX = oldX - newX;
				double deltaY = oldY - newY;
				
				re.pan(deltaX, deltaY);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(undoPanEdit != null)
				undoPanEdit.post();
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			mousePressedPoint = null;
			undoPanEdit = null;
		}
		
		private Cursor createPanCursor() {
			if (LookAndFeelUtil.isMac()) {
				Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(24, 24);
				Image image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				String icon = IconManager.ICON_ARROWS;
				JLabel label = new JLabel(icon);
				label.setBounds(0, 0, size.width, size.height);
				label.setFont(registrar.getService(IconManager.class).getIconFont(14));
				label.paint(graphics);
				graphics.dispose();
				return Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "custom:" + (int) icon.charAt(0));
			} else {
				Cursor panCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				if(panCursor == null) {
					panCursor = new Cursor(Cursor.MOVE_CURSOR);
				}
				return panCursor;
			}
		}
	}
	
	
	
	// Utility methods below
	
	private enum Toggle {
		SELECT, DESELECT, NOCHANGE
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
	
	private boolean isLODEnabled(int flag) {
		return re.getLastRenderDetail().has(flag);
	}

	private boolean overNode(Point2D mousePoint) {
		return re.getPicker().getNodeAt(mousePoint) != null;
	}

	private CyNode getNodeAtLocation(Point2D mousePoint) {
		return re.getPicker().getNodeAt(mousePoint).getModel();
	}
	
	
	private <T extends CyIdentifiable> void toggleSelection(View<T> element, Class<T> type, Toggle toggle) {
		if(element != null) {
			if(toggle == Toggle.SELECT)
				select(Collections.singletonList(element), type, true);
			else if(toggle == Toggle.DESELECT)
				select(Collections.singletonList(element), type, false);
		}
	}
	
	private void deselectAllNodes() {
		if(nodeSelectionEnabled()) {
			Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
			select(selectedNodes, CyNode.class, false);
		}
	}
	
	private void deselectAllEdges() {
		if(edgeSelectionEnabled()) {
			re.getBendStore().unselectAllHandles();
			Collection<View<CyEdge>> selectedEdges = re.getViewModelSnapshot().getTrackedEdges(CyNetworkViewConfig.SELECTED_EDGES);
			select(selectedEdges, CyEdge.class, false);
		}
	}
	
	private void deselectAllAnnotations() {
		if(annotationSelectionEnabled()) {
			cyAnnotator.clearSelectedAnnotations();
		}
	}
	
	private void deselectAllNodesAndEdges() {
		deselectAllNodes();
		deselectAllEdges();
	}
	
	private void deselectAll() {
		deselectAllNodes();
		deselectAllEdges();
		deselectAllAnnotations();
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
	
	
	private <T extends CyIdentifiable> void select(Collection<View<T>> nodesOrEdgeViews, Class<T> type, boolean selected) {
		if (nodesOrEdgeViews == null || nodesOrEdgeViews.isEmpty())
			return;
		
		boolean isNodes = type.equals(CyNode.class);
		Boolean selectedBoxed = Boolean.valueOf(selected);
		
		CyNetwork model = re.getViewModel().getModel();
		CyTable table = isNodes ? model.getDefaultNodeTable() : model.getDefaultEdgeTable();
		
		// MKTODO is this right? what if the row doesn't exist?
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		for (View<? extends CyIdentifiable> nodeOrEdgeView : nodesOrEdgeViews) {
			if(isNodes) {
				View<CyNode> mutableNodeView = re.getViewModel().getNodeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getNodeInfo((View<CyNode>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			} else {
				View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getEdgeInfo((View<CyEdge>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableEdgeView.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			}
		}
	}
	
}
