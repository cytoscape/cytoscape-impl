/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal.editor;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.C2CEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.C2DEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.GradientEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.DiscreteValueEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;

/**
 *
 */
public class EditorManagerImpl implements EditorManager {

	private static final Logger logger = LoggerFactory.getLogger(EditorManagerImpl.class);
	
	private static final PropertyEditorRegistry REGISTRY = new PropertyEditorRegistry();

	private final Map<Class<?>, VisualPropertyEditor<?>> editors;

	private final Map<String, PropertyEditor> comboBoxEditors;

	private final Map<Class<?>, ListEditor> attrComboBoxEditors;

	private final Map<Class<?>, ValueEditor<?>> valueEditors;

	private final PropertyEditor mappingTypeEditor;

	private final CyApplicationManager appManager;

	private final CyNetworkTableManager tableManager;
	private final SelectedVisualStyleManager selectedManager;
	private final VisualMappingManager vmm;

	/**
	 * Creates a new EditorFactory object.
	 */
	public EditorManagerImpl(final CyApplicationManager appManager, final AttributeSetManager attrManager,
			final VisualMappingManager vmm, final CyNetworkTableManager tableManager,
			final SelectedVisualStyleManager selectedManager) {

		this.appManager = appManager;
		this.tableManager = tableManager;
		this.vmm = vmm;
		this.selectedManager = selectedManager;

		editors = new HashMap<Class<?>, VisualPropertyEditor<?>>();

		comboBoxEditors = new HashMap<String, PropertyEditor>();
		attrComboBoxEditors = new HashMap<Class<?>, ListEditor>();

		final AttributeComboBoxPropertyEditor nodeAttrEditor = new AttributeComboBoxPropertyEditor(CyNode.class,
				attrManager, appManager);
		final AttributeComboBoxPropertyEditor edgeAttrEditor = new AttributeComboBoxPropertyEditor(CyEdge.class,
				attrManager, appManager);
		final AttributeComboBoxPropertyEditor networkAttrEditor = new AttributeComboBoxPropertyEditor(CyNetwork.class,
				attrManager, appManager);
		attrComboBoxEditors.put(nodeAttrEditor.getTargetObjectType(), nodeAttrEditor);
		attrComboBoxEditors.put(edgeAttrEditor.getTargetObjectType(), edgeAttrEditor);
		attrComboBoxEditors.put(networkAttrEditor.getTargetObjectType(), networkAttrEditor);

		valueEditors = new HashMap<Class<?>, ValueEditor<?>>();

		// Create mapping type editor
		this.mappingTypeEditor = getDefaultComboBoxEditor("mappingTypeEditor");

		Set<VisualLexicon> lexSet = vmm.getAllVisualLexicon();

		for (final VisualLexicon lex : lexSet) {
			this.buildDiscreteEditors(lex);
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
	public void addValueEditor(ValueEditor<?> ve, @SuppressWarnings("rawtypes") Map properties) {
		logger.debug("Got Value Editor " + ve.toString() + ", this is for " + ve.getType() + "\n\n\n");
		this.valueEditors.put(ve.getType(), ve);
	}

	public void removeValueEditor(ValueEditor<?> valueEditor, @SuppressWarnings("rawtypes") Map properties) {
		logger.debug("************* Removing Value Editor ****************");
		valueEditors.remove(valueEditor.getType());
	}

	public void addVisualPropertyEditor(VisualPropertyEditor<?> ve, @SuppressWarnings("rawtypes") Map properties) {
		logger.debug("### Got VP Editor " + ve.toString() + ", this is for " + ve.getType());
		this.editors.put(ve.getType(), ve);
	}

	public void removeVisualPropertyEditor(VisualPropertyEditor<?> vpEditor,
			@SuppressWarnings("rawtypes") Map properties) {
		logger.debug("************* Removing VP Editor ****************");
		editors.remove(vpEditor.getType());
	}

	@Override
	public <V> V showVisualPropertyValueEditor(final Component parentComponent, final VisualProperty<V> type,
			V initialValue) throws Exception {

		@SuppressWarnings("unchecked")
		final ValueEditor<V> editor = (ValueEditor<V>) valueEditors.get(type.getRange().getType());

		if (editor == null)
			throw new IllegalStateException("No value editor for " + type.getDisplayName() + " is available.");

		final V newValue = editor.showEditor(parentComponent, initialValue);
		// Null is valid return value. It's from "Cancel" button.
		if (newValue == null)
			return null;

		if (type.getRange().inRange(newValue))
			return newValue;
		else {
			String message = "Value is out-of-range.";
			if (type.getRange() instanceof ContinuousRange)
				message = message + ": " + ((ContinuousRange) type.getRange()).getMin() + " to "
						+ ((ContinuousRange) type.getRange()).getMax();
			JOptionPane.showMessageDialog(parentComponent, message, "Invalid Value", JOptionPane.ERROR_MESSAGE);
			
			return initialValue;
		}
	}


	@SuppressWarnings("unchecked")
	public <V> VisualPropertyEditor<V> getVisualPropertyEditor(final VisualProperty<V> vp) {
		return (VisualPropertyEditor<V>) editors.get(vp.getRange().getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cytoscape.application.swing.vizmap.gui.editors.EditorFactory#
	 * getCellEditors()
	 */
	@Override
	public List<PropertyEditor> getCellEditors() {
		List<PropertyEditor> ret = new ArrayList<PropertyEditor>();

		for (Class<?> type : editors.keySet())
			ret.add(editors.get(type).getPropertyEditor());

		return ret;
	}

	public PropertyEditor getDefaultComboBoxEditor(String editorName) {
		PropertyEditor editor = comboBoxEditors.get(editorName);
		if (editor == null) {
			editor = new CyComboBoxPropertyEditor();
			comboBoxEditors.put(editorName, editor);
		}
		return editor;
	}

	public <V> ValueEditor<V> getValueEditor(Class<V> dataType) {
		return (ValueEditor<V>) this.valueEditors.get(dataType);
	}

	/**
	 * Editor name is NODE, EDGE, or NETWORK.
	 */
	@Override
	public PropertyEditor getDataTableComboBoxEditor(final Class<? extends CyIdentifiable> targetObjectType) {

		final ListEditor editor = attrComboBoxEditors.get(targetObjectType);

		if (editor == null)
			throw new IllegalArgumentException("No such list editor: " + targetObjectType);

		return (PropertyEditor) editor;
	}

	@Override
	public Collection<PropertyEditor> getAttributeSelectors() {
		final Collection<PropertyEditor> selectors = new HashSet<PropertyEditor>();
		for (ListEditor selector : attrComboBoxEditors.values())
			selectors.add((PropertyEditor) selector);
		return selectors;
	}

	@Override
	public PropertyEditor getMappingFunctionSelector() {
		return mappingTypeEditor;
	}

	private void buildDiscreteEditors(final VisualLexicon lexicon) {
		final Set<VisualProperty<?>> vps = lexicon.getAllVisualProperties();

		for (final VisualProperty<?> vp : vps) {
			Range<?> range = vp.getRange();

			// If data type is basic (String, Boolean, etc.) not custom editor is not necessary.
			final Class<?> targetDataType = range.getType();
			if(REGISTRY.getEditor(targetDataType) != null)
				continue;
			
			if (range instanceof DiscreteRange<?>) {
				// Visual Property Editor.
				final Set<?> values = ((DiscreteRange<?>) range).values();
				VisualPropertyEditor<?> vpEditor = new DiscreteValuePropertyEditor(range.getType(), values,
						tableManager, appManager, selectedManager, this, vmm);
				this.addVisualPropertyEditor(vpEditor, null);

				if (this.getValueEditor(range.getType()) == null) {
					ValueEditor<?> valEditor = new DiscreteValueEditor(appManager, range.getType(),
							(DiscreteRange) range, vp);
					this.addValueEditor(valEditor, null);
				}
			}
		}
	}

	public void addRenderingEngineFactory(RenderingEngineFactory<?> factory, Map props) {
		final VisualLexicon lexicon = factory.getVisualLexicon();
		buildDiscreteEditors(lexicon);
	}

	public void removeRenderingEngineFactory(RenderingEngineFactory<?> factory, Map props) {
		// TODO: clean up state when rendering engines are removed.
	}

	@Override
	public PropertyEditor getContinuousEditor(final VisualProperty<?> vp) {
		final ContinuousEditorType editorType = this.getVisualPropertyEditor(vp).getContinuousEditorType();
		
		if(editorType == ContinuousEditorType.COLOR)
			return new GradientEditor(tableManager, appManager, selectedManager, this, vmm);
		else if(editorType == ContinuousEditorType.CONTINUOUS)
			return new C2CEditor(tableManager, appManager, selectedManager, this, vmm);
		else if(editorType == ContinuousEditorType.DISCRETE)
			return new C2DEditor(tableManager, appManager, selectedManager, this, vmm);
		
		return null;
	}

}
