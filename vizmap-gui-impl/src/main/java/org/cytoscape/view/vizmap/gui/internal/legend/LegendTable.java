package org.cytoscape.view.vizmap.gui.internal.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.view.model.VisualProperty;

public class LegendTable extends JPanel {
	private VisualProperty<?> vp;
	private JTable legendTable;

	
	public LegendTable(Object[][] data, VisualProperty vp) {
		super();
		legendTable = new JTable(data.length, 2);
		legendTable.setRowHeight(50);
		legendTable.setDefaultRenderer(Object.class, (TableCellRenderer) new LegendCellRenderer());
		this.vp = vp;
		
		setLayout(new BorderLayout());

		Object value = null;

		for (int i = 0; i < data.length; i++) {
			value = getValue(data[i][0]);

			if (value != null) {
				legendTable.getModel().setValueAt(value, i, 0);
			}

			legendTable.getModel().setValueAt(data[i][1], i, 1);
		}

		add(legendTable, SwingConstants.CENTER);
	}

	private Object getValue(final Object value) {
//		final VisualPropertyIcon icon;
//
//		if (value == null) {
//			return null;
//		}
//
//		icon = (VisualPropertyIcon) vp.get.getIcon(value);
//		icon.setLeftPadding(5);
//
//		return icon;
		return null;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param attrName DOCUMENT ME!
	 * @param type DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static JPanel getHeader(String attrName, VisualProperty<?> vp) {
		final JPanel titles = new JPanel();
		final JLabel[] labels = new JLabel[2];
		labels[0] = new JLabel(vp.getDisplayName());
		labels[1] = new JLabel(attrName);

		for (int i = 0; i < labels.length; i++) {
			labels[i].setVerticalAlignment(SwingConstants.CENTER);
			labels[i].setHorizontalAlignment(SwingConstants.LEADING);
			labels[i].setVerticalTextPosition(SwingConstants.CENTER);
			labels[i].setHorizontalTextPosition(SwingConstants.LEADING);
			labels[i].setForeground(Color.DARK_GRAY);
			labels[i].setBorder(new EmptyBorder(10, 0, 7, 10));
			labels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
		}

		titles.setLayout(new GridLayout(1, 2));
		titles.setBackground(Color.white);

		titles.add(labels[0]);
		titles.add(labels[1]);
		titles.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));

		return titles;
	}

	public class LegendCellRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value,
		                                               boolean isSelected, boolean hasFocus,
		                                               int row, int column) {
			final JLabel cell = new JLabel();

			if (value instanceof Icon) {
//				VisualPropertyIcon icon = (VisualPropertyIcon) value;
//				icon.setBottomPadding(0);
//				cell.setIcon(icon);

				cell.setVerticalAlignment(SwingConstants.CENTER);
				cell.setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				cell.setText(value.toString());
				cell.setVerticalTextPosition(SwingConstants.CENTER);
				cell.setVerticalAlignment(SwingConstants.CENTER);
				cell.setHorizontalAlignment(SwingConstants.LEADING);
				cell.setHorizontalTextPosition(SwingConstants.LEADING);
			}

			cell.setPreferredSize(new Dimension(170, 1));

			return cell;
		}
	}
}
