package org.cytoscape.view.vizmap.gui.internal.view.editor;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentRenderingEngineListener;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.C2CEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.C2DEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.GradientEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyDiscreteValuePropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.MappingTypeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.DiscreteValueEditor;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

public class EditorManagerImpl implements EditorManager {

	private static final PropertyEditorRegistry REGISTRY = new PropertyEditorRegistry();

	// FIXME: We need better mechanism to manage icon sizes.
	private static final Map<Class<? extends VisualPropertyValue>, Integer> ICON_WIDTH_MAP = new HashMap<Class<? extends VisualPropertyValue>, Integer>();
	private static final int ICON_W = 14;
	private static final int ICON_H = 14;
	
	static {
		ICON_WIDTH_MAP.put(LineType.class, ICON_W * 3);
		ICON_WIDTH_MAP.put(ArrowShape.class, ICON_W * 3);
	}

	private final Map<Class<?>, VisualPropertyEditor<?>> editors;
	private final Map<String, PropertyEditor> comboBoxEditors;
	private final Map<Class<?>, ListEditor> attrComboBoxEditors;
	private final Map<Class<?>, ValueEditor<?>> valueEditors;
	private final Map<Class<?>, VisualPropertyValueEditor<?>> vizPropValueEditors;

	private final PropertyEditor mappingTypeEditor;
	private final ServicesUtil servicesUtil;
	
	private final Object mutex = new Object();

	/**
	 * Creates a new EditorFactory object.
	 * @param cellRendererFactory 
	 */
	public EditorManagerImpl(
			AttributeSetProxy attrProxy,
			MappingFunctionFactoryProxy mappingFactoryProxy,
			ServicesUtil servicesUtil
	) {
		this.servicesUtil = servicesUtil;

		editors = new HashMap<>();
		comboBoxEditors = new HashMap<>();
		valueEditors = new HashMap<>();
		vizPropValueEditors = new HashMap<>();
		
		var appMgr = servicesUtil.get(CyApplicationManager.class);
		var netMgr = servicesUtil.get(CyNetworkManager.class);
		var presMgr = servicesUtil.get(CyColumnPresentationManager.class);
		
		// Create attribute (Column Name) editors
		var nodeAttrEditor = new AttributeComboBoxPropertyEditor(GraphObjectType.node(), attrProxy, appMgr, netMgr, presMgr);
		var edgeAttrEditor = new AttributeComboBoxPropertyEditor(GraphObjectType.edge(), attrProxy, appMgr, netMgr, presMgr);
		var networkAttrEditor = new AttributeComboBoxPropertyEditor(GraphObjectType.network(), attrProxy, appMgr, netMgr, presMgr);
//		var columnAttrEditor = new AttributeComboBoxPropertyEditor(CyColumn.class, attrProxy, appMgr, netMgr, presMgr);
		
		attrComboBoxEditors = new HashMap<>();
		attrComboBoxEditors.put(nodeAttrEditor.getTargetObjectType(), nodeAttrEditor);
		attrComboBoxEditors.put(edgeAttrEditor.getTargetObjectType(), edgeAttrEditor);
		attrComboBoxEditors.put(networkAttrEditor.getTargetObjectType(), networkAttrEditor);
//		attrComboBoxEditors.put(columnAttrEditor.getTargetObjectType(), columnAttrEditor);

		// Create Mapping Type editor
		mappingTypeEditor = new MappingTypeComboBoxPropertyEditor(mappingFactoryProxy);
		comboBoxEditors.put("mappingTypeEditor", mappingTypeEditor);

		var vmMgr = servicesUtil.get(VisualMappingManager.class);
		var lexSet = vmMgr.getAllVisualLexicon();

		for (var lex : lexSet) {
			buildDiscreteEditors(lex);
		}
	}

	public AttributeComboBoxPropertyEditor getNodeEditor() {
		return (AttributeComboBoxPropertyEditor) attrComboBoxEditors.get(CyNode.class);
	}

	public AttributeComboBoxPropertyEditor getEdgeEditor() {
		return (AttributeComboBoxPropertyEditor) attrComboBoxEditors.get(CyEdge.class);
	}

	public AttributeComboBoxPropertyEditor getNetworkEditor() {
		return (AttributeComboBoxPropertyEditor) attrComboBoxEditors.get(CyNetwork.class);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void addValueEditor(ValueEditor<?> ve, Map properties) {
		synchronized (mutex) {
			valueEditors.put(ve.getValueType(), ve);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void removeValueEditor(ValueEditor<?> ve, Map properties) {
		synchronized (mutex) {
			valueEditors.remove(ve.getValueType());
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void addVisualPropertyValueEditor(VisualPropertyValueEditor<?> ve, Map properties) {
		synchronized (mutex) {
			vizPropValueEditors.put(ve.getValueType(), ve);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void removeVisualPropertyValueEditor(VisualPropertyValueEditor<?> ve, Map properties) {
		synchronized (mutex) {
			vizPropValueEditors.remove(ve.getValueType());
		}
	}

	@SuppressWarnings("rawtypes")
	public void addVisualPropertyEditor(VisualPropertyEditor<?> vpEditor, Map properties) {
		synchronized (mutex) {
			editors.put(vpEditor.getType(), vpEditor);
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeVisualPropertyEditor(VisualPropertyEditor<?> vpEditor, Map properties) {
		synchronized (mutex) {
			editors.remove(vpEditor.getType());
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <V> V showVisualPropertyValueEditor(Component parentComponent, VisualProperty<V> vp, V initialValue)
			throws Exception {
		V newValue = null;
		var range = vp.getRange();
		var valueType = range.getType();
		var vizPropEditor = (VisualPropertyValueEditor<V>) vizPropValueEditors.get(valueType);
		var editor = (ValueEditor<V>) valueEditors.get(valueType);

		if (vizPropEditor != null) {
			newValue = vizPropEditor.showEditor(parentComponent, initialValue, vp);
		} else if (editor != null) {
			newValue = editor.showEditor(parentComponent, initialValue);
		} else {
			throw new IllegalStateException("No value editor for " + vp.getDisplayName() + " is available.");
		}

		// Null is valid return value. It's from "Cancel" button.
		if (newValue == null)
			return null;

		if (range.inRange(newValue)) {
			return newValue;
		} else {
			var msg = "Value is out-of-range.";
			
			if (range instanceof ContinuousRange)
				msg = msg + ": " + ((ContinuousRange) range).getMin() + " to " + ((ContinuousRange) range).getMax();
			
			JOptionPane.showMessageDialog(parentComponent, msg, "Invalid Value", JOptionPane.ERROR_MESSAGE);

			return initialValue;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> VisualPropertyEditor<V> getVisualPropertyEditor(VisualProperty<V> vp) {
		synchronized (mutex) {
			return (VisualPropertyEditor<V>) editors.get(vp.getRange().getType());
		}
	}

	@Override
	public List<PropertyEditor> getCellEditors() {
		var ret = new ArrayList<PropertyEditor>();

		for (var type : editors.keySet())
			ret.add(editors.get(type).getPropertyEditor());

		return ret;
	}

	@Override
	public PropertyEditor getDefaultComboBoxEditor(String editorName) {
		var editor = comboBoxEditors.get(editorName);
		
		if (editor == null) {
			editor = new CyComboBoxPropertyEditor();
			comboBoxEditors.put(editorName, editor);
		}
		
		return editor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> ValueEditor<V> getValueEditor(Class<V> dataType) {
		synchronized (mutex) {
			return (ValueEditor<V>) valueEditors.get(dataType);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> VisualPropertyValueEditor<V> getVisualPropertyValueEditor(VisualProperty<V> vp) {
		synchronized (mutex) {
			return (VisualPropertyValueEditor<V>) vizPropValueEditors.get(vp.getRange().getType());
		}
	}
	
	/**
	 * Editor name is NODE, EDGE, or NETWORK.
	 */
	@Override
	public PropertyEditor getDataTableComboBoxEditor(Class<? extends CyIdentifiable> targetObjectType) {
		ListEditor editor = attrComboBoxEditors.get(targetObjectType);
		if (editor == null)
			throw new IllegalArgumentException("No such list editor: " + targetObjectType);
		return (PropertyEditor) editor;
	}

	@Override
	public Collection<PropertyEditor> getAttributeSelectors() {
		var selectors = new HashSet<PropertyEditor>();
		
		for (var selector : attrComboBoxEditors.values())
			selectors.add((PropertyEditor) selector);
		
		return selectors;
	}

	@Override
	public PropertyEditor getMappingFunctionSelector() {
		return mappingTypeEditor;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <V> void buildDiscreteEditors(VisualLexicon lexicon) {
		Set<VisualProperty> vps = (Set) lexicon.getAllVisualProperties();

		for (var vp : vps) {
			var range = vp.getRange();

			// If data type is basic (String, Boolean, etc.), custom editor is not necessary.
			var targetDataType = range.getType();
			
			synchronized (mutex) {
				if (REGISTRY.getEditor(targetDataType) != null)
					continue;
	
				if (getVisualPropertyEditor(vp) != null)
					continue;
	
				if (range instanceof DiscreteRange<?>) {
					var valEditor = (DiscreteValueEditor) getVisualPropertyValueEditor(vp);
					
					if (valEditor == null) {
						valEditor = new DiscreteValueEditor(range.getType(), ((DiscreteRange) range).values(),
								servicesUtil);
						addVisualPropertyValueEditor(valEditor, null);
					}
	
					var discretePropEditor = new CyDiscreteValuePropertyEditor(valEditor);
					
					var values = ((DiscreteRange) range).values();
					// FIXME how can we manage the custom icon size based on value type?
					var width = ICON_WIDTH_MAP.get(range.getType());
					
					if (width == null)
						width = ICON_W;
					
					var cellRendererFactory = servicesUtil.get(ContinuousMappingCellRendererFactory.class);
					var vpEditor = new DiscreteValueVisualPropertyEditor(range.getType(), discretePropEditor,
							cellRendererFactory, values, width, ICON_H);
					
					addVisualPropertyEditor(vpEditor, null);
					servicesUtil.registerService(vpEditor, SetCurrentRenderingEngineListener.class, new Properties());
				}
			}
		}
	}

	public void addRenderingEngineFactory(RenderingEngineFactory<?> factory, Map<?, ?> props) {
		var lexicon = factory.getVisualLexicon();
		buildDiscreteEditors(lexicon);
	}

	public void removeRenderingEngineFactory(RenderingEngineFactory<?> factory, Map<?, ?> props) {
		// TODO: clean up state when rendering engines are removed.
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PropertyEditor getContinuousEditor(VisualProperty<?> vp) {
		var editorType = getVisualPropertyEditor(vp).getContinuousEditorType();

		if (editorType == ContinuousEditorType.COLOR)
			return new GradientEditor(this, servicesUtil);
		else if (editorType == ContinuousEditorType.CONTINUOUS)
			return new C2CEditor(this, servicesUtil);
		else if (editorType == ContinuousEditorType.DISCRETE)
			return new C2DEditor(this, servicesUtil);

		return null;
	}
}
