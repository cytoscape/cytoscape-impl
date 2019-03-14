package org.cytoscape.internal.util;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public final class Util {

	/**
	 * @return true if both objects are null or obj1 equals obj2.
	 */
	public static boolean same(final Object obj1, final Object obj2) {
		return (obj1 == null && obj2 == null) || (obj1 != null && obj1.equals(obj2));
	}
	
	/**
	 * @return true if both lists are empty or contain the same elements (the order of the elements is not important).
	 */
	public static boolean equalSets(final Collection<?> list1, final Collection<?> list2) {
		if ((list1 == null || list1.isEmpty()) && (list2 == null || list2.isEmpty()))
			return true; // Both are empty
		
		if (list1 != null && list2 != null) {
			final Set<Object> set1 = new HashSet<>(list1);
			final Set<Object> set2 = new HashSet<>(list2);
			
			if (set1.equals(set2))
				return true; // Both contain the same elements
		}
		
		return false;
	}
	
	public static Set<CyNetwork> getNetworks(final Collection<CyNetworkView> views) {
		final Set<CyNetwork> networks = new LinkedHashSet<>();
		
		for (CyNetworkView v : views)
			networks.add(v.getModel());
		
		return networks;
	}
	
	public static Set<CyNetworkView> getNetworkViews(final Collection<CyNetwork> networks,
			final CyServiceRegistrar serviceRegistrar) {
		final Set<CyNetworkView> views = new LinkedHashSet<>();
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetwork n : networks)
			views.addAll(netViewMgr.getNetworkViews(n));
		
		return views;
	}
	
	public static boolean isDisposed(final CyNetworkView view) {
		return view.getModel().getDefaultNodeTable() == null || view.getModel().getDefaultEdgeTable() == null;
	}
	
	public static boolean isVisualPropertySupported(final String vpId, final Class<?> type, final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar) {
		if (view == null)
			return false;
		
		VisualLexicon lexicon = getVisualLexicon(view, serviceRegistrar);
		
		if (lexicon == null) // Should not happen!
			return false;
		
		VisualProperty<?> vp = lexicon.lookup(type, vpId);
		
		return vp != null && lexicon.isSupported(vp);
	}
	
	/**
	 * Sets a {@link VisualProperty} value by property id, but only if it's supported by the view's lexicon.
	 */
	public static void setLockedValue(final String vpId, final Class<?> type, final Object value,
			final CyNetworkView view, final CyServiceRegistrar serviceRegistrar) {
		if (view == null)
			return;
		
		VisualLexicon lexicon = getVisualLexicon(view, serviceRegistrar);
		
		if (lexicon == null) // Should not happen!
			return;
		
		VisualProperty<?> vp = lexicon.lookup(type, vpId);
		
		if (vp != null && lexicon.isSupported(vp))
			view.setLockedValue(vp, value);
	}
	
	public static Object getVisualProperty(final String vpId, final Class<?> type,
			final CyNetworkView view, final CyServiceRegistrar serviceRegistrar) {
		if (view != null) {
			VisualLexicon lexicon = getVisualLexicon(view, serviceRegistrar);
			
			if (lexicon != null) { // Should never be null, but just in case...
				VisualProperty<?> vp = lexicon.lookup(type, vpId);
				
				if (vp != null && lexicon.isSupported(vp))
					return view.getVisualProperty(vp);
			}
		}
		
		return null;
	}
	
	public static VisualLexicon getVisualLexicon(final CyNetworkView view, final CyServiceRegistrar serviceRegistrar) {
    	final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
    	NetworkViewRenderer renderer = applicationManager.getNetworkViewRenderer(view.getRendererId());
		RenderingEngineFactory<CyNetwork> factory = renderer == null ? null
				: renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);
		
		return factory == null ? null : factory.getVisualLexicon();
	}
	
	public static double squarenessRatio(final double w, final double h) {
		return Math.abs(1.0 - (w > h ? w / h : h / w));
	}
	
	public static void maybeOpenSession(File file, Component owner, CyServiceRegistrar serviceRegistrar) {
		maybeOpenSession(file, owner, serviceRegistrar, null);
	}
	
	public static void maybeOpenSession(File file, Component owner, CyServiceRegistrar serviceRegistrar,
			TaskObserver observer) {
		if (file.exists() && file.canRead()) {
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				openSession(file, serviceRegistrar, observer);
			else
				openSessionWithWarning(file, owner, serviceRegistrar, observer);
		}
	}
	
	public static void openSession(File file, CyServiceRegistrar serviceRegistrar, TaskObserver observer) {
		final OpenSessionTaskFactory taskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		
		if (observer == null)
			taskManager.execute(taskFactory.createTaskIterator(file));
		else
			taskManager.execute(taskFactory.createTaskIterator(file), observer);
	}
	
	public static void openSessionWithWarning(File file, Component owner, CyServiceRegistrar serviceRegistrar,
			TaskObserver observer) {
		if (JOptionPane.showConfirmDialog(
				owner,
				"Current session (all networks and tables) will be lost.\nDo you want to continue?",
				"Open Session",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			openSession(file, serviceRegistrar, observer);
	}
	
	public static URL getURL(final String s) {
		try {
			if (s != null)
				return new URL(s);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Util() {
	}
}
