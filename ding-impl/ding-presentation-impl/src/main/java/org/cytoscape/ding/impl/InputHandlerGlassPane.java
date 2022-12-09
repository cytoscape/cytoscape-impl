package org.cytoscape.ding.impl;

import static java.awt.event.KeyEvent.*;
import static org.cytoscape.ding.internal.util.ViewUtil.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolTip;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DRenderingEngine.Panner;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AnchorLocation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.impl.undo.AnnotationEdit;
import org.cytoscape.ding.impl.undo.CompositeCyEdit;
import org.cytoscape.ding.impl.undo.LabelEdit;
import org.cytoscape.ding.impl.undo.ViewChangeEdit;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.ding.internal.util.OrderedMouseAdapter;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.graph.render.stateful.GraphLOD.RenderEdges;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextWrapToolTip;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * This is a Swing glass pane that sits above the network canvas and handles all
 * mouse and keyboard events.
 * 
 * The glass pane is also responsible for drawing the selection rectangle and
 * the small progress bar.
 */
@SuppressWarnings("serial")
public class InputHandlerGlassPane extends JComponent implements CyDisposable {
	
	private static final int PROGRESS_BAR_TICKS = 1000;
	
	private final CyServiceRegistrar registrar;
	private final OrderedMouseAdapter orderedMouseAdapter;
	private final PopupMenuHelper popupMenuHelper;
	private final JProgressBar progressBar;
	
	private DRenderingEngine re;
	private CyAnnotator cyAnnotator;
	
	public InputHandlerGlassPane(CyServiceRegistrar registrar, DRenderingEngine re) {
		this.registrar = registrar;
		this.re = re;
		this.cyAnnotator = re.getCyAnnotator();
		
		// Order matters, some listeners use MouseEvent.consume() to prevent subsequent listeners from running
		orderedMouseAdapter = new OrderedMouseAdapter(
			new FocusRequestListener(),
			new CanvasKeyListener(),  // key listener also needs to listen for mouse presses
			new ContextMenuListener(),
			new DoubleClickListener(),
			new AddEdgeListener(),
			new TooltipListener(),
			new AddAnnotationListener(),
			new SelectionClickAndDragListener(),
			new SelectionLassoListener(),
			new SelectionRectangleListener(),
			new PanListener() // panning only happens if no node/edge/annotation/handle is clicked, so it needs to go last
		);

		addMouseListener(orderedMouseAdapter);
		addMouseMotionListener(orderedMouseAdapter);
		addKeyListener(orderedMouseAdapter.get(CanvasKeyListener.class));
		addMouseWheelListener(new CanvasMouseWheelListener());
		setFocusable(true); // key listener needs the focus
		
		this.popupMenuHelper = new PopupMenuHelper(re, InputHandlerGlassPane.this, registrar);
		this.progressBar = addProgressBar();
	}
	
	@Override
	public void dispose() {
		orderedMouseAdapter.dispose();
		re = null;
		cyAnnotator = null;
	}
	
	@Override
	public JToolTip createToolTip() {
		TextWrapToolTip tip = new TextWrapToolTip();
		tip.setMaximumSize(new Dimension(480, 320));
		tip.setComponent(this);
		return tip;
	}
	
