package org.cytoscape.filter.internal.filters.column;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * A combo box that displays the current set of node and edge columns.
 * 
 * This was designed to work similarly to the "Column" selector for
 * visual property mappings.
 */
@SuppressWarnings("serial")
public class ColumnChooser extends JPanel {

	private JComboBox<ColumnElement> comboBox;
	private Set<ColumnElement> enabledColumns = new HashSet<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ColumnChooser(final FilterPanelStyle style, final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		setOpaque(false);
		
		comboBox = style.createCombo();
		comboBox.setRenderer(new AttributeComboBoxCellRenderer());
		
		updateComboBox();
		
		setLayout(new BorderLayout());
		add(comboBox, BorderLayout.CENTER);
	}
	
	
	/**
	 * The "Choose Column..." option doesn't count.
	 */
	public ColumnElement getSelectedItem() {
		int index = comboBox.getSelectedIndex();
		if(index < 0)
			index = 0;
		return comboBox.getItemAt(index);
	}
	
	
	/**
	 * Don't use ActionEvent.getSource(), because the returned component might change.
	 */
	public void addActionListener(ActionListener al) {
		comboBox.addActionListener(al);
	}
	
	public void removeActionListener(ActionListener al) {
		comboBox.removeActionListener(al);
	}
	
	void updateComboBox() {
		Object selected = comboBox.getSelectedItem();
		comboBox.removeAllItems();
		comboBox.addItem(new ColumnElement("Choose column..."));
		
		CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		if (network == null)
			return;
		
		enabledColumns = getColumns(network);
		
		SortedSet<ColumnElement> allColumns = new TreeSet<>();
		final Set<CyNetwork> networks = serviceRegistrar.getService(CyNetworkManager.class).getNetworkSet();
		
		for (final CyNetwork net : networks) {
			Set<ColumnElement> networkColumns = getColumns(net);
			allColumns.addAll(networkColumns);
		}

		for(ColumnElement columnElement : allColumns) {
			comboBox.addItem(columnElement);
		}

		comboBox.setSelectedItem(selected);
	}
	
	
	private Set<ColumnElement> getColumns(CyNetwork network) {
		Set<ColumnElement> columns = new HashSet<>();
		for(CyColumn col : network.getDefaultNodeTable().getColumns()) {
			columns.add(new ColumnElement(CyNode.class, col));
		}
		for(CyColumn col : network.getDefaultEdgeTable().getColumns()) {
			columns.add(new ColumnElement(CyEdge.class, col));
		}
		return columns;
	}
	
	
	public int find(Matcher<ColumnElement> matcher) {
		ComboBoxModel<ColumnElement> model = comboBox.getModel();
		for(int i = 0; i < model.getSize(); i++) {
			ColumnElement element = model.getElementAt(i);
			if (matcher.matches(element)) {
				return i;
			}
		}
		return -1;
	}
	
	public void select(int defaultIndex, Matcher<ColumnElement> matcher) {
		int index = find(matcher);
		if(index == -1) {
			index = defaultIndex;
		}
		comboBox.setSelectedIndex(index);
	}
	
	
	private final class AttributeComboBoxCellRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value == null)
				return component;

			final JLabel lbl = component instanceof JLabel ? (JLabel) component : new JLabel();
			lbl.setText(ViewUtil.abbreviate(value.toString(), 30));

			ColumnElement valueElememnt = (ColumnElement) value;

			if(!enabledColumns.contains(valueElememnt)) {//not enabled
				if(isSelected) {
					component.setBackground(UIManager.getColor("ComboBox.background"));
				} else {
					component.setBackground(super.getBackground());
				}
				component.setForeground(UIManager.getColor("Label.disabledForeground"));
			} else {
				component.setBackground(super.getBackground());
				component.setForeground(super.getForeground());
			}

			return component;
		}
	}
	
	
}
