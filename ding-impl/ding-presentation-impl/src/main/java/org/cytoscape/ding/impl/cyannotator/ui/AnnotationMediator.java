package org.cytoscape.ding.impl.cyannotator.ui;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.annotations.Annotation.BACKGROUND;
import static org.cytoscape.view.presentation.annotations.Annotation.FOREGROUND;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.AnnotationNode;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.GroupAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.RemoveSelectedAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderSelectedAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.UngroupAnnotationsTask;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * This class mediates the communication between the Annotations UI and the rest of Cytoscape.
 */
public class AnnotationMediator implements CyStartListener, CyShutdownListener, SessionAboutToBeLoadedListener,
		SessionLoadedListener, NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener,
		SetCurrentNetworkViewListener, PropertyChangeListener, CytoPanelComponentSelectedListener {

	private AnnotationMainPanel mainPanel;
	private final Map<String, AnnotationFactory<? extends Annotation>> factories = new LinkedHashMap<>();
	private final EscapePressedListener escapePressedListener = new EscapePressedListener();
	private boolean appStarted;
	private boolean loadingSession;
	private boolean ignoreSelectedPropChangeEvents;
	
	private final DebounceTimer updateOrderTimer = new DebounceTimer(100);
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public AnnotationMediator(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(CyStartEvent evt) {
		var set = new LinkedHashSet<AnnotationFactory<? extends Annotation>>(factories.values());
		
		invokeOnEDT(() -> {
			if (mainPanel == null) {
				// We have to initialize and register the panel here,
				// after we know the correct Look And Feel has already been initialized
				mainPanel = new AnnotationMainPanel(serviceRegistrar);
				serviceRegistrar.registerAllServices(mainPanel, new Properties());
			}
			
			set.forEach(f -> addAnnotationButton(f));
			mainPanel.setEnabled(false);
			mainPanel.getGroupAnnotationsButton().addActionListener(e -> groupAnnotations());
			mainPanel.getUngroupAnnotationsButton().addActionListener(e -> ungroupAnnotations());
			mainPanel.getRemoveAnnotationsButton().addActionListener(e -> removeSelectedAnnotations());
			mainPanel.getPushToBackgroundButton().addActionListener(e -> moveAnnotationsToCanvas(BACKGROUND));
			mainPanel.getPullToForegroundButton().addActionListener(e -> moveAnnotationsToCanvas(FOREGROUND));
			mainPanel.getBackgroundLayerPanel().getForwardButton().addActionListener(e -> reorderAnnotations(BACKGROUND, mainPanel.getBackgroundTree(), Shift.UP_ONE));
			mainPanel.getBackgroundLayerPanel().getBackwardButton().addActionListener(e -> reorderAnnotations(BACKGROUND, mainPanel.getBackgroundTree(), Shift.DOWN_ONE));
			mainPanel.getForegroundLayerPanel().getForwardButton().addActionListener(e -> reorderAnnotations(FOREGROUND, mainPanel.getForegroundTree(), Shift.UP_ONE));
			mainPanel.getForegroundLayerPanel().getBackwardButton().addActionListener(e -> reorderAnnotations(FOREGROUND, mainPanel.getForegroundTree(), Shift.DOWN_ONE));
			mainPanel.getBackgroundTree().getSelectionModel().addTreeSelectionListener(e -> {
				if (!mainPanel.getBackgroundTree().isEditing()) {
					mainPanel.updateSelectionButtons();
					selectAnnotationsFromSelectedRows();
				}
			});
			mainPanel.getForegroundTree().getSelectionModel().addTreeSelectionListener(e -> {
				if (!mainPanel.getForegroundTree().isEditing()) {
					mainPanel.updateSelectionButtons();
					selectAnnotationsFromSelectedRows();
				}
			});
			mainPanel.getBackgroundTree().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowPopupMenu(mainPanel.getBackgroundTree(), e);
				}
				/** On Windows, popup is triggered by mouse release, not press. */
				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowPopupMenu(mainPanel.getBackgroundTree(), e);
				}
			});
			mainPanel.getForegroundTree().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowPopupMenu(mainPanel.getForegroundTree(), e);
				}
				/** On Windows, popup is triggered by mouse release, not press. */
				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowPopupMenu(mainPanel.getForegroundTree(), e);
				}
			});
			
			setKeyBindings(mainPanel.getBackgroundTree());
			setKeyBindings(mainPanel.getForegroundTree());
		});
		
		appStarted = true;
	}

	@Override
	public void handleEvent(CyShutdownEvent evt) {
		appStarted = false;
	}
	
	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent evt) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent evt) {
		loadingSession = false;
		
		if (mainPanel == null)
			return;
		
		mainPanel.clearAnnotationButtonSelection();
		
		var allViews = serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViewSet();
		var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
		
		allViews.forEach(view -> {
			var re = dingRenderer.getRenderingEngine(view);
			
			if (re != null) {
				addPropertyListeners(re);
				addPropertyListeners(re.getCyAnnotator().getAnnotations());
			}
		});
		
		var re = getCurrentDRenderingEngine();
		invokeOnEDT(() -> mainPanel.update(re));
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent evt) {
		if (!appStarted || loadingSession)
			return;
		
		var view = evt.getNetworkView();
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re != null) {
			addPropertyListeners(re);
			addPropertyListeners(re.getCyAnnotator().getAnnotations());
		}
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent evt) {
		var view = evt.getNetworkView();
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re != null) {
			removePropertyListeners(re);
			removePropertyListeners(re.getCyAnnotator().getAnnotations());
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent evt) {
		if (appStarted && !loadingSession) {
			mainPanel.clearAnnotationButtonSelection();
			var view = evt.getNetworkView();
			var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
			
			invokeOnEDT(() -> mainPanel.update(re));
		}
	}
	
	@Override
	public void handleEvent(CytoPanelComponentSelectedEvent evt) {
		// When the panel component changes, disable all annotation buttons,
		// so the user doesn't add an annotation by accident
		if (appStarted && CytoPanelName.WEST == evt.getCytoPanel().getCytoPanelName())
			invokeOnEDT(() -> mainPanel.clearAnnotationButtonSelection());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent evt) {
		if (!appStarted || loadingSession)
			return;
		
		var re = getCurrentDRenderingEngine();
		var cyAnnotator = re != null ? re.getCyAnnotator() : null;
		
		var source = evt.getSource();
		var propertyName = evt.getPropertyName();
		
		if (source.equals(cyAnnotator)) {
			switch (propertyName) {
				case CyAnnotator.PROP_ANNOTATIONS:
					// First remove property listeners from deleted annotations and add them to the new ones
					var oldList = mainPanel.getAllAnnotations();
					var newList = cyAnnotator.getAnnotations();
					oldList.removeAll(newList);
					removePropertyListeners(oldList);
					addPropertyListeners((Collection<Annotation>) evt.getNewValue());
					// Now update the UI
					invokeOnEDT(() -> mainPanel.update(re));
					break;
				case CyAnnotator.PROP_REORDERED:
					if (re != null && re.equals(mainPanel.getRenderingEngine()))
						updateOrderTimer.debounce(() -> {
							invokeOnEDT(() -> mainPanel.updateAnnotationsOrder());
						});
					break;
			}
		} else if (source instanceof Annotation) {
			if (re != null && re.equals(mainPanel.getRenderingEngine())) {
				if ("selected".equals(propertyName) && !ignoreSelectedPropChangeEvents) {
					invokeOnEDT(() -> mainPanel.setSelected((Annotation) source, (boolean) evt.getNewValue()));
				} else if ("canvas".equals(propertyName) && !ignoreSelectedPropChangeEvents) {
					updateOrderTimer.debounce(() -> {
						invokeOnEDT(() -> mainPanel.updateAnnotationsOrder());
					});
				}
			}
		}
	}
	
	/**
	 * Show the Control panel, if not visible, and switch to the Annotation tab.
	 */
	public void showAnnotationPanel() {
		invokeOnEDT(() -> {
			var swingApp = serviceRegistrar.getService(CySwingApplication.class);
			var cytoPanel = swingApp.getCytoPanel(AnnotationMainPanel.CYTOPANEL_NAME);
			
			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);
			
			int idx = cytoPanel.indexOfComponent(mainPanel.getComponent());
			
			if (idx >= 0)
				cytoPanel.setSelectedIndex(idx);
		});
	}
	
	/**
	 * Asks the editor to be updated with the passed annotation and show the editor.
	 */
	public void editAnnotation(Annotation a) {
		editAnnotation(a, null);
	}
	
	/**
	 * Asks the editor to be updated with the passed annotation and show the editor near the passed location,
	 * if that makes sense.
	 */
	public void editAnnotation(Annotation a, Point location) {
		if (a != null) {
			invokeOnEDT(() -> {
				mainPanel.editAnnotation(a, location);
				
				if (!mainPanel.getAppearancePanel().isFloating())
					showAnnotationPanel();
			});
		}
	}
	
	/**
	 * Asks the view to let the user edit the annotation's name.
	 */
	public void renameAnnotation(Annotation a) {
		if (a != null)
			invokeOnEDT(() -> mainPanel.renameAnnotation(a));
	}
	
	public void addAnnotationFactory(AnnotationFactory<? extends Annotation> f, Map<?, ?> props) {
		if (f instanceof AbstractDingAnnotationFactory == false)
			return; // For now, only DING annotations are supported!
		
		if (f instanceof GroupAnnotationFactory)
			return;
		
		synchronized (lock) {
			factories.put(f.getId(), f);
		}
		
		if (appStarted)
			invokeOnEDT(() -> addAnnotationButton(f));
	}
	
	public void removeAnnotationFactory(AnnotationFactory<? extends Annotation> f, Map<?, ?> props) {
		synchronized (lock) {
			if (factories.remove(f.getId()) != null && appStarted)
				invokeOnEDT(() -> mainPanel.removeAnnotationButton(f));
		}
	}
	
	private void addAnnotationButton(AnnotationFactory<? extends Annotation> factory) {
		var btn = mainPanel.addAnnotationButton(factory);
		btn.addItemListener(evt -> {
			int state = evt.getStateChange();
			var re = getCurrentDRenderingEngine();
			
			if (re != null) {
				var ihGlassPane = re.getInputHandlerGlassPane();
				
				if (state == ItemEvent.SELECTED) {
					addEscapePressedListener();
					ihGlassPane.beginClickToAddAnnotation(factory, () -> mainPanel.clearAnnotationButtonSelection());
				} else {
					ihGlassPane.cancelClickToAddAnnotation(factory);
					removeEscapePressedListener();
				}
			}
			
			mainPanel.setCreateMode(state == ItemEvent.SELECTED);
		});
	}

	private DRenderingEngine getCurrentDRenderingEngine() {
		var view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		var re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		return re;
	}
	
	private void selectAnnotationsFromSelectedRows() {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		var selList = mainPanel.getSelectedAnnotations();
		var all = re.getCyAnnotator().getAnnotations();
		
		if (all != null && !all.isEmpty()) {
			ignoreSelectedPropChangeEvents = true;
			
			try {
				all.forEach(a -> a.setSelected(selList.contains(a)));
			} finally {
				ignoreSelectedPropChangeEvents = false;
			}
		}
		
		var a = selList.size() > 0 ? selList.get(selList.size() - 1) : null;
		mainPanel.maybeUpdateEditingAnnotation(a);
	}
	
	private void groupAnnotations() {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		var selList = mainPanel.getSelectedAnnotations(DingAnnotation.class);

		if (!selList.isEmpty()) {
			var task = new GroupAnnotationsTask(re, selList);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void ungroupAnnotations() {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		var selList = mainPanel.getSelectedAnnotations(GroupAnnotation.class);

		if (!selList.isEmpty()) {
			var task = new UngroupAnnotationsTask(re, selList);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void removeSelectedAnnotations() {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		var selList = mainPanel.getSelectedAnnotations();
		
		if (!selList.isEmpty()) {
			var iterator = new TaskIterator(new RemoveSelectedAnnotationsTask(re, selList, serviceRegistrar));
			serviceRegistrar.getService(DialogTaskManager.class).execute(iterator);
		}
	}
	
	private void moveAnnotationsToCanvas(String canvasName) {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		if (re != null) {
			var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
			var factory = new ReorderSelectedAnnotationsTaskFactory(dingRenderer, canvasName);
			serviceRegistrar.getService(DialogTaskManager.class).execute(factory.createTaskIterator(re.getViewModel()));
		}
	}
	
	private void reorderAnnotations(String canvas, JTree tree, Shift shift) {
		var re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		if (re != null) {
			var annotations = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);
			var task = new ReorderAnnotationsTask(re, annotations, null, shift);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void addPropertyListeners(DRenderingEngine re) {
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		removePropertyListeners(re);
		re.getCyAnnotator().addPropertyChangeListener(CyAnnotator.PROP_ANNOTATIONS, this);
		re.getCyAnnotator().addPropertyChangeListener(CyAnnotator.PROP_REORDERED, this);
	}
	
	private void removePropertyListeners(DRenderingEngine re) {
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		re.getCyAnnotator().removePropertyChangeListener(CyAnnotator.PROP_ANNOTATIONS, this);
		re.getCyAnnotator().removePropertyChangeListener(CyAnnotator.PROP_REORDERED, this);
	}
	
	private void addPropertyListeners(Collection<? extends Annotation> list) {
		if (list != null)
			list.forEach(a -> {
				if (a instanceof DingAnnotation) {
					((DingAnnotation) a).removePropertyChangeListener("selected", this);
					((DingAnnotation) a).addPropertyChangeListener("selected", this);
					
					((DingAnnotation) a).removePropertyChangeListener("canvas", this);
					((DingAnnotation) a).addPropertyChangeListener("canvas", this);
				}
			});
	}
	
	private void removePropertyListeners(Collection<? extends Annotation> list) {
		if (list != null)
			list.forEach(a -> {
				if (a instanceof DingAnnotation) {
					((DingAnnotation) a).removePropertyChangeListener("selected", this);
					((DingAnnotation) a).removePropertyChangeListener("canvas", this);
				}
			});
	}
	
	private void maybeShowPopupMenu(JTree tree, MouseEvent e) {
		// Ignore if not valid trigger.
		if (!e.isPopupTrigger())
			return;

		var re = getCurrentDRenderingEngine();
		
		if (re == null)
			return;
		
		// If the item is not selected, select it first
		var node = getNodeAt(tree, e.getPoint());
		var annotations = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);

		if (node != null && !annotations.contains(node.getAnnotation()))
			mainPanel.setSelected(node.getAnnotation(), true);
		
		var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
		var dingRenderer = serviceRegistrar.getService(DingRenderer.class);
		var popup = new JPopupMenu();

		// Edit
		{
			var list = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);
			var a = list.size() == 1 ? list.get(0) : null;
			
			var mi = new JMenuItem("Modify Annotation...");
			mi.addActionListener(evt -> editAnnotation(a));
			popup.add(mi);
			mi.setEnabled(a != null && !(a instanceof GroupAnnotation));
		
			mi = new JMenuItem("Rename Annotation...");
			mi.addActionListener(evt -> renameAnnotation(a));
			popup.add(mi);
			mi.setEnabled(a != null);
		}
		popup.addSeparator();
		// Reorder
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(dingRenderer, Shift.TO_FRONT);
			var mi = new JMenuItem("Bring Annotations to Front");
			mi.addActionListener(evt -> {
				taskMgr.execute(factory.createTaskIterator(re.getViewModel()));
			});
			popup.add(mi);
			mi.setEnabled(factory.isReady(re.getViewModel()));
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(dingRenderer, Shift.TO_BACK);
			var mi = new JMenuItem("Send Annotations to Back");
			mi.addActionListener(evt -> {
				taskMgr.execute(factory.createTaskIterator(re.getViewModel()));
			});
			popup.add(mi);
			mi.setEnabled(factory.isReady(re.getViewModel()));
		}
		popup.addSeparator();
		{
			var text = FOREGROUND.equalsIgnoreCase(tree.getName()) ?
					"Push Annotations to Background Layer" : "Pull Annotations to Foreground Layer";
			var canvasName = FOREGROUND.equalsIgnoreCase(tree.getName()) ? BACKGROUND : FOREGROUND;
			var factory = new ReorderSelectedAnnotationsTaskFactory(dingRenderer, canvasName);
			
			var mi = new JMenuItem(text);
			mi.addActionListener(evt -> taskMgr.execute(factory.createTaskIterator(re.getViewModel())));
			popup.add(mi);
			mi.setEnabled(factory.isReady(re.getViewModel()));
		}
		
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private AnnotationNode getNodeAt(JTree tree, Point point) {
		var path = tree.getPathForLocation(point.x, point.y);
		
		return path == null ? null : (AnnotationNode) path.getLastPathComponent();
	}
	
	private void addEscapePressedListener() {
	    Toolkit.getDefaultToolkit().addAWTEventListener(escapePressedListener, AWTEvent.KEY_EVENT_MASK);
	}
	
	private void removeEscapePressedListener() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(escapePressedListener);
	}
	
	private void setKeyBindings(JComponent comp) {
		var actionMap = comp.getActionMap();
		var inputMap = comp.getInputMap(JComponent.WHEN_FOCUSED);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KeyAction.VK_DELETE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KeyAction.VK_DELETE);
		
		actionMap.put(KeyAction.VK_DELETE, new KeyAction(KeyAction.VK_DELETE));
	}
	
	@SuppressWarnings("serial")
	private class KeyAction extends AbstractAction {

		final static String VK_DELETE = "VK_DELETE";
		
		KeyAction(String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			var cmd = evt.getActionCommand();
			
			if (cmd.equals(VK_DELETE)) {
				removeSelectedAnnotations();
			}
		}
	}
	
	private class EscapePressedListener implements AWTEventListener {
		
		@Override
		public void eventDispatched(AWTEvent evt) {
			if (evt instanceof KeyEvent) {
				var key = (KeyEvent) evt;
				
				if (key.getID() == KeyEvent.KEY_PRESSED && key.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// Deselect the currently selected add button, if there's one,
					// in order to cancel the click-to-add-annotation action
					var btn = mainPanel.getSelectedAnnotationButton();
					
					if (btn != null && btn.isEnabled() && btn.isSelected())
						btn.doClick();
				}
			}
		}
	}
}
