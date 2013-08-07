package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.gui.editor.ListEditor;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
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
	private final AttributeSetProxy attrProxy;
	private final CyNetworkManager networkManager;

	private Map<String, Class<?>> currentColumnMap;

	public AttributeComboBoxPropertyEditor(final Class<? extends CyIdentifiable> type,
										   final AttributeSetProxy attrProxy,
										   final CyApplicationManager appManager,
										   final CyNetworkManager networkManager) {
		this.attrProxy = attrProxy;
		this.graphObjectType = type;
		this.networkManager = networkManager;
		currentColumnMap = new HashMap<String, Class<?>>();

		final JComboBox comboBox = (JComboBox) editor;
		comboBox.setRenderer(new AttributeComboBoxCellRenderer());
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		
		final AttributeSet compatibleColumns = attrProxy.getAttributeSet(currentNetwork, graphObjectType);
		currentColumnMap = compatibleColumns.getAttrMap();
		final AttributeSet targetSet = attrProxy.getAttributeSet(currentNetwork, graphObjectType);

		if (targetSet == null)
			return;
		
		final Collator collator = Collator.getInstance(Locale.getDefault()); // For locale-specific sorting
		final SortedSet<String> sortedName = new TreeSet<String>(collator);
		
		final Set<CyNetwork> networks = networkManager.getNetworkSet();

		for (final CyNetwork net : networks) {
			final AttributeSet currentSet = attrProxy.getAttributeSet(net, graphObjectType);
			
			for (Entry<String, Class<?>> entry: currentSet.getAttrMap().entrySet()) {
				if (columnIsAllowed(entry.getKey(), entry.getValue()))
					sortedName.add(entry.getKey());
			}
		}

		for (final String attrName : sortedName)
			box.addItem(attrName);

		// Add new name if not in the list.
		box.setSelectedItem(selected);
	}

	private boolean columnIsAllowed(String name, Class<?> type) {
		if (!Long.class.equals(type) && !List.class.equals(type))
			return true;
		
		return !CyIdentifiable.SUID.equals(name) && !name.endsWith(".SUID");
	}

	private final class AttributeComboBoxCellRenderer extends Renderer {

		private static final long serialVersionUID = 6828337202195089669L;

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus) {
			final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value == null)
				return component;

			final JLabel lbl = component instanceof JLabel ? (JLabel) component : new JLabel();
			lbl.setText(value.toString());

			final Set<String> keys = currentColumnMap.keySet();
			final String valueString = value.toString();
			
			if (keys != null && !keys.contains(valueString)) {
				this.setEnabled(false);
				this.setFocusable(false);
				this.setForeground(Color.LIGHT_GRAY);
				this.setToolTipText("This column does not exist in current network's table.");
			} else {
				this.setEnabled(true);
				this.setFocusable(true);
				this.setToolTipText("Column Data Type: " + currentColumnMap.get(valueString).getSimpleName());
			}

			return lbl;
		}
	}
}
