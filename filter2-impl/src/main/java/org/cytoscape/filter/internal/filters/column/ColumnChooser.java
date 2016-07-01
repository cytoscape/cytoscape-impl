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

/**
 * A combo box that displays the current set of node and edge columns.
 * 
 * This was designed to work similarly to the "Column" selector for
 * visual property mappings.
 */
@SuppressWarnings("serial")
public class ColumnChooser extends JPanel {

	private final CyNetworkManager networkManager;
	private final CyApplicationManager appManager;
	
	private JComboBox<ColumnElement> comboBox;
	private Set<ColumnElement> enabledColumns = new HashSet<>();
	
	
	public ColumnChooser(CyNetworkManager networkManager, CyApplicationManager appManager, FilterPanelStyle style) {
		this.networkManager = networkManager;
		this.appManager = appManager;
		
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
		
		CyNetwork network = appManager.getCurrentNetwork();
		if(network == null)
			return;
		
		enabledColumns = getColumns(network);
		
		SortedSet<ColumnElement> allColumns = new TreeSet<>();
		
		final Set<CyNetwork> networks = networkManager.getNetworkSet();
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