	private JProgressBar addProgressBar() {
		JProgressBar progressBar = new JProgressBar(0, PROGRESS_BAR_TICKS);
		Dimension size = progressBar.getPreferredSize();
		progressBar.setMaximumSize(new Dimension(100, size.height));
		progressBar.setVisible(false);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(progressBar);
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.SOUTH);
		return progressBar;
	}
	
	/**
	 * The progress monitor returned by this method cannot be restarted. 
	 * You can only call start() and then done() once on this object.
	 * No effort is made to report erroneous use of the API, behavior under invalid usage is undefined.
	 */
	public ProgressMonitor createProgressMonitor() {
		return new ProgressMonitor() {
			
			private boolean cancelled = false;
			private double currentProgress = 0.0;
			private Timer timer = new Timer(300, null);
			
			@Override
			public void start(String s) {
				progressBar.setValue(0);
				timer.setInitialDelay(300);
				timer.setRepeats(false);
				timer.addActionListener(e -> progressBar.setVisible(true));
				timer.start();
			}
			
			@Override
			public void done() {
				timer.stop();
				progressBar.setVisible(false);
				progressBar.setValue(0);
			}
			
			@Override
			public void addProgress(double progress) {
				synchronized(this) {
					currentProgress += progress;
				}
				// theoretically this could read the wrong value for currentProgress, but its not critical
				int ticks = (int) (currentProgress * PROGRESS_BAR_TICKS);
				progressBar.setValue(ticks);
			}
			
			@Override
			public void cancel() {
				cancelled = true;
				done();
			}
			
			@Override
			public boolean isCancelled() {
				return cancelled;
			}
		};
	}
	
	/**
	 * Note, when accessing an event listener DO NOT call any of its mouse event handler methods directly.
	 * They must be proxied through the {@link HiDPIProxyMouseAdapter}.
	 */
	private <T> T get(Class<T> t) {
		return orderedMouseAdapter.get(t);
	}
	
	private <T> Optional<T> maybe(Class<T> t) {
		return Optional.ofNullable(orderedMouseAdapter.get(t));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		maybe(AddEdgeListener.class).ifPresent(l -> l.drawAddingEdge(g));
		maybe(SelectionRectangleListener.class).ifPresent(l -> l.drawSelectionRectangle(g));
		maybe(SelectionLassoListener.class).ifPresent(l -> l.drawSelectionLasso(g));
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

	/** 
	 * Usually called by the Annotation UI.
	 * 
	 * @param factory
	 * @param callback To be be called after the anottation is added
	 */
	public void beginClickToAddAnnotation(AnnotationFactory<? extends Annotation> factory, Runnable callback) {
		get(AddAnnotationListener.class).beginClickToAddAnnotation(factory, callback);
	}

	public void cancelClickToAddAnnotation(AnnotationFactory<? extends Annotation> factory) {
		var listener = get(AddAnnotationListener.class);
		
		if (listener != null && factory.equals(listener.getAnnotationFactory()))
			listener.cancel();
	}

	
	private class CanvasKeyListener extends MouseAdapter implements KeyListener {

		private final int KEYPRESS_TIMER_DELAY = 600; //milliseconds
		
		private AnnotationEdit moveAnnotationsEdit;
		private ViewChangeEdit moveNodesEdit;
		private ViewChangeEdit panEdit;
		private CompositeCyEdit<LabelEdit> labelEdit;
		private Panner panner = null;
		
		private Timer swingTimer;
		
		private void endEdit() {
			// If the timer expires, or the user switches to mouse input, then post the undo edit.
			if(swingTimer != null) {
				swingTimer.stop();
				swingTimer = null;
			}
			
			if(labelEdit != null) {
				labelEdit.getChildren().forEach(LabelEdit::savePositionAndAngle);
				labelEdit.post();
			} else if(moveAnnotationsEdit != null || moveNodesEdit != null || panEdit != null) {
				if(moveAnnotationsEdit != null)
					 moveAnnotationsEdit.saveNewAnnotations();
				if(moveNodesEdit != null)
					moveNodesEdit.saveNewPositions();
				if(panEdit != null)
					panEdit.saveNewPositions();
				var composite = new CompositeCyEdit<AbstractCyEdit>("Move", registrar, 3);
				composite.add(moveAnnotationsEdit).add(moveNodesEdit).add(panEdit);
				composite.post();
				
				if(panner != null) {
					panner.endPan();
				}
			}
			
			moveAnnotationsEdit = null;
			moveNodesEdit = null;
			panEdit = null;
			labelEdit = null;
			panner = null;
		}
		
		/**
		 * Key listener also needs to listen for mouse presses to cancel the undo timer.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			endEdit();
		}
		
		private void resetMoveTimer() {
			// Use a swing timer to coalesce multiple quick keypresses into a single undo edit.
			if(swingTimer == null) {
				swingTimer = new Timer(KEYPRESS_TIMER_DELAY, ev -> endEdit());
				swingTimer.start();
			} else {
				swingTimer.restart();
			}
		}
		
		
		private int getMoveAmountImageUnit(KeyEvent e) {
			return e.isShiftDown() ? 15 : 2;
		}
		
		private float getMoveAmountNodeUnit(KeyEvent e) {
			int imageUnit = getMoveAmountImageUnit(e);
			float nodeUnit = re.getTransform().getNodeDistance(imageUnit);
			return nodeUnit;
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			
			// These flags will tell us if a re-render is needed and what kind
			boolean allChanged = false;
			boolean annotationsChanged = false;
			boolean moved = false;

			if(code == VK_UP || code == VK_DOWN || code == VK_LEFT || code == VK_RIGHT) {
				if(isControlOrMetaDown(e)) {
					panCanvas(e);
					moved = allChanged = true;
				} else {
					if(labelSelectionEnabled() && !re.getLabelSelectionManager().isEmpty()) {
						moved |= allChanged = moveLabels(e);
					} else {
						if(nodeSelectionEnabled()) {
							moved |= allChanged = moveNodesAndHandles(e);
						}
						if(annotationSelectionEnabled()) {
							moved |= annotationsChanged = moveAnnotations(e);
						}
					}
				}
			} else if(code == VK_ESCAPE) {
				allChanged = cancelAddingEdge();
				if(annotationSelectionEnabled()) {
					allChanged |= cancelAnnotations();
				}
			} else if(code == VK_BACK_SPACE || code == VK_DELETE) {
				deleteSelected();  // In this case changing the model will trigger a render
			}

			if(allChanged)
				re.updateView(UpdateType.ALL_FULL);
			else if(annotationsChanged)
				re.updateView(UpdateType.JUST_ANNOTATIONS);

			if(moved)
				resetMoveTimer();
		}
		
		@Override
		public void keyReleased(KeyEvent e) { 
			int code = e.getKeyCode();
			var annotationSelection = cyAnnotator.getAnnotationSelection();

			if (annotationSelectionEnabled() && !annotationSelection.isEmpty() && code == VK_DELETE) {
				for (DingAnnotation a : annotationSelection) {
					a.removeAnnotation();
				}
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			// Just ignore...
		}
		
		private boolean moveAnnotations(KeyEvent e) {
			var selection = cyAnnotator.getAnnotationSelection();
			if(selection.isEmpty())
				return false;
			
			Point start = re.getTransform().getImageCoordinates(selection.getLocation());
			int x = start.x;
			int y = start.y;
			
			if(moveAnnotationsEdit == null) {
				String message = selection.size() == 1 ? "Move Annotation" : "Move Annotations";
				moveAnnotationsEdit = new AnnotationEdit(message, re);
			}
			
			final int move = getMoveAmountImageUnit(e);
			switch(e.getKeyCode()) {
				case VK_UP:    y -= move; break;
				case VK_DOWN:  y += move; break;
				case VK_LEFT:  x -= move; break;
				case VK_RIGHT: x += move; break;
			}
			
			selection.setMovingStartOffset(start);
			selection.moveSelection(x, y);

			return true;
		}
		
		private boolean cancelAnnotations() {
			if (cyAnnotator.getResizeShape() != null) {
				cyAnnotator.getResizeShape().contentChanged();
				cyAnnotator.resizeShape(null);
				cyAnnotator.postUndoEdit();
				
				return true;
			} else if (cyAnnotator.getRepositioningArrow() != null) {
				cyAnnotator.removeAnnotation(cyAnnotator.getRepositioningArrow());
				cyAnnotator.clearUndoEdit();
				
				return true;
			}
			
			return false;
		}
		
		private void panCanvas(KeyEvent k) {
			if(panEdit == null) {
				panEdit = new ViewChangeEdit(re, null, "Pan", registrar); 
			}
			if(panner == null) {
				panner = re.startPan();
			}
			
			float move = getMoveAmountNodeUnit(k);
			switch(k.getKeyCode()) {
				case VK_UP:    panner.continuePan(0, move); break;
				case VK_DOWN:  panner.continuePan(0, -move); break;
				case VK_LEFT:  panner.continuePan( move, 0); break;
				case VK_RIGHT: panner.continuePan(-move, 0); break;
			}
		}

		private boolean moveNodesAndHandles(KeyEvent k) {
			var snapshot = re.getViewModelSnapshot();
			var selectedNodes = snapshot.getTrackedNodes(DingNetworkViewFactory.SELECTED_NODES);
			
			if(!selectedNodes.isEmpty() && moveNodesEdit == null) {
				moveNodesEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED, "Move", registrar);
			}
			
			final float move = getMoveAmountNodeUnit(k);
			for(View<CyNode> node : selectedNodes) {
				double xPos = re.getNodeDetails().getXPosition(node);
				double yPos = re.getNodeDetails().getYPosition(node);

				switch(k.getKeyCode()) {
					case VK_UP:    yPos -= move; break;
					case VK_DOWN:  yPos += move; break;
					case VK_LEFT:  xPos -= move; break;
					case VK_RIGHT: xPos += move; break;
				}

				// MKTODO better way of doing this???
				View<CyNode> mutableNodeView = snapshot.getMutableNodeView(node.getSUID());
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPos);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPos);
			}

			if(re.getBendStore().areHandlesSelected()) {
				float dx = 0, dy = 0;
				switch(k.getKeyCode()) {
					case VK_UP:    dy = -move; break;
					case VK_DOWN:  dy = +move; break;
					case VK_LEFT:  dx = -move; break;
					case VK_RIGHT: dx = +move; break;
				}
				re.getBendStore().moveSelectedHandles(dx, dy);
			}
			
			return !selectedNodes.isEmpty() || re.getBendStore().areHandlesSelected();
		}
		
		
		private boolean moveLabels(KeyEvent k) {
			var labelSelectionManager = re.getLabelSelectionManager();
			var selectedNodeLabels = labelSelectionManager.getSelectedNodeLabels();
			var selectedEdgeLabels = labelSelectionManager.getSelectedEdgeLabels();
			if((selectedNodeLabels == null && selectedEdgeLabels == null) || (selectedNodeLabels.isEmpty() && selectedEdgeLabels.isEmpty()))
				return false;
			
			if(labelEdit == null) {
				int size = 0;
				if (selectedNodeLabels != null && selectedNodeLabels.size() > 0)
					size += selectedNodeLabels.size();
				if (selectedEdgeLabels != null && selectedEdgeLabels.size() > 0)
					size += selectedEdgeLabels.size();

				labelEdit = new CompositeCyEdit<LabelEdit>("Move Labels", registrar, size);
				for(var selectedLabel : selectedNodeLabels) {
					View<CyNode> mutableNode = re.getViewModelSnapshot().getMutableNodeView(selectedLabel.getNode().getSUID());
					Long suid = mutableNode.getModel().getSUID();
					var edit = new LabelEdit(registrar, re, re.getViewModel(), suid, selectedLabel);
					labelEdit.add(edit);
				}
				for(var selectedLabel : selectedEdgeLabels) {
					View<CyEdge> mutableEdge = re.getViewModelSnapshot().getMutableEdgeView(selectedLabel.getEdge().getSUID());
					Long suid = mutableEdge.getModel().getSUID();
					var edit = new LabelEdit(registrar, re, re.getViewModel(), suid, selectedLabel);
					labelEdit.add(edit);
				}
			}

			ObjectPosition pos;
			if (selectedNodeLabels != null && !selectedNodeLabels.isEmpty())
				pos = selectedNodeLabels.iterator().next().getPosition();
			else
				pos = selectedEdgeLabels.iterator().next().getPosition();

			// System.out.println("pos = "+pos);
			Point start = re.getTransform().getImageCoordinates(pos.getOffsetX(), pos.getOffsetY());
			int x = start.x;
			int y = start.y;
			
			final int move = getMoveAmountImageUnit(k);
			switch(k.getKeyCode()) {
				case VK_UP:    y -= move; break;
				case VK_DOWN:  y += move; break;
				case VK_LEFT:  x -= move; break;
				case VK_RIGHT: x += move; break;
			}

			labelSelectionManager.setCurrentDragPoint(start);
			// System.out.println("Move to: "+x+","+y);
			labelSelectionManager.move(new Point(x, y));
			
			for(var selectedLabel : re.getLabelSelectionManager().getSelectedNodeLabels()) {
				View<CyNode> mutableNode = re.getViewModelSnapshot().getMutableNodeView(selectedLabel.getNode().getSUID());
				ObjectPosition newPosition = selectedLabel.getPosition();
				double newAngle = selectedLabel.getAngleDegrees();
				mutableNode.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, newPosition);
				mutableNode.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, newAngle);
			}

			for(var selectedLabel : re.getLabelSelectionManager().getSelectedEdgeLabels()) {
				View<CyEdge> mutableEdge = re.getViewModelSnapshot()
						.getMutableEdgeView(selectedLabel.getEdge().getSUID());
				ObjectPosition newPosition = selectedLabel.getPosition();
				// So, this is a bit tricky. We want the resolved, translated position to be
				// where our cursor currently is, so we need to essentially reverse the
				// translation we're going to do
				// later in GraphRenderer
				// System.out.println("newPosition="+newPosition);
				double newAngle = selectedLabel.getAngleDegrees();
				mutableEdge.setLockedValue(DVisualLexicon.EDGE_LABEL_POSITION, newPosition);
				mutableEdge.setLockedValue(DVisualLexicon.EDGE_LABEL_ROTATION, newAngle);
			}

			return true;
		}
		
		
		private boolean cancelAddingEdge() {
			var addEdgeListener = get(AddEdgeListener.class);

			if (addEdgeListener.addingEdge()) {
				addEdgeListener.reset();
				return true;
			}

			return false;
		}
		
		private void deleteSelected() {
			var tasks = new TaskIterator();
			{
				var f = registrar.getService(NetworkViewTaskFactory.class, "(id=removeSelectedAnnotationsTaskFactory)");
				
				if (f.isReady(re.getViewModel()))
					tasks.append(f.createTaskIterator(re.getViewModel()));
			}
			{
				var f = registrar.getService(DeleteSelectedNodesAndEdgesTaskFactory.class);
				
				if (f.isReady(re.getViewModel().getModel()))
					tasks.append(f.createTaskIterator(re.getViewModel().getModel()));
			}

			var taskManager = registrar.getService(TaskManager.class);
			taskManager.execute(tasks);
		}
	}
	
	private class CanvasMouseWheelListener implements MouseWheelListener {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			re.zoomToPointer(e.getWheelRotation(), e.getX(), e.getY());
			re.updateView(UpdateType.ALL_FULL);
		}
	}

	private class FocusRequestListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			requestFocusInWindow(); // need to do this to receive key events
		}
	}
	
	private class ContextMenuListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (isSingleRightClick(e)) {
				showContextMenu(e.getPoint());
				e.consume();
				get(AddAnnotationListener.class).cancel();
			}
		}
		
		private void showContextMenu(Point p) {
			// Node menu
			var nodeView = re.getPicker().getNodeAt(p);
			
			if (nodeView != null) {
				popupMenuHelper.createNodeViewMenu(nodeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
				return;
			}
			
			// Edge menu
			var edgeView = re.getPicker().getEdgeAt(p);
			
			if (edgeView != null) {
				popupMenuHelper.createEdgeViewMenu(edgeView, p.x, p.y, PopupMenuHelper.ACTION_NEW);
				return;
			}
			
			// Network canvas menu
			double[] loc = { p.getX(), p.getY() };
			re.getTransform().xformImageToNodeCoords(loc);
			var xformP = new Point();
			xformP.setLocation(loc[0], loc[1]);
			popupMenuHelper.createNetworkViewMenu(p, xformP, PopupMenuHelper.ACTION_NEW);
		}
	}

	private class DoubleClickListener extends MouseAdapter {
		
		private boolean doublePressed = false;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(isDoubleLeftClick(e)) {
				var picker = re.getPicker();
				
				if(annotationSelectionEnabled()) {
					var annotation = picker.getAnnotationAt(e.getPoint());
					
					if (annotation != null) {
						editAnnotation(annotation, e.getPoint());
						e.consume();
						return;
					}
				}
				
				showContextMenu(e.getPoint());
				e.consume();
			}
		}
		
		// Guard against double-click-then-drag, don't let the event continue down the chain
		@Override
		public void mousePressed(MouseEvent e) {
			if(isDoubleLeftClick(e)) {
				e.consume();
				doublePressed = true;
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(doublePressed) {
				e.consume();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(doublePressed) {
				e.consume();
			}
			doublePressed = false;
		}
		
		
		private void showContextMenu(Point p) {
			NetworkPicker picker = re.getPicker();
			// This also causes groups to expand/collapse on double click
			View<CyNode> node = picker.getNodeAt(p);
			if(node != null) {
				popupMenuHelper.createNodeViewMenu(node, p.x, p.y, PopupMenuHelper.ACTION_OPEN);
				return;
			}
			
			View<CyEdge> edge = picker.getEdgeAt(p);
			if(edge != null) {
				popupMenuHelper.createEdgeViewMenu(edge, p.x, p.y, PopupMenuHelper.ACTION_OPEN);
				return;
			}
			
			Point2D nodePt = re.getTransform().getNodeCoordinates(p);
			Point xformPt = new Point();
			xformPt.setLocation(nodePt.getX(), nodePt.getY());
			popupMenuHelper.createNetworkViewMenu(p, xformPt, PopupMenuHelper.ACTION_OPEN);
		}
		
		private void editAnnotation(DingAnnotation annotation, Point p) {
			invokeOnEDT(() -> {
				var tm = registrar.getService(DialogTaskManager.class);
				var f = registrar.getService(NetworkViewLocationTaskFactory.class, "(id=editAnnotationTaskFactory)");

				if (f instanceof EditAnnotationTaskFactory)
					tm.execute(((EditAnnotationTaskFactory) f).createTaskIterator(annotation, re.getViewModel(), p));
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
		
		public boolean addingEdge() {
			return startPoint != null;
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
			if (lineLen == 0)
				lineLen = 1;

			y2 += ((y1 - y2) / lineLen) * offset;
			x2 += ((x1 - x2) / lineLen) * offset;

			Graphics2D g = (Graphics2D) graphics.create();
			g.setColor(Color.BLACK);
			g.drawLine(((int) x1) - 1, ((int) y1) - 1, ((int) x2) + 1, ((int) y2) + 1);
		}
		
		private void createEdge(View<CyNode> sourceNodeView, View<CyNode> targetNodeView) {
			var netView = re.getViewModelSnapshot();
			AddEdgeTask addEdgeTask = new AddEdgeTask(registrar, netView, sourceNodeView, targetNodeView);
			DialogTaskManager taskManager = registrar.getService(DialogTaskManager.class);
			taskManager.execute(new TaskIterator(addEdgeTask));
		}
	}
	
	
	private class TooltipListener extends MouseAdapter {
		
		private DebounceTimer delayTimer = new DebounceTimer(60);
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// This event gets called a lot as the user moves the mouse over the canvas.
			// Use a CoalesceTimer to debounce the event to avoid calling getPickedNodeView() constantly.
			delayTimer.debounce(() -> showTooltip(e));
		}
		
		private String getNodeOrEdgeTooltipText(MouseEvent e) {
			String text = null;
			
			View<CyNode> node = re.getPicker().getNodeAt(e.getPoint());
			if(node != null) {
				text = re.getNodeDetails().getTooltipText(node);
			} else if(edgeCountIsLowEnoughToEnablePicking()) {
				View<CyEdge> edge = re.getPicker().getEdgeAt(e.getPoint());
				if(edge != null) {
					text = re.getEdgeDetails().getTooltipText(edge);
				}
			}
			
			if(text != null) {
				text = text.trim();
				if(text.isBlank()) {
					text = null;
				}
			}
			
			return text;
		}
		
		private void showTooltip(MouseEvent e) {
			// CYTOSCAPE-12956: text may be null, that clears the tooltip for the component 
			String text = getNodeOrEdgeTooltipText(e);
			
			ViewUtil.invokeOnEDT(() -> {
				setToolTipText(text);
				ToolTipManager.sharedInstance().mouseMoved(e);
			});
		}
	}
	
	private class SelectionClickAndDragListener extends MouseAdapter {

		private Point mousePressedPoint;
		private boolean deselectAllOnRelease;
		private boolean hit;
		
		private AnnotationEdit annotationResizeEdit;
		private AnnotationEdit annotationMovingEdit;
		private AnnotationEdit annotationRotateEdit;
		private ViewChangeEdit removeHandleEdit;
		private ViewChangeEdit addHandleEdit;
		private ViewChangeEdit moveNodesEdit;
		private CompositeCyEdit<LabelEdit> labelEdit;
		
		private final Cursor rotateCursor = createRotateCursor();

		@Override
		public void mousePressed(MouseEvent e) {
			if(!isSingleLeftClick(e))
				return;
			
			annotationResizeEdit = null;
			annotationMovingEdit = null;
			annotationRotateEdit = null;
			removeHandleEdit = null;
			addHandleEdit = null;
			moveNodesEdit = null;
			labelEdit = null;
			
			deselectAllOnRelease = false;
			mousePressedPoint = e.getPoint();
			
			hit = mousePressedCheckHit(e);
			if(hit) {
				e.consume(); // no selection rectangle or lasso
			} else if(!isAdditiveSelect(e)) {
				deselectAllOnRelease = true;
			}
		}
		
		private boolean mousePressedCheckHit(MouseEvent e) {
			NetworkPicker picker = re.getPicker();

			if(annotationSelectionEnabled()) {
				var annotationSelection = cyAnnotator.getAnnotationSelection();
				annotationSelection.setMovingStartOffset(e.getPoint());
				
				AnchorLocation anchor = annotationSelection.overAnchor(e.getX(), e.getY());
				if(!annotationSelection.isEmpty() && anchor != null) {
					if (isControlOrMetaDown(e)) {
						changeCursor(rotateCursor);
						annotationSelection.setRotations();
					} else {
						mousePressedHandleAnnotationAnchor(anchor, e);
					}
					re.getLabelSelectionManager().clear();
					return true;
				}
				
				DingAnnotation annotation = picker.getAnnotationAt(CanvasID.FOREGROUND, e.getPoint());
				if(annotation != null) {
					Toggle select = mousePressedHandleAnnotation(annotation, e);
					if(select != Toggle.NOCHANGE && !isAdditiveSelect(e)) {
						deselectAllNodes();
						deselectAllEdges();
					}
					annotationSelection.setMovingStartOffset(e.getPoint());
					re.getLabelSelectionManager().clear();
					return true;
				}
			}
			
			if(labelSelectionEnabled()) {
				LabelSelection selectedLabel = picker.getNodeLabelAt(e.getPoint());

				// Not node label -- try for an edge label
				if (selectedLabel == null)
					selectedLabel = picker.getEdgeLabelAt(e.getPoint());
				
				if (selectedLabel != null) {
					var labelSelectionManager = re.getLabelSelectionManager();
					labelSelectionManager.setCurrentDragPoint(e.getPoint());
					if(isControlOrMetaDown(e)) {
						changeCursor(rotateCursor);
					}
					if(!labelSelectionManager.contains(selectedLabel)) {
						if(!e.isShiftDown()) {
							labelSelectionManager.clear();
						}
						labelSelectionManager.add(selectedLabel);
					}
					labelSelectionManager.setPrimary(selectedLabel);
					return true;
				}
				// selectedLabel = picker.getEdgeLabelAt(e.getPoint());
			}
			re.getLabelSelectionManager().clear();
			
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
			
			final boolean selectEdges = edgeSelectionEnabled() && edgeCountIsLowEnoughToEnablePicking();
			
			if(selectEdges && isLODEnabled(RenderDetailFlags.LOD_EDGE_ANCHORS)) {
				HandleInfo handle = picker.getHandleAt(e.getPoint());
				if(handle != null) {
					toggleChosenAnchor(handle, e);
					return true;
				}
			}
			
			if(selectEdges) {
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
			changeCursor(getResizeCursor(anchor.getPosition()));
			
			var annotationSelection = cyAnnotator.getAnnotationSelection();
			annotationSelection.startResizing(anchor);
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

			if(toggle != Toggle.NOCHANGE)
				re.updateView(UpdateType.JUST_ANNOTATIONS);
			
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
		
		private void toggleChosenAnchor(HandleInfo chosenAnchor, MouseEvent e) {
			// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
			if(e.isAltDown()) { // Remove handle
				removeHandleEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Remove Edge Handle", registrar);
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
			re.updateView(UpdateType.ALL_FULL);
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
					HandleInfo hit = re.getPicker().getHandleAt(e.getPoint());
					if(hit != null) {
						re.getBendStore().selectHandle(hit);
					}
				}
			}

			re.updateView(UpdateType.ALL_FULL);
			return toggle;
		}
		
		private void addHandle(View<CyEdge> edgeView, MouseEvent e) {
			var bendStore = re.getBendStore();
			bendStore.unselectAllHandles();
			addHandleEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", registrar);
			
			Point2D newHandlePoint = re.getTransform().getNodeCoordinates(e.getPoint());
			HandleInfo handle = bendStore.addHandle(edgeView, newHandlePoint);
			bendStore.selectHandle(handle);
		}
		
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(!hit)
				return;
			hit = false;
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(!isLeftClick(e)) // We only care about left mouse button
				return;

			var labelSelectionManager = re.getLabelSelectionManager();
			if(!labelSelectionManager.isEmpty()) {
				var selectedLabels = labelSelectionManager.getSelectedNodeLabels();
				
				for(var selectedLabel : selectedLabels) {
					View<CyNode> mutableNode = re.getViewModelSnapshot().getMutableNodeView(selectedLabel.getNode().getSUID());
					ObjectPosition newPosition = selectedLabel.getPosition();
					double newAngle = selectedLabel.getAngleDegrees();

					// Don't override these if they are the same
					ObjectPosition currentPosition = mutableNode.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
					if (!newPosition.equals(currentPosition))
						mutableNode.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, newPosition);
					double currentAngle = mutableNode.getVisualProperty(DVisualLexicon.NODE_LABEL_ROTATION);
					if (currentAngle != newAngle)
						mutableNode.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, newAngle);
				}
			
				if(labelEdit != null) {
					labelEdit.getChildren().forEach(LabelEdit::savePositionAndAngle);
					labelEdit.post();
				}

				selectedLabels = labelSelectionManager.getSelectedEdgeLabels();
				for(var selectedLabel : selectedLabels) {
					View<CyEdge> mutableEdge = re.getViewModelSnapshot().getMutableEdgeView(selectedLabel.getEdge().getSUID());
					ObjectPosition newPosition = selectedLabel.getPosition();
					// System.out.println("newPosition="+newPosition);
					double newAngle = selectedLabel.getAngleDegrees();

					// Don't override these if they are the same
					ObjectPosition currentPosition = mutableEdge.getVisualProperty(DVisualLexicon.EDGE_LABEL_POSITION);
					if (!newPosition.equals(currentPosition))
						mutableEdge.setLockedValue(DVisualLexicon.EDGE_LABEL_POSITION, newPosition);
					double currentAngle = mutableEdge.getVisualProperty(DVisualLexicon.EDGE_LABEL_ROTATION);
					if (currentAngle != newAngle)
						mutableEdge.setLockedValue(DVisualLexicon.EDGE_LABEL_ROTATION, newAngle);
				}
				
				if(labelEdit != null) {
					labelEdit.getChildren().forEach(LabelEdit::savePositionAndAngle);
					labelEdit.post();
				}

				// If moving a label then nothing else moves
				mousePressedPoint = null;
				e.consume();
				return;
			}
			
			if(annotationSelectionEnabled()) {
				var annotationSelection = cyAnnotator.getAnnotationSelection();
				annotationSelection.stopResizing();
				annotationSelection.stopMoving();
				annotationSelection.stopRotating();
			}
			
			mousePressedPoint = null;
			
			if(annotationMovingEdit != null || moveNodesEdit != null) {
				if(annotationMovingEdit != null)
					annotationMovingEdit.saveNewAnnotations();
				if(moveNodesEdit != null)
					moveNodesEdit.saveNewPositions();
				var composite = new CompositeCyEdit<AbstractCyEdit>("Move", registrar, 2);
				composite.add(annotationMovingEdit).add(moveNodesEdit);
				composite.post();
			} else if(annotationResizeEdit != null) {
				annotationResizeEdit.post();
			} else if(annotationRotateEdit != null) {
				annotationRotateEdit.post();
			} else if(removeHandleEdit != null) {
				removeHandleEdit.post();
			} else if(addHandleEdit != null) {
				addHandleEdit.post();
			}
			
			e.consume();
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// this method does not fire if there was a drag
			if(!isSingleLeftClick(e)) // We only care about left mouse button
				return;
			if(deselectAllOnRelease) {
				re.getLabelSelectionManager().clear();
				deselectAll();
			}
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!hit || !isLeftMouse(e))
				return;
			if (get(SelectionRectangleListener.class).isDragging() || maybe(SelectionLassoListener.class).map(l -> l.isDragging()).orElse(false))
				return;

			var labelSelectionManager = re.getLabelSelectionManager();
			if(!labelSelectionManager.isEmpty()) {
				if(labelEdit == null) {
					var selectedLabels = labelSelectionManager.getSelectedNodeLabels();

					if (selectedLabels != null && selectedLabels.size() > 0) {
						labelEdit = new CompositeCyEdit<LabelEdit>("Move Labels", registrar, selectedLabels.size());
						for (var selectedLabel : selectedLabels) {
							View<CyNode> mutableNode = re.getViewModelSnapshot().getMutableNodeView(selectedLabel.getNode().getSUID());
							Long suid = mutableNode.getModel().getSUID();
							var edit = new LabelEdit(registrar, re, re.getViewModel(), suid, selectedLabel);
							labelEdit.add(edit);
						}
					} else {
						selectedLabels = labelSelectionManager.getSelectedEdgeLabels();
						if (selectedLabels != null && selectedLabels.size() > 0) {
							labelEdit = new CompositeCyEdit<LabelEdit>("Move Labels", registrar, selectedLabels.size());
							for (var selectedLabel : selectedLabels) {
								View<CyEdge> mutableEdge = re.getViewModelSnapshot().getMutableEdgeView(selectedLabel.getEdge().getSUID());
								Long suid = mutableEdge.getModel().getSUID();
								var edit = new LabelEdit(registrar, re, re.getViewModel(), suid, selectedLabel);
								labelEdit.add(edit);
							}
						}
					}
				}
		
				if(isControlOrMetaDown(e)) {
					labelSelectionManager.rotate(e.getPoint());
				} else {
					labelSelectionManager.move(e.getPoint());
				}
				
				labelSelectionManager.setCurrentDragPoint(e.getPoint());
				re.updateView(UpdateType.ALL_FAST);
				return;
			}

			var selectedNodes = re.getViewModelSnapshot().getTrackedNodes(DingNetworkViewFactory.SELECTED_NODES);

			var annotationSelection = cyAnnotator.getAnnotationSelection();

			if (!annotationSelection.isEmpty()) {
				if (annotationSelection.isResizing()) {
					if (annotationResizeEdit == null)
						annotationResizeEdit = new AnnotationEdit("Resize Annotation", re);

					annotationSelection.resizeAnnotationsRelative(e.getX(), e.getY(), e.isShiftDown());
					re.updateView(UpdateType.JUST_ANNOTATIONS);

					return;
				} if (annotationSelection.isRotating()) {
					if (annotationRotateEdit == null)
						annotationRotateEdit = new AnnotationEdit("Rotate Annotation", re);
					
					annotationSelection.rotateSelection(e.getPoint());
				} else {
					if (annotationMovingEdit == null)
						annotationMovingEdit = new AnnotationEdit("Move Annotation", re);

					annotationSelection.moveSelection(e.getPoint());
					annotationSelection.setMovingStartOffset(e.getPoint());
				}
			}

			if (!selectedNodes.isEmpty() || re.getBendStore().areHandlesSelected())
				mouseDraggedHandleNodesAndEdges(selectedNodes, e);

			if (!selectedNodes.isEmpty() || re.getBendStore().areHandlesSelected())
				re.updateView(UpdateType.ALL_FAST);
			else
				re.updateView(UpdateType.JUST_ANNOTATIONS);
		}

		
		private void mouseDraggedHandleNodesAndEdges(Collection<View<CyNode>> selectedNodes, MouseEvent e) {
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
			
			var snapshot = re.getViewModelSnapshot();
			
			if(re.getBendStore().areHandlesSelected()) {
				re.getBendStore().moveSelectedHandles((float)deltaX, (float)deltaY);
			} 
			else { // If we are moving anchors of edges, no need to move nodes (bug #2360).
				NodeDetails nodeDetails = re.getNodeDetails();
				var networkView = snapshot.getMutableNetworkView();
				var eventHelper = registrar.getService(CyEventHelper.class);
				double dx = deltaX, dy = deltaY;
				
				networkView.batch(nv -> {
					for(var node : selectedNodes) {
						View<CyNode> mutableNode = snapshot.getMutableNodeView(node.getSUID());
						if(mutableNode != null) {
							double oldXPos = nodeDetails.getXPosition(mutableNode);
							double oldYPos = nodeDetails.getYPosition(mutableNode);
							mutableNode.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, oldXPos + dx);
							mutableNode.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, oldYPos + dy);
						}
				    }
				});
				
				// Fire the ViewChangedEvent immediately
				eventHelper.flushPayloadEvents(networkView);
			}
			
			if (!selectedNodes.isEmpty() || re.getBendStore().areHandlesSelected()) {
				re.updateView(UpdateType.ALL_FULL);
			}
		}
	}
	
	private class AddAnnotationListener extends MouseAdapter {
		
		private AnnotationFactory<?> annotationFactory;
		private Runnable callback;
		
		public void beginClickToAddAnnotation(AnnotationFactory<? extends Annotation> factory, Runnable callback) {
			this.annotationFactory = factory;
			this.callback = callback;
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		
		public AnnotationFactory<?> getAnnotationFactory() {
			return annotationFactory;
		}
		
		public void cancel() {
			if (annotationFactory != null)
				cyAnnotator.fireAnnotations(); // tell the Annotation panel that we cancelled
			
			annotationFactory = null;
			callback = null;
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (annotationFactory != null && isSingleLeftClick(e)) {
				createAnnotation(annotationFactory, e.getPoint());
				e.consume();
				
				if (callback != null)
					callback.run();
			}
			
			annotationFactory = null;
			callback = null;
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		private void createAnnotation(AnnotationFactory<? extends Annotation> f, Point point) {
			if (!(f instanceof AbstractDingAnnotationFactory)) // For now, only DING annotations are supported!
				return;
			
			var filter = "(id=addAnnotationTaskFactory_" + f.getId() + ")";
			var factory = registrar.getService(NetworkViewLocationTaskFactory.class, filter);
			var iterator = factory.createTaskIterator(re.getViewModel(), point, null);
			registrar.getService(DialogTaskManager.class).execute(iterator);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			// Note: Auto resizing was removed in 3.9, however this remaining code is still needed for arrow annotations
			var arrow = cyAnnotator.getRepositioningArrow();
			if (arrow != null) {
				var mousePoint = e.getPoint();
				var annotations = re.getPicker().getAnnotationsAt(mousePoint);
				
				if (annotations.contains(arrow))
					annotations.remove(arrow);

				// Target can be another annotation, a node, or just a point.
				if (!annotations.isEmpty()) {
					arrow.setTarget(annotations.get(0));
				} else if (overNode(mousePoint)) {
					var overNode = re.getPicker().getNodeAt(mousePoint);
					// The node view must be mutable so that the coordinates will update when the node is moved
					var mutableNodeView = re.getViewModelSnapshot().getMutableNodeView(overNode.getSUID());
					arrow.setTarget(mutableNodeView);
				} else {
					var nodeCoordinates = re.getTransform().getNodeCoordinates(mousePoint);
					arrow.setTarget(nodeCoordinates);
				}

				arrow.update();
				re.updateView(UpdateType.JUST_ANNOTATIONS);
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			var arrow = cyAnnotator.getRepositioningArrow();
			if(arrow != null && arrow.getTarget() != null) {
				arrow.contentChanged();
				
				// Select only the new arrow annotation
				cyAnnotator.clearSelectedAnnotations();
				ViewUtils.selectAnnotation(re, arrow);
				
				cyAnnotator.positionArrow(null);
				cyAnnotator.postUndoEdit(); // markUndoEdit() is in ArrowAnnotationDialog
			}
		}
	}

	
	/**
	 * MKTODO This listener is not finished and should not be enabled.
	 * @author mkucera
	 */
	private class SelectionLassoListener extends MouseAdapter {
		
		private final Color lassoColor = UIManager.getColor("Focus.color");
		private final BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);

		private GeneralPath selectionLasso;
		private Point start;
		private Point current;
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isShiftDown() && isControlOrMetaDown(e)) { // Temporary
				get(AddEdgeListener.class).reset();
				selectionLasso = new GeneralPath();
				selectionLasso.moveTo(e.getX(), e.getY());
				start = e.getPoint();
				e.consume();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(selectionLasso != null) {
				selectionLasso.lineTo(e.getX(), e.getY());
				current = e.getPoint();
				repaint();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(selectionLasso != null) {
				selectionLasso.closePath();
				
				if(nodeSelectionEnabled()) {
					var nodes = re.getPicker().getNodesInPath(selectionLasso);
					var edges = List.<View<CyEdge>>of();
					
					if(edgeSelectionEnabled() && edgeCountIsLowEnoughToEnablePicking()) {
						edges = new ArrayList<>();
						var netView = re.getViewModelSnapshot();
						
						for(var nv1 : nodes) {
							for(var nv2 : nodes) {
								edges.addAll(DEdgeDetails.getConnectingEdgeList(netView, nv1, nv2));
							}
						}
					}
					
					select(nodes, CyNode.class, true);
					select(edges, CyEdge.class, true);
				}
			}
			
			repaint(); // repaint the glass pane
			selectionLasso = null;
		}
		
		public boolean isDragging() {
			return selectionLasso != null;
		}
		
		public void drawSelectionLasso(Graphics graphics) {
			if(selectionLasso != null) {
				Graphics2D g = (Graphics2D) graphics.create();
				g.setColor(lassoColor);
				GeneralPath path = new GeneralPath(selectionLasso);
				//path.closePath();
				g.draw(path);
				
				if(start != null && current != null) {
					g.setStroke(dashedStroke);
					g.drawLine(start.x, start.y, current.x, current.y);
				}
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
				}
				
				// Select
				if(!nodes.isEmpty())
					select(nodes, CyNode.class, true);
				if(!edges.isEmpty())
					select(edges, CyEdge.class, true);
				for(DingAnnotation a : annotations)
					a.setSelected(true);
				
				if(nodes.isEmpty() && edges.isEmpty() && !annotations.isEmpty())
					re.updateView(UpdateType.JUST_ANNOTATIONS);
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
		
		private Panner panner = null;
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(!isSingleLeftClick(e))
				return;
			
			panner = re.startPan();
			
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
				
				panner.continuePan(deltaX, deltaY);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(undoPanEdit != null)
				undoPanEdit.post();
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			mousePressedPoint = null;
			undoPanEdit = null;
			
			if(panner != null) { // why does this happen?
				panner.endPan();
				panner = null;
			}
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
		return re.getViewModel().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION);
	}
	
	private boolean nodeSelectionEnabled() {
		return re.getViewModel().getVisualProperty(DVisualLexicon.NETWORK_NODE_SELECTION);
	}
	
	private boolean labelSelectionEnabled() {
		return re.getViewModel().getVisualProperty(DVisualLexicon.NETWORK_NODE_LABEL_SELECTION);
	}
	
	
	private boolean edgeSelectionEnabled() {
		if(Boolean.FALSE.equals(re.getViewModel().getVisualProperty(DVisualLexicon.NETWORK_EDGE_SELECTION))) {
			return false;
		}
		return true;
	}
	
	private boolean edgeCountIsLowEnoughToEnablePicking() {
		// Optimization, turn edge selection off if there are too many edges visible.
		// Picking the edge that the user clicked can become very performance intensive, and chances are they
		// are not trying to select individual edges from a hairball.
		var snapshot = re.getViewModelSnapshot();
		var fastLod = re.getGraphLOD().faster();
		RenderEdges edges = RenderDetailFlags.renderEdges(snapshot, re.getTransform(), fastLod);
		return edges != RenderEdges.NONE;
	}
	
	
	private boolean isLODEnabled(int flag) {
		return re.getPicker().getLastRenderDetail().has(flag);
	}

	private boolean overNode(Point2D mousePoint) {
		return re.getPicker().getNodeAt(mousePoint) != null;
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
	
	private <T extends CyIdentifiable> void toggleSelection(View<T> element, Class<T> type, Toggle toggle) {
		if(element != null) {
			if(toggle == Toggle.SELECT)
				select(Collections.singletonList(element), type, true);
			else if(toggle == Toggle.DESELECT)
				select(Collections.singletonList(element), type, false);
		}
	}
	
	private boolean deselectAllNodes() {
		var table = re.getViewModel().getModel().getDefaultNodeTable();
		var rows = table.getMatchingRows(CyNetwork.SELECTED, true);
		if(rows.isEmpty())
			return false;
		batchDeselectRows(table, rows);
		return true;
	}
	
	private boolean deselectAllEdges() {
		re.getBendStore().unselectAllHandles();
		var table = re.getViewModel().getModel().getDefaultEdgeTable();
		var rows = table.getMatchingRows(CyNetwork.SELECTED, true);
		if(rows.isEmpty())
			return false;
		batchDeselectRows(table, rows);
		return true;
	}
		
	// We want to fire the selection event immediately, waiting for the payload event to fire
	// on its own time creates a noticeable lag in rendering the nodes/edges as deselected.
	private void batchDeselectRows(CyTable table, Collection<CyRow> rows) {
		CyEventHelper eventHelper = registrar.getService(CyEventHelper.class);
		eventHelper.silenceEventSource(table);

		// Create the RowSetRecord collection
		List<RowSetRecord> rowsChanged = new ArrayList<>();

		// The list of objects will be all nodes or all edges
		for(var row : rows) {
			if (row != null) {
				row.set(CyNetwork.SELECTED, false);
				rowsChanged.add(new RowSetRecord(row, CyNetwork.SELECTED, false, false));
			}
		}

		// Enable all events from our table
		eventHelper.unsilenceEventSource(table);

		RowsSetEvent event = new RowsSetEvent(table, rowsChanged);
		eventHelper.fireEvent(event);
	}
	
	private boolean deselectAllAnnotations() {
		if(!cyAnnotator.getAnnotationSelection().isEmpty()) {
			cyAnnotator.clearSelectedAnnotations();
			return true;
		}
		return false;
	}
	
	private void deselectAll() {
		boolean nodesDeselected = deselectAllNodes();
		boolean edgesDeselected = deselectAllEdges();
		boolean annotDeselected = deselectAllAnnotations();
		if(!(nodesDeselected || edgesDeselected) && annotDeselected) {
			re.updateView(UpdateType.JUST_ANNOTATIONS);
		}
	}

  private Cursor createRotateCursor() {
		Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(256, 256);
		Image image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		String icon = IconManager.ICON_ROTATE_RIGHT;
		JLabel label = new JLabel(icon);
		label.setBounds(0, 0, size.width, size.height);
		label.setFont(registrar.getService(IconManager.class).getIconFont(150));
		label.paint(graphics);
		graphics.dispose();
		Image newImage = image.getScaledInstance(24, 24, Image.SCALE_DEFAULT);
		return Toolkit.getDefaultToolkit().createCustomCursor(newImage, new Point(0, 0), "custom:" + (int) icon.charAt(0));
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
				View<CyNode> mutableNodeView = snapshot.getMutableNodeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getNodeInfo((View<CyNode>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			} else {
				View<CyEdge> mutableEdgeView = snapshot.getMutableEdgeView(nodeOrEdgeView.getSUID());
				Long modelSuid = snapshot.getEdgeInfo((View<CyEdge>)nodeOrEdgeView).getModelSUID();
				CyRow row = table.getRow(modelSuid);
				mutableEdgeView.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED, selectedBoxed);
				row.set(CyNetwork.SELECTED, selectedBoxed);	
			}
		}
	}

}
