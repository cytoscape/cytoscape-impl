package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComboBox;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.internal.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds list of attributes. By default, three instances of this should be
 * created (for NODE, EDGE, and NETWORK).
 * 
 * Export this as an OSGi service!
 */
public class AttributeComboBoxPropertyEditor extends CyComboBoxPropertyEditor
		implements ListEditor, SetCurrentNetworkViewListener {
	
	private static final Logger logger = LoggerFactory
			.getLogger(AttributeComboBoxPropertyEditor.class);

	private final Class<? extends CyTableEntry> type;
	
	private final AttributeSetManager attrManager;

	public AttributeComboBoxPropertyEditor(final Class<? extends CyTableEntry> type,
			final AttributeSetManager attrManager, final CyApplicationManager appManager) {
		super();
		this.attrManager = attrManager;
		this.type = type;
		
		final JComboBox comboBox = (JComboBox) editor;
		comboBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateComboBox(appManager.getCurrentNetwork());
			}});
	}
	

	@Override
	public Class<?> getTargetObjectType() {
		return type;
	}

	private void updateComboBox(final CyNetwork currentNetwork) {
		final JComboBox box = (JComboBox) editor;
		final Object selected = box.getSelectedItem();
		box.removeAllItems();
	
		if ( currentNetwork != null ) {
			final AttributeSet targetSet = this.attrManager.getAttributeSet(currentNetwork, type);

			if(targetSet == null)
				throw new NullPointerException("AttributeSet is null.");
		
			for (String attrName : targetSet.getAttrMap().keySet())
				box.addItem(attrName);

			// Add new name if not in the list.
			box.setSelectedItem(selected);

			logger.debug(type + " attribute Combobox Updated: New Names = "
					+ targetSet.getAttrMap().keySet());
		}
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		final CyNetworkView networkView = e.getNetworkView();
		if ( networkView == null ) {
			logger.debug("Current network view switched to null");
			updateComboBox(null);
		} else {
			logger.debug("Current network view switched to " + networkView.getModel());
			updateComboBox(networkView.getModel());
		}
	}
}
