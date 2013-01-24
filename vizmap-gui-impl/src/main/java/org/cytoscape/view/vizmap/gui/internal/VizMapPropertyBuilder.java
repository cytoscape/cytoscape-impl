package org.cytoscape.view.vizmap.gui.internal;

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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;

/**
 * Create property for the Property Sheet object.
 */
public class VizMapPropertyBuilder {

	private static final Logger logger = LoggerFactory.getLogger(VizMapPropertyBuilder.class);

	private final DefaultTableCellRenderer emptyBoxRenderer;
	private final DefaultTableCellRenderer filledBoxRenderer;

	private final EditorManager editorManager;
	private final CyNetworkManager cyNetworkManager;
	private final CyApplicationManager appManager;

	public VizMapPropertyBuilder(CyNetworkManager cyNetworkManager, CyApplicationManager appManager, EditorManager editorManager,
			DefaultTableCellRenderer emptyBoxRenderer, DefaultTableCellRenderer filledBoxRenderer) {
		this.cyNetworkManager = cyNetworkManager;
		this.appManager = appManager;
		this.editorManager = editorManager;
		this.emptyBoxRenderer = emptyBoxRenderer;
		this.filledBoxRenderer = filledBoxRenderer;
	}

	/**
	 * Build one property for one visual property.
	 * 
	 * @param <K>
	 *            data type of attribute to be mapped.
	 * @param <V>
	 *            data type of Visual Property.
	 * 
	 */
	public <K, V> VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory> buildProperty(
			final VisualMappingFunction<K, V> visualMapping, final String categoryName,
			final PropertySheetPanel propertySheetPanel, final VisualMappingFunctionFactory factory) {

		// Mapping is empty
		if (visualMapping == null)
			throw new NullPointerException("Mapping is null.");
		if (categoryName == null)
			throw new NullPointerException(
					"Category is null.  It should be one of the following: NODE, EDGE, or NETWORK.");
		if (propertySheetPanel == null)
			throw new NullPointerException("PropertySheet is null.");

		final VisualProperty<V> vp = visualMapping.getVisualProperty();
		final VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory> topProperty = new VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory>(
				CellType.VISUAL_PROPERTY_TYPE, vp, String.class);

		// Build Property object
		topProperty.setCategory(categoryName);
		topProperty.setDisplayName(vp.getDisplayName());
		topProperty.setInternalValue(factory);

		final String attrName = visualMapping.getMappingColumnName();
		final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<K, V>> mappingHeader = new VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<K, V>>(
				CellType.MAPPING_TYPE, "Mapping Type", VisualMappingFunctionFactory.class);

		if (attrName == null) {
			topProperty.setValue("Select Column");
			((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
					topProperty, emptyBoxRenderer);
		} else {
			topProperty.setValue(attrName);
			((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
					topProperty, filledBoxRenderer);
		}

		mappingHeader.setDisplayName("Mapping Type");
		// Set mapping type as string.
		mappingHeader.setValue(factory);
		mappingHeader.setInternalValue(visualMapping);

		// Set parent-child relationship
		mappingHeader.setParentProperty(topProperty);
		topProperty.addSubProperty(mappingHeader);

		// TODO: Should refactor factory.
		((PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory()).registerEditor(mappingHeader,
				editorManager.getDefaultComboBoxEditor("mappingTypeEditor"));

		final CyNetwork network = appManager.getCurrentNetwork();
		final Set<CyIdentifiable> graphObjects = new HashSet<CyIdentifiable>();

		if (network != null) {
			if (vp.getTargetDataType().equals(CyNode.class)) {
				graphObjects.addAll(network.getNodeList());
			} else if (vp.getTargetDataType().equals(CyEdge.class)) {
				graphObjects.addAll(network.getEdgeList());
			} else if (vp.getTargetDataType().equals(CyNetwork.class)) {
				graphObjects.add(network);
			} else {
				throw new IllegalArgumentException("Data type not supported: " + vp.getTargetDataType());
			}
		}

		((PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory()).registerEditor(topProperty,
				editorManager.getDataTableComboBoxEditor((Class<? extends CyIdentifiable>) vp.getTargetDataType()));
		
		final VisualPropertyEditor<V> vpEditor = editorManager.getVisualPropertyEditor(vp);

		if (visualMapping instanceof DiscreteMapping && attrName != null) {
			// Discrete Mapping
			// This set should not contain null!
			final SortedSet<Object> attrSet = new TreeSet<Object>();
			
			if (network != null) {
				if (vp.getTargetDataType() == CyNetwork.class) {
					final CyRow row = network.getRow(network);
					final CyColumn column = row.getTable().getColumn(attrName);
					
					if (column != null)
						processDiscretValues(row, attrName, column, column.getType(), attrSet);
				} else {
					// Make sure all data sets have the same data type.
					if (!graphObjects.isEmpty()) {
						CyIdentifiable firstEntry = graphObjects.iterator().next();
						final CyRow firstRow = network.getRow(firstEntry);
						final CyColumn column = firstRow.getTable().getColumn(attrName);
						
						if (column != null) {
							final Class<?> type = column.getType();
							
							for (final CyIdentifiable go : graphObjects) {
								final CyRow row = network.getRow(go);
								processDiscretValues(row, attrName, column, type, attrSet);
							}
						}
					}
				}
			}

			// FIXME
			setDiscreteProps(vp, visualMapping, attrSet, vpEditor, topProperty, propertySheetPanel);

		} else if (visualMapping instanceof ContinuousMapping && (attrName != null)) {
			final VizMapperProperty<String, VisualMappingFunction, VisualMappingFunction<K, V>> graphicalView = 
					new VizMapperProperty<String, VisualMappingFunction, VisualMappingFunction<K, V>>(
							CellType.CONTINUOUS,
							visualMapping.getVisualProperty().getDisplayName() + "_" + AbstractVizMapperPanel.GRAPHICAL_MAP_VIEW,
							visualMapping.getClass());

			graphicalView.setShortDescription("Continuous Mapping from " + visualMapping.getMappingColumnName()
					+ " to " + visualMapping.getVisualProperty().getDisplayName());
			graphicalView.setValue(visualMapping);
			graphicalView.setDisplayName("Current Mapping");
			graphicalView.setParentProperty(topProperty);
			topProperty.addSubProperty(graphicalView);

			final PropertySheetTable table = propertySheetPanel.getTable();
			final PropertyRendererRegistry rendReg = (PropertyRendererRegistry) table.getRendererFactory();
			final PropertyEditorRegistry cellEditorFactory = (PropertyEditorRegistry) table.getEditorFactory();
			final PropertyEditor continuousCellEditor = editorManager.getContinuousEditor(vp);

			if (continuousCellEditor == null) {
				throw new NullPointerException("Continuous Mapping cell editor is null.");
			} else {
				// Renderer for Continuous mapping icon cell
				final TableCellRenderer continuousRenderer = vpEditor
						.getContinuousTableCellRenderer((ContinuousMappingEditor<? extends Number, V>) continuousCellEditor);
				rendReg.registerRenderer(graphicalView, continuousRenderer);
				continuousCellEditor.setValue(visualMapping);
				cellEditorFactory.registerEditor(graphicalView, continuousCellEditor);
				table.repaint();
			}
		} else if (visualMapping instanceof PassthroughMapping && (attrName != null)) {
			// Doesn't need to display the mapped values!
		} else {
			throw new IllegalArgumentException("Unsupported mapping type: " + visualMapping);
		}

		propertySheetPanel.addProperty(0, topProperty);

		return topProperty;
	}

	private void processDiscretValues(final CyRow row, final String columnName, final CyColumn column,
			final Class<?> attrClass, final SortedSet<Object> attrSet) {

		if (column.getListElementType() != null) {
			// Expand list contents as a flat list.
			final List<?> list = row.getList(columnName, column.getListElementType());
			if (list != null) {
				for (final Object item : list) {
					if (item != null)
						attrSet.add(item);
				}
			}
		} else {
			final Object id = row.get(columnName, attrClass);
			if (id != null) {
				if (id.getClass() != attrClass && id instanceof Number) {
					attrSet.add(NumberConverter.convert(attrClass, (Number) id));
				} else {
					try {
						attrSet.add(id);
					} catch (Exception e) {
						logger.debug(columnName + ": Invalid entry ignored", e);
					}
				}
			}
		}
	}

	/*
	 * Set value, title, and renderer for each property in the category. This
	 * list should be created against all available attribute values.
	 */
	private <K, V> void setDiscreteProps(VisualProperty<V> vp, VisualMappingFunction<K, V> mapping,
			SortedSet<Object> attrSet, VisualPropertyEditor<V> visualPropertyEditor, DefaultProperty parent,
			PropertySheetPanel propertySheetPanel) {
		if (attrSet == null)
			return;

		final Map<K, V> discMapping = ((DiscreteMapping<K, V>) mapping).getAll();

		V val = null;
		VizMapperProperty<K, V, VisualMappingFunction<K, V>> valProp;
		String strVal;

		final List<VizMapperProperty<K, V, VisualMappingFunction<K, V>>> children = new ArrayList<VizMapperProperty<K, V, VisualMappingFunction<K, V>>>();
		final PropertySheetTable table = propertySheetPanel.getTable();
		final PropertyRendererRegistry cellRendererFactory = (PropertyRendererRegistry) table.getRendererFactory();
		final PropertyEditorRegistry cellEditorFactory = (PropertyEditorRegistry) table.getEditorFactory();

		for (Object key : attrSet) {

			valProp = new VizMapperProperty<K, V, VisualMappingFunction<K, V>>(CellType.DISCRETE, (K) key, mapping
					.getVisualProperty().getRange().getType());
			strVal = key.toString();
			valProp.setDisplayName(strVal);
			valProp.setParentProperty(parent);

			// Get the mapped value

			// TODO: Is there a way to fix it when opening 2.x sessions?
			// Even if the CyColumn type is a List of Numbers or Booleans, the
			// Visual Style Serializer might have built the discrete mapping
			// with keys as Strings!
			// In 2.x the session_vizmap.props format does not specify the type
			// of the List-type attributes.
			// Example:
			// "nodeLabelColor.MyStyle-Node Label Color-Discrete Mapper.mapping.controllerType=-2"
			// In that case "controllerType=-2" means that the attribute type is
			// List, but we don't know the
			// type of the list items.
			if (mapping.getMappingColumnType() == String.class && !(key instanceof String))
				val = discMapping.get(key.toString());
			else
				val = discMapping.get(key);

			if (val != null)
				valProp.setType(val.getClass());

			children.add(valProp);
			final VisualPropertyEditor<V> editor = editorManager.getVisualPropertyEditor(vp);
						
			if (editor != null) {
				final TableCellRenderer renderer = editor.getDiscreteTableCellRenderer();
				if (renderer != null)
					cellRendererFactory.registerRenderer(valProp, renderer);
				
				final PropertyEditor cellEditor = editor.getPropertyEditor();

				if (cellEditor != null)
					cellEditorFactory.registerEditor(valProp, cellEditor);
			}
			
			valProp.setValue(val);
			valProp.setInternalValue(mapping);
		}

		// Add all children.
		parent.addSubProperties(children);
	}

}
