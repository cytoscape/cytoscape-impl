package org.cytoscape.view.vizmap.gui.internal.view;

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

import java.awt.Component;
import java.beans.PropertyEditor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.MappingFunctionFactoryManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;

/**
 * Create property for the Property Sheet object.
 */
public class VizMapPropertyBuilder {

	public static final String COLUMN = "Column";
	public static final String MAPPING_TYPE = "Mapping Type";
	public static final String GRAPHICAL_MAP_VIEW = "Graphical View";
	
	private static final Logger logger = LoggerFactory.getLogger(VizMapPropertyBuilder.class);

	private final DefaultTableCellRenderer defaultTableCellRenderer;

	private final EditorManager editorManager;
	private final MappingFunctionFactoryManager mappingFactoryManager;
	private final ServicesUtil servicesUtil;

	public VizMapPropertyBuilder(final EditorManager editorManager,
			final MappingFunctionFactoryManager mappingFactoryManager, final ServicesUtil servicesUtil) {
		this.editorManager = editorManager;
		this.mappingFactoryManager = mappingFactoryManager;
		this.servicesUtil = servicesUtil;
		this.defaultTableCellRenderer = new DefaultVizMapTableCellRenderer();
	}

	/**
	 * Build the properties for a new Visual Mapping Function.
	 * @param <V> data type of Visual Property.
	 */
	public <V> void buildProperty(final VisualProperty<V> visualProperty, final PropertySheetPanel propertySheetPanel) {
		if (visualProperty == null)
			throw new IllegalArgumentException("'visualProperty' must not be null.");
		if (propertySheetPanel == null)
			throw new IllegalArgumentException("'propertySheetPanel' must not be null.");
		
		// TODO: Refactor--create new view component for mapping editor???
		final VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory> columnProp = 
				new VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory>(
						CellType.VISUAL_PROPERTY_TYPE, visualProperty, String.class);

		// Build Property object
		columnProp.setDisplayName(COLUMN);
		((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
				columnProp, defaultTableCellRenderer);
		
		final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, V>> mapTypeProp = 
				new VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, V>>(
						CellType.MAPPING_TYPE, MAPPING_TYPE, VisualMappingFunctionFactory.class);
		
		mapTypeProp.setDisplayName(MAPPING_TYPE);
		((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
				mapTypeProp, defaultTableCellRenderer);

		propertySheetPanel.addProperty(0, columnProp);
		propertySheetPanel.addProperty(1, mapTypeProp);
		propertySheetPanel.repaint();
		
		final PropertyEditorRegistry propEditorRegistry = 
				(PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory();
		propEditorRegistry.registerEditor(columnProp, 
				editorManager.getDataTableComboBoxEditor((Class<? extends CyIdentifiable>) visualProperty.getTargetDataType()));
		propEditorRegistry.registerEditor(mapTypeProp, editorManager.getDefaultComboBoxEditor("mappingTypeEditor"));
	}
	
	/**
	 * Build the properties for an existing Visual Mapping Function.
	 * @param <K> data type of attribute to be mapped.
	 * @param <V> data type of Visual Property.
	 */
	public <K, V> void buildProperty(final VisualMappingFunction<K, V> visualMapping,
									 final PropertySheetPanel propertySheetPanel,
									 final VisualMappingFunctionFactory factory) {
		if (visualMapping == null)
			throw new IllegalArgumentException("'visualMapping' must not be null.");
		if (propertySheetPanel == null)
			throw new IllegalArgumentException("'propertySheetPanel' must not be null.");
		if (factory == null)
			throw new IllegalArgumentException("'factory' must not be null.");

		final VisualProperty<V> vp = visualMapping.getVisualProperty();
		final VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory> columnProp = 
				new VizMapperProperty<VisualProperty<V>, String, VisualMappingFunctionFactory>(
						CellType.VISUAL_PROPERTY_TYPE, vp, String.class);

		// Build Property object
		columnProp.setDisplayName(COLUMN);
		columnProp.setValue(visualMapping.getMappingColumnName());
		columnProp.setInternalValue(factory);

		final String attrName = visualMapping.getMappingColumnName();
		final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<K, V>> mapTypeProp = 
				new VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<K, V>>(
						CellType.MAPPING_TYPE, MAPPING_TYPE, VisualMappingFunctionFactory.class);

		if (attrName == null)
			columnProp.setValue(null);
			
		((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
				columnProp, defaultTableCellRenderer);

		mapTypeProp.setDisplayName(MAPPING_TYPE);
		mapTypeProp.setValue(factory); // Set mapping type as string.
		mapTypeProp.setInternalValue(visualMapping);
		
		((PropertyRendererRegistry) propertySheetPanel.getTable().getRendererFactory()).registerRenderer(
				mapTypeProp, defaultTableCellRenderer);

		// TODO: Should refactor factory.

		propertySheetPanel.addProperty(0, columnProp);
		propertySheetPanel.addProperty(1, mapTypeProp);
		
		final PropertyEditorRegistry propEditorRegistry = 
				(PropertyEditorRegistry) propertySheetPanel.getTable().getEditorFactory();
		propEditorRegistry.registerEditor(columnProp, 
				editorManager.getDataTableComboBoxEditor((Class<? extends CyIdentifiable>) vp.getTargetDataType()));
		propEditorRegistry.registerEditor(mapTypeProp, editorManager.getDefaultComboBoxEditor("mappingTypeEditor"));
		
		// Mapping Editor
		createMappingProperties(visualMapping, propertySheetPanel, factory);
	}

	public <K, V> void createMappingProperties(final VisualMappingFunction<K, V> visualMapping,
											   final PropertySheetPanel propertySheetPanel,
											   final VisualMappingFunctionFactory factory) {
		final String attrName = visualMapping.getMappingColumnName();

		if (attrName == null)
			return;
		
		removeMappingProperties(propertySheetPanel);
		
		final VisualProperty<V> vp = visualMapping.getVisualProperty();
		final VisualPropertyEditor<V> vpEditor = editorManager.getVisualPropertyEditor(vp);
		
		if (visualMapping instanceof DiscreteMapping) {
			// Discrete Mapping
			// This set should not contain null!
			final SortedSet<Object> attrSet = new TreeSet<Object>();
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			final CyNetwork network = appMgr.getCurrentNetwork();
			
			if (network != null) {
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
				
				if (vp.getTargetDataType() == CyNetwork.class) {
					final CyRow row = network.getRow(network);
					final CyColumn column = row.getTable().getColumn(attrName);
					
					if (column != null)
						processDiscretValues(row, attrName, column, column.getType(), attrSet);
				} else {
					// Make sure all data sets have the same data type.
					if (!graphObjects.isEmpty()) {
						final CyIdentifiable firstEntry = graphObjects.iterator().next();
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
			
			// Also keep current mapping entries that have non-null values
			for (final Map.Entry<K, V> entry : ((DiscreteMapping<K, V>)visualMapping).getAll().entrySet()) {
				if (entry.getValue() != null)
					attrSet.add(entry.getKey());
			}

			setDiscreteProps(vp, visualMapping, attrSet, vpEditor, propertySheetPanel);
		} else if (visualMapping instanceof ContinuousMapping) {
			// TODO: How do we decide when to reset the range tracer for this mapping?
			final VizMapperProperty<String, VisualMappingFunction, VisualMappingFunction<K, V>> graphicalView = 
					new VizMapperProperty<String, VisualMappingFunction, VisualMappingFunction<K, V>>(
							CellType.CONTINUOUS,
							visualMapping.getVisualProperty().getDisplayName() + "_" + GRAPHICAL_MAP_VIEW,
							visualMapping.getClass());

			graphicalView.setShortDescription("Continuous Mapping from " + visualMapping.getMappingColumnName()
					+ " to " + visualMapping.getVisualProperty().getDisplayName());
			graphicalView.setValue(visualMapping);
			graphicalView.setDisplayName("Current Mapping");
			
			propertySheetPanel.addProperty(2, graphicalView);
			
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
			}
		} else if (visualMapping instanceof PassthroughMapping && (attrName != null)) {
			// Doesn't need to display the mapped values!
		} else {
			throw new IllegalArgumentException("Unsupported mapping type: " + visualMapping);
		}
		
		propertySheetPanel.getTable().repaint();
		propertySheetPanel.repaint();
	}
	
	public VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> getColumnProperty(
			final PropertySheetPanel propSheetPnl) {
		final Property[] properties = propSheetPnl != null ? propSheetPnl.getProperties() : null;
		
		if (properties != null) {
			for (final Property p : properties) {
				if (p instanceof VizMapperProperty && 
						((VizMapperProperty)p).getCellType() == CellType.VISUAL_PROPERTY_TYPE)
					return (VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory>) p;
			}
		}
		
		return null;
	}
	
	public VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> getMappingTypeProperty(
			final PropertySheetPanel propSheetPnl) {
		final Property[] properties = propSheetPnl != null ? propSheetPnl.getProperties() : null;
		
		if (properties != null) {
			for (final Property p : properties) {
				if (p instanceof VizMapperProperty && 
						((VizMapperProperty)p).getCellType() == CellType.MAPPING_TYPE)
					return (VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>>) p;
			}
		}
		
		return null;
	}
	
	public VisualMappingFunctionFactory getMappingFactory(final VisualMappingFunction<?, ?> mapping) {
		if (mapping != null) {
			for (final VisualMappingFunctionFactory f : mappingFactoryManager.getFactories()) {
				final Class<?> type = f.getMappingFunctionType();
				
				if (type.isAssignableFrom(mapping.getClass()))
					return f;
			}
		}
		
		return null;
	}
	
	protected void removeMappingProperties(final PropertySheetPanel propertySheetPanel) {
		final Property[] properties = propertySheetPanel.getProperties();
		
		if (properties != null) {
			final PropertySheetTable table = propertySheetPanel.getTable();
			final PropertyRendererRegistry rendReg = (PropertyRendererRegistry) table.getRendererFactory();
			
			for (final Property p : properties) {
				if (p instanceof VizMapperProperty) {
					final CellType cellType = ((VizMapperProperty<?, ?, ?>) p).getCellType();
					
					if (cellType == CellType.CONTINUOUS || cellType == CellType.DISCRETE) {
						if (cellType == CellType.CONTINUOUS) {
							// TODO: Update range trace?
							rendReg.unregisterRenderer(p);
						}
						
						propertySheetPanel.removeProperty(p);
					}
				}
			}
			
			table.repaint();
			propertySheetPanel.repaint();
		}
	}

	private void processDiscretValues(final CyRow row, final String columnName, final CyColumn column,
			final Class<?> attrClass, final SortedSet<Object> attrSet) {

		if (column.getListElementType() != null) {
			// Expand list contents as a flat list.
			final List<?> list = row.getList(columnName, column.getListElementType());
			if (list != null) {
				for (final Object item : list) {
					if (item != null)
						attrSet.add(item != null ? item.toString() : null);
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

	/**
	 * Set value, title, and renderer for each property in the category. This
	 * list should be created against all available attribute values.
	 */
	private <K, V> void setDiscreteProps(VisualProperty<V> vp, VisualMappingFunction<K, V> mapping,
			SortedSet<Object> attrSet, VisualPropertyEditor<V> vpEditor,
			PropertySheetPanel propertySheetPanel) {
		if (attrSet == null)
			return;

		final Map<K, V> discMapping = ((DiscreteMapping<K, V>) mapping).getAll();

		V val;
		VizMapperProperty<K, V, VisualMappingFunction<K, V>> valProp;
		String strVal;

		final PropertySheetTable table = propertySheetPanel.getTable();
		final PropertyRendererRegistry cellRendererFactory = (PropertyRendererRegistry) table.getRendererFactory();
		final PropertyEditorRegistry cellEditorFactory = (PropertyEditorRegistry) table.getEditorFactory();

		for (final Object key : attrSet) {
			valProp = new VizMapperProperty<K, V, VisualMappingFunction<K, V>>(CellType.DISCRETE, (K) key, mapping
					.getVisualProperty().getRange().getType());
			strVal = key.toString();
			valProp.setDisplayName(strVal);

			// Get the mapped value

			// TODO: Is there a way to fix it when opening 2.x sessions?
			// Even if the CyColumn type is a List of Numbers or Booleans, the
			// Visual Style Serializer might have built the discrete mapping with keys as Strings!
			// In 2.x the session_vizmap.props format does not specify the type of the List-type attributes.
			// Example:
			// 
			//     nodeLabelColor.MyStyle-Node Label Color-Discrete Mapper.mapping.controllerType=-2
			//
			// In that case "controllerType=-2" means that the attribute type is List,
			// but we don't know the type of the list items.
			if (mapping.getMappingColumnType() == String.class && !(key instanceof String))
				val = discMapping.get(key.toString());
			else
				val = discMapping.get(key);

			if (val != null)
				valProp.setType(val.getClass());

			propertySheetPanel.addProperty(valProp);
			
			if (vpEditor != null) {
				final TableCellRenderer renderer = vpEditor.getDiscreteTableCellRenderer();
				
				if (renderer != null)
					cellRendererFactory.registerRenderer(valProp, renderer);

				PropertyEditor cellEditor;
				if (vpEditor instanceof VisualPropertyEditor2) {
					cellEditor = ((VisualPropertyEditor2) vpEditor).getPropertyEditor(vp);
				} else {
					cellEditor = vpEditor.getPropertyEditor();
				}

				if (cellEditor != null)
					cellEditorFactory.registerEditor(valProp, cellEditor);
			}
			
			valProp.setValue(val);
			valProp.setInternalValue(mapping);
		}
	}
	
	private static class DefaultVizMapTableCellRenderer extends DefaultTableCellRenderer {
		
		private static final long serialVersionUID = 3837281348887354114L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (c instanceof JLabel && value == null)
				((JLabel) c).setText("-- select value --");
			
			if (!isSelected)
				c.setForeground(UIManager.getColor("Label.disabledForeground"));
			
			return c;
		}
	}
}
