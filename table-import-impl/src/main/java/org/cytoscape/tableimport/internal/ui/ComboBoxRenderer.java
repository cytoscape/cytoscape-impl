package org.cytoscape.tableimport.internal.ui;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	private List<Byte> attributeDataTypes;

	/**
	 * Creates a new ComboBoxRenderer object.
	 *
	 * @param attributeDataTypes  DOCUMENT ME!
	 */
	public ComboBoxRenderer(List<Byte> attributeDataTypes) {
		this.attributeDataTypes = attributeDataTypes;
		setOpaque(true);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param list DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param index DOCUMENT ME!
	 * @param isSelected DOCUMENT ME!
	 * @param cellHasFocus DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index,
	                                              boolean isSelected, boolean cellHasFocus) {
		setText(value.toString());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
		    && (index < attributeDataTypes.size()) && (index >= 0)) {
			final Byte dataType = attributeDataTypes.get(index);

			if (dataType == null) {
				setIcon(null);
			} else {
				setIcon(ImportTablePanel.getDataTypeIcon(dataType));
			}
		} else if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
		           && (index < attributeDataTypes.size())) {
			setIcon(ImportTablePanel.getDataTypeIcon(attributeDataTypes.get(list.getSelectedIndex())));
		}

		return this;
	}
}