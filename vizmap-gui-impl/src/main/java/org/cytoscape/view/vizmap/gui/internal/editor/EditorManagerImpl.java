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
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.DiscreteValueEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EditorManagerImpl implements EditorManager {

	private static final Logger logger = LoggerFactory.getLogger(EditorManagerImpl.class);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cytoscape.application.swing.vizmap.gui.editors.EditorFactory#
	 * addEditorDisplayer(org .cytoscape.vizmap.gui.editors.EditorDisplayer,
	 * java.util.Map)
	 */
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
		logger.debug("Total editor count = " + editors.size());
	}


	public void removeVisualPropertyEditor(VisualPropertyEditor<?> vpEditor,
			@SuppressWarnings("rawtypes") Map properties) {
		logger.debug("************* Removing VP Editor ****************");
		editors.remove(vpEditor.getType());
	}


	@Override
	public <V> V showVisualPropertyValueEditor(final Component parentComponent, final VisualProperty<V> type, V initialValue)
			throws Exception {

		@SuppressWarnings("unchecked")
		final ValueEditor<V> editor = (ValueEditor<V>) valueEditors.get(type.getRange().getType());

		if (editor == null)
			throw new IllegalStateException("No value editor for " + type.getDisplayName() + " is available.");

		while (true) {
			final V newValue = editor.showEditor(parentComponent, initialValue);
			if (type.getRange().inRange(newValue))
				return newValue;
			else {
				String message = "Please evter valid value";
				if(type.getRange() instanceof ContinuousRange)
					message = message + ": " + ((ContinuousRange)type.getRange()).getMin() + " to " + ((ContinuousRange)type.getRange()).getMax();
				JOptionPane.showMessageDialog(parentComponent, message, "Invalid Value",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cytoscape.application.swing.vizmap.gui.editors.EditorFactory#
	 * showContinuousEditor(java .awt.Component,
	 * org.cytoscape.application.swing.viewmodel.VisualProperty)
	 */
	public <V> void showContinuousEditor(Component parentComponent, VisualProperty<V> type) throws Exception {
		final VisualPropertyEditor<?> editor = editors.get(type.getRange().getType());

		// TODO: design dialog state mamagement
		//
		//
		// Component mappingEditor = editor.getContinuousMappingEditor();
		//
		// JDialog editorDialog = new JDialog();
		// editorDialog.setModal(true);
		// editorDialog.setLocationRelativeTo(parentComponent);

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
	public PropertyEditor getDataTableComboBoxEditor(final Class<? extends CyTableEntry> targetObjectType) {

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

		logger.debug("\n\n\nNew Engine Factory: Adding discrete value editors------------------------");

		Set<VisualProperty<?>> vps = lexicon.getAllVisualProperties();
		for (final VisualProperty<?> vp : vps) {
			Range<?> range = vp.getRange();

			if (range instanceof DiscreteRange<?>) {

				// Visual Property Editor.
				logger.debug("Got new Discrete.  Creating new VP editor: " + vp.getDisplayName());
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
}
