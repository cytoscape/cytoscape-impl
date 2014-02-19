package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComboBox;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;


public class MappingTypeComboBoxPropertyEditor extends CyComboBoxPropertyEditor {

	private final MappingFunctionFactoryProxy mappingFactoryProxy;

	public MappingTypeComboBoxPropertyEditor(final MappingFunctionFactoryProxy mappingFactoryProxy) {
		this.mappingFactoryProxy = mappingFactoryProxy;
		
		final JComboBox comboBox = (JComboBox) editor;
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateComboBox();
			}
		});
	}

	private void updateComboBox() {
		final JComboBox box = (JComboBox) editor;
		final Object selected = box.getSelectedItem();
		box.removeAllItems();
		
		final Set<VisualMappingFunctionFactory> factories = mappingFactoryProxy.getMappingFactories();

		for (final VisualMappingFunctionFactory f : factories)
			box.addItem(f);

		box.setSelectedItem(selected);
	}
}
