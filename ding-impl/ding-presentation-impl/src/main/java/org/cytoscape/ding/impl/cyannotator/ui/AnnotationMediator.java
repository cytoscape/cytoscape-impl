package org.cytoscape.ding.impl.cyannotator.ui;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.annotations.Annotation.BACKGROUND;
import static org.cytoscape.view.presentation.annotations.Annotation.FOREGROUND;

import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.AnnotationNode;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTask;
import org.cytoscape.ding.impl.cyannotator.tasks.GroupAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.RemoveAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderAnnotationsTask;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderSelectedAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.UngroupAnnotationsTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
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

/**
 * This class mediates the communication between the Annotations UI and the rest of Cytoscape.
 */
public class AnnotationMediator implements CyStartListener, CyShutdownListener, SessionAboutToBeLoadedListener,
		SessionLoadedListener, NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener,
		SetCurrentNetworkViewListener, PropertyChangeListener,
		CytoPanelComponentSelectedListener {

	private AnnotationMainPanel mainPanel;
	private final Map<String, AnnotationFactory<? extends Annotation>> factories = new LinkedHashMap<>();
	private boolean appStarted;
	private boolean loadingSession;
	private boolean ignoreSelectedPropChangeEvents;
	
	private ClickToAddAnnotationListener clickToAddAnnotationListener;
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public AnnotationMediator(CyServiceRegistrar serviceRegistrar) {
		super();
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(CyStartEvent evt) {
		final HashSet<AnnotationFactory<? extends Annotation>> set = new LinkedHashSet<>(factories.values());
		
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
			mainPanel.getRemoveAnnotationsButton().addActionListener(e -> removeAnnotations());
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
		
		final Set<CyNetworkView> allViews = serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViewSet();
		DingRenderer dingRenderer = serviceRegistrar.getService(DingRenderer.class);
		
		allViews.forEach(view -> {
			DRenderingEngine re = dingRenderer.getRenderingEngine(view);
			if (re != null) {
				addPropertyListeners(re);
				addPropertyListeners(re.getCyAnnotator().getAnnotations());
			}
		});
		
		DRenderingEngine re = getCurrentDRenderingEngine();
		invokeOnEDT(() -> mainPanel.update(re));
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent evt) {
		if (!appStarted || loadingSession)
			return;
		
		CyNetworkView view = evt.getNetworkView();
		DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re != null) {
			addPropertyListeners(re);
			addPropertyListeners(re.getCyAnnotator().getAnnotations());
		}
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent evt) {
		CyNetworkView view = evt.getNetworkView();
		DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		
		if (re != null) {
			removePropertyListeners(re);
			removePropertyListeners(re.getCyAnnotator().getAnnotations());
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent evt) {
		if (appStarted && !loadingSession) {
			mainPanel.clearAnnotationButtonSelection();
			CyNetworkView view = evt.getNetworkView();
			DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
			
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
		
		DRenderingEngine re = getCurrentDRenderingEngine();
		CyAnnotator cyAnnotator = re != null ? re.getCyAnnotator() : null;
		
		Object source = evt.getSource();
		
		if (source.equals(cyAnnotator)) {
			if ("annotations".equals(evt.getPropertyName())) {
				// First remove property listeners from deleted annotations and add them to the new ones
				Set<Annotation> oldList = mainPanel.getAllAnnotations();
				List<Annotation> newList = cyAnnotator.getAnnotations();
				oldList.removeAll(newList);
				removePropertyListeners(oldList);
				addPropertyListeners((Collection<Annotation>) evt.getNewValue());
				// Now update the UI
				invokeOnEDT(() -> mainPanel.update(re));
			} else if ("annotationsReordered".equals(evt.getPropertyName())) {
				if (re != null && re.equals(mainPanel.getRenderingEngine())) {
					invokeOnEDT(() -> mainPanel.updateAnnotationsOrder());
				}
			}
		} else if (source instanceof Annotation) {
			if ("selected".equals(evt.getPropertyName()) && !ignoreSelectedPropChangeEvents) {
				if (re != null && re.equals(mainPanel.getRenderingEngine())) {
					invokeOnEDT(() -> mainPanel.setSelected((Annotation) source, (boolean) evt.getNewValue()));
				}
			}
		}
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
	
	private void addAnnotationButton(AnnotationFactory<? extends Annotation> f) {
		final JToggleButton btn = mainPanel.addAnnotationButton(f);
		btn.addItemListener(evt -> {
			disposeClickToAddAnnotationListener();
			int state = evt.getStateChange();
			
			if (state == ItemEvent.SELECTED) {
				DRenderingEngine re = getCurrentDRenderingEngine();
				
				if (re != null) {
					clickToAddAnnotationListener = new ClickToAddAnnotationListener(re, f);
					re.addMouseListener(clickToAddAnnotationListener);
				}
			}
		});
	}

	private void createAnnotation(DGraphView view, AnnotationFactory<? extends Annotation> f, Point point) {
		if (view == null || f instanceof AbstractDingAnnotationFactory == false)
			return; // For now, only DING annotations are supported!
		
		TaskIterator iterator = new TaskIterator(new AddAnnotationTask(view, point, f));
		serviceRegistrar.getService(DialogTaskManager.class).execute(iterator);
	}

	private DRenderingEngine getCurrentDRenderingEngine() {
		CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		return re;
	}
	
	private void selectAnnotationsFromSelectedRows() {
		DRenderingEngine re = mainPanel.getRenderingEngine();
		
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		final List<Annotation> all = re.getCyAnnotator().getAnnotations();
		
		if (all != null && !all.isEmpty()) {
			ignoreSelectedPropChangeEvents = true;
			
			try {
				final Collection<Annotation> selList = mainPanel.getSelectedAnnotations();
				all.forEach(a -> a.setSelected(selList.contains(a)));
			} finally {
				ignoreSelectedPropChangeEvents = false;
			}
		}
	}
	
	private void groupAnnotations() {
		DRenderingEngine re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		Collection<DingAnnotation> selList = mainPanel.getSelectedAnnotations(DingAnnotation.class);

		if (!selList.isEmpty()) {
			GroupAnnotationsTask task = new GroupAnnotationsTask(re, selList);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void ungroupAnnotations() {
		DRenderingEngine re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		Collection<GroupAnnotation> selList = mainPanel.getSelectedAnnotations(GroupAnnotation.class);

		if (!selList.isEmpty()) {
			UngroupAnnotationsTask task = new UngroupAnnotationsTask(re, selList);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void removeAnnotations() {
		DRenderingEngine re = mainPanel.getRenderingEngine();
		
		if (re == null)
			return;
		
		Collection<Annotation> selList = mainPanel.getSelectedAnnotations();
		
		if (!selList.isEmpty()) {
			TaskIterator iterator = new TaskIterator(new RemoveAnnotationsTask(re, selList, serviceRegistrar));
			serviceRegistrar.getService(DialogTaskManager.class).execute(iterator);
		}
	}
	
	private void moveAnnotationsToCanvas(String canvasName) {
		DGraphView view = getCurrentDGraphView();
		
		if (view != null) {
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(canvasName);
			serviceRegistrar.getService(DialogTaskManager.class).execute(factory.createTaskIterator(view));
		}
	}
	
	private void reorderAnnotations(String canvas, JTree tree, Shift shift) {
		DGraphView view = getCurrentDGraphView();
		
		if (view != null) {
			List<DingAnnotation> annotations = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);
			ReorderAnnotationsTask task = new ReorderAnnotationsTask(view, annotations, null, shift);
			serviceRegistrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}
	
	private void addPropertyListeners(DRenderingEngine re) {
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		removePropertyListeners(re);
		re.getCyAnnotator().addPropertyChangeListener("annotations", this);
		re.getCyAnnotator().addPropertyChangeListener("annotationsReordered", this);
	}
	
	private void removePropertyListeners(DRenderingEngine re) {
		if (re == null || re.getCyAnnotator() == null)
			return;
		
		re.getCyAnnotator().removePropertyChangeListener("annotations", this);
		re.getCyAnnotator().removePropertyChangeListener("annotationsReordered", this);
	}
	
	private void addPropertyListeners(Collection<Annotation> list) {
		if (list != null)
			list.forEach(a -> {
				if (a instanceof DingAnnotation) {
					((DingAnnotation) a).removePropertyChangeListener("selected", this);
					((DingAnnotation) a).addPropertyChangeListener("selected", this);
				}
			});
	}
	
	private void removePropertyListeners(Collection<Annotation> list) {
		if (list != null)
			list.forEach(a -> {
				if (a instanceof DingAnnotation)
					((DingAnnotation) a).removePropertyChangeListener("selected", this);
			});
	}
	
	private void disposeClickToAddAnnotationListener() {
		invokeOnEDT(() -> {
			if (clickToAddAnnotationListener != null) {
				clickToAddAnnotationListener.getView().removeMouseListener(clickToAddAnnotationListener);
				clickToAddAnnotationListener = null;
			}
		});
	}
	
	private void maybeShowPopupMenu(JTree tree, MouseEvent e) {
		// Ignore if not valid trigger.
		if (!e.isPopupTrigger())
			return;

		DGraphView view = getCurrentDGraphView();
		
		if (view == null)
			return;
		
		// If the item is not selected, select it first
		AnnotationNode node = getNodeAt(tree, e.getPoint());
		List<DingAnnotation> annotations = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);

		if (node != null && !annotations.contains(node.getAnnotation()))
			mainPanel.setSelected((Annotation) node.getAnnotation(), true);
		
		final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
		final JPopupMenu popup = new JPopupMenu();

		// Edit
		{
			List<DingAnnotation> list = mainPanel.getSelectedAnnotations(tree, DingAnnotation.class);
			DingAnnotation a = list.size() == 1 ? list.get(0) : null;
			
			JMenuItem mi = new JMenuItem("Modify Annotation...");
			mi.addActionListener(evt -> {
				JDialog dialog = a != null ? a.getModifyDialog() : null;

				if (dialog != null) {
					dialog.setLocationRelativeTo(mainPanel);
					dialog.setVisible(true);
				}
			});
			popup.add(mi);
			mi.setEnabled(a != null && !(a instanceof GroupAnnotation));
		}
		popup.addSeparator();
		// Reorder
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Shift.TO_FRONT);
			JMenuItem mi = new JMenuItem("Bring Annotations to Front");
			mi.addActionListener(evt -> {
				taskMgr.execute(factory.createTaskIterator(view));
			});
			popup.add(mi);
			mi.setEnabled(factory.isReady(view));
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Shift.TO_BACK);
			JMenuItem mi = new JMenuItem("Send Annotations to Back");
			mi.addActionListener(evt -> {
				taskMgr.execute(factory.createTaskIterator(view));
			});
			popup.add(mi);
			mi.setEnabled(factory.isReady(view));
		}
		popup.addSeparator();
		{
			String text = FOREGROUND.equalsIgnoreCase(tree.getName()) ?
					"Push Annotations to Background Layer" : "Pull Annotations to Foreground Layer";
			String canvasName = FOREGROUND.equalsIgnoreCase(tree.getName()) ? BACKGROUND : FOREGROUND;
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(canvasName);
			
			JMenuItem mi = new JMenuItem(text);
			mi.addActionListener(evt -> taskMgr.execute(factory.createTaskIterator(view)));
			popup.add(mi);
			mi.setEnabled(factory.isReady(view));
		}
		
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private AnnotationNode getNodeAt(JTree tree, Point point) {
		TreePath path = tree.getPathForLocation(point.x, point.y);
        
		return path == null ? null : (AnnotationNode) path.getLastPathComponent();
	}
	
	private class ClickToAddAnnotationListener extends MouseAdapter {
		
		private final DGraphView view;
		private final AnnotationFactory<? extends Annotation> factory;

		public ClickToAddAnnotationListener(DGraphView view, AnnotationFactory<? extends Annotation> f) {
			this.view = view;
			this.factory = f;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
				mainPanel.clearAnnotationButtonSelection();
				createAnnotation(view, factory, e.getPoint());
			}
		}
		
		public DGraphView getView() {
			return view;
		}
	}
}
