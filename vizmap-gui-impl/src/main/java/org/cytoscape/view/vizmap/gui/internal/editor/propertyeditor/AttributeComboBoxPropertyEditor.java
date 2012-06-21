package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.internal.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracking the list of columns. By default, three instances of this should be
 * created (for NODE, EDGE, and NETWORK).
 * 
 * Export this as an OSGi service!
 */
public final class AttributeComboBoxPropertyEditor extends CyComboBoxPropertyEditor implements ListEditor {

	private static final Logger logger = LoggerFactory.getLogger(AttributeComboBoxPropertyEditor.class);

	private final Class<? extends CyIdentifiable> graphObjectType;
	private final AttributeSetManager attrManager;
	private final CyNetworkManager networkManager;

	private Map<String, Class<?>> currentColumnMap;

	public AttributeComboBoxPropertyEditor(final Class<? extends CyIdentifiable> type,
			final AttributeSetManager attrManager, final CyApplicationManager appManager,
			final CyNetworkManager networkManager) {
		super();
		this.attrManager = attrManager;
		this.graphObjectType = type;
		this.networkManager = networkManager;
		currentColumnMap = new HashMap<String, Class<?>>();

		final JComboBox comboBox = (JComboBox) editor;
		comboBox.setRenderer(new AttributeComboBoxCellRenderer());
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateComboBox(appManager.getCurrentNetwork());
			}
		});
	}

	@Override
	public Class<?> getTargetObjectType() {
		return graphObjectType;
	}

	private void updateComboBox(final CyNetwork currentNetwork) {
		final JComboBox box = (JComboBox) editor;
		final Object selected = box.getSelectedItem();
		box.removeAllItems();
		
		if (currentNetwork == null)
			return;
		
		final AttributeSet compatibleColumns = attrManager.getAttributeSet(currentNetwork, graphObjectType);
		currentColumnMap = compatibleColumns.getAttrMap();
		final AttributeSet targetSet = attrManager.getAttributeSet(currentNetwork, graphObjectType);

		if (targetSet == null)
			return;
		
		final SortedSet<String> sortedName = new TreeSet<String>();
		final Set<CyNetwork> networks = networkManager.getNetworkSet();

		for (CyNetwork net : networks) {
			final AttributeSet currentSet = this.attrManager.getAttributeSet(net, graphObjectType);
			
			for (String attrName : currentSet.getAttrMap().keySet())
				sortedName.add(attrName);
		}

		for (final String attrName : sortedName)
			box.addItem(attrName);

		// Add new name if not in the list.
		box.setSelectedItem(selected);

		logger.debug(graphObjectType + " attribute Combobox Updated: New Names = "
				+ targetSet.getAttrMap().keySet());
	}

	private final class AttributeComboBoxCellRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = 6828337202195089669L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value == null)
				return this;

			this.setText(value.toString());

			if (isSelected) {
				this.setForeground(Color.RED);
				this.setBackground(list.getSelectionBackground());
			} else {
				this.setForeground(Color.BLACK);
				this.setBackground(list.getBackground());
			}

			final Set<String> keys = currentColumnMap.keySet();
			String valueString = value.toString();
			if (keys != null && keys.contains(valueString) == false) {
				this.setEnabled(false);
				this.setFocusable(false);
				this.setForeground(Color.LIGHT_GRAY);
				this.setToolTipText("This column does not exist in current network's table.");
			} else {
				this.setEnabled(true);
				this.setFocusable(true);
				this.setToolTipText("Column Data Type: " + currentColumnMap.get(valueString).getSimpleName());
			}

			return this;
		}
	}
}
