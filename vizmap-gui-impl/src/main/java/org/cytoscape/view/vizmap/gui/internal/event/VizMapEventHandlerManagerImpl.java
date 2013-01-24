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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandler;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandlerManager;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.cytoscape.view.vizmap.gui.internal.VizMapPropertySheetBuilder;
import org.cytoscape.view.vizmap.gui.internal.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class VizMapEventHandlerManagerImpl implements VizMapEventHandlerManager, PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(VizMapEventHandlerManagerImpl.class);

	// This event is used in PropertyEditor object.
	private static final String VALUE = "VALUE";

	private Map<String, VizMapEventHandler> eventHandlers;

	private final EditorManager editorManager;
	private VizMapPropertySheetBuilder vizMapPropertySheetBuilder;

	private final CyNetworkTableManager tableMgr;
	private final CyApplicationManager applicationManager;

	private final AttributeSetManager attrManager;

	private final VizMapperUtil util;
	private final VisualMappingManager vmm;

	public VizMapEventHandlerManagerImpl(final VisualMappingManager vmm, final EditorManager editorManager,
			final VizMapPropertySheetBuilder vizMapPropertySheetBuilder, final PropertySheetPanel propertySheetPanel,
			final VizMapperMainPanel gui, final CyNetworkTableManager tableMgr,
			final CyApplicationManager applicationManager, final AttributeSetManager attrManager,
			final VizMapperUtil util) {
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
		this.editorManager = editorManager;
		this.tableMgr = tableMgr;
		this.applicationManager = applicationManager;
		this.attrManager = attrManager;
		this.util = util;
		this.vmm = vmm;

		registerCellEditorListeners();

		eventHandlers = new HashMap<String, VizMapEventHandler>();
		createHandlers(propertySheetPanel);
	}

	private void createHandlers(PropertySheetPanel propertySheetPanel) {
		AbstractVizMapEventHandler windowEventHandler = new EditorWindowEventHandler();

		// FIXME
		eventHandlers.put(EditorManager.EDITOR_WINDOW_CLOSED, windowEventHandler);
		eventHandlers.put(EditorManager.EDITOR_WINDOW_OPENED, windowEventHandler);

		// Create handler for local property editor event.
		eventHandlers.put(VALUE, new CellEditorEventHandler(propertySheetPanel, tableMgr, applicationManager,
				vizMapPropertySheetBuilder, attrManager, util, vmm));
	}

	/*
	 * Register listeners for editors.
	 */
	private void registerCellEditorListeners() {
		// FIXME
		for (PropertyEditor p : editorManager.getCellEditors())
			p.addPropertyChangeListener(this);

		logger.debug("New Cell Editor registered: " + editorManager.getCellEditors().size());

		for (final PropertyEditor p : editorManager.getAttributeSelectors())
			p.addPropertyChangeListener(this);

		// Add Mapping type editor: continuous, discrete, or passthrough.
		final PropertyEditor mappingSelector = editorManager.getMappingFunctionSelector();
		mappingSelector.addPropertyChangeListener(this);
	}

	// Called through OSGi service listener mechanism.
	public void registerPCL(RenderingEngineFactory<?> factory, Map props) {
		registerCellEditorListeners();
	}

	public void unregisterPCL(RenderingEngineFactory<?> factory, Map props) {
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

}
