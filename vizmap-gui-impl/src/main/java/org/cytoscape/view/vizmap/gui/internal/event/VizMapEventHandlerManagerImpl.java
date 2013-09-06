package org.cytoscape.view.vizmap.gui.internal.event;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandler;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandlerManager;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;

public class VizMapEventHandlerManagerImpl implements VizMapEventHandlerManager, PropertyChangeListener {

	// This event is used in PropertyEditor object.
	private static final String VALUE = "VALUE";

	private final Map<String, VizMapEventHandler> eventHandlers;
	private final EditorManager editorManager;
	private final AttributeSetProxy attrManager;
	private final ServicesUtil servicesUtil;

	public VizMapEventHandlerManagerImpl(final EditorManager editorManager,
										 final AttributeSetProxy attrManager,
										 final ServicesUtil servicesUtil,
										 final VizMapPropertyBuilder vizMapPropertyBuilder,
										 final VizMapperMediator vizMapperMediator) {
		this.editorManager = editorManager;
		this.attrManager = attrManager;
		this.servicesUtil = servicesUtil;

		eventHandlers = new HashMap<String, VizMapEventHandler>();
		createHandlers(vizMapPropertyBuilder, vizMapperMediator);
	}

	/**
	 * Called through OSGi service listener mechanism.
	 */
	@SuppressWarnings("rawtypes")
	public void registerPCL(final RenderingEngineFactory<?> factory, final Map props) {
		registerCellEditorListeners();
	}

	/**
	 * Called through OSGi service listener mechanism.
	 */
	@SuppressWarnings("rawtypes")
	public void unregisterPCL(final RenderingEngineFactory<?> factory, final Map props) {
		// TODO implement this
	}

	@Override
	public VizMapEventHandler getHandler(final String name) {
		return eventHandlers.get(name);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e) {
		// Check caller
		final String handlerKey = e.getPropertyName();

		// Do nothing if key is null.
		if (handlerKey == null)
			return;

		final VizMapEventHandler handler = getHandler(handlerKey.toUpperCase());
		
		if (handler != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					handler.processEvent(e);
				}
			});
		}
	}

	private void createHandlers(final VizMapPropertyBuilder vizMapPropertyBuilder,
			final VizMapperMediator vizMapperMediator) {
		// Create handler for local property editor event.
		eventHandlers.put(VALUE, new CellEditorEventHandler(attrManager, servicesUtil, vizMapPropertyBuilder,
				vizMapperMediator));
	}

	/**
	 * Register listeners for editors.
	 */
	private void registerCellEditorListeners() {
		for (final PropertyEditor p : editorManager.getCellEditors()) {
			// First remove the listener to prevent adding it more than once
			p.removePropertyChangeListener(this);
			p.addPropertyChangeListener(this);
		}
		
		for (final PropertyEditor p : editorManager.getAttributeSelectors()) {
			// First remove the listener to prevent adding it more than once
			p.removePropertyChangeListener(this);
			p.addPropertyChangeListener(this);
		}

		// Add Mapping type editor: continuous, discrete, or passthrough.
		final PropertyEditor mappingSelector = editorManager.getMappingFunctionSelector();
		mappingSelector.addPropertyChangeListener(this);
	}
}
