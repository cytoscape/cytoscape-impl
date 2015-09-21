package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id$
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class ColumnSelector extends JPanel {
	
	public static final String SHARED_COL_ICON_TEXT = IconManager.ICON_SITEMAP;
	
	private static final Border CELL_BORDER = new EmptyBorder(0, 0, 0, 0);
	
	private static final int SELECTED_COL_IDX = 0;
	private static final int NAME_COL_IDX = 1;
	private static final int TYPE_COL_IDX = 2;
	private static final int SHARED_COL_IDX = 3;
	
	private static final String[] headerNames = new String[]{ "", "Column Name", "Type", "" };
	
	private JTable table;
	private JScrollPane tableScrollPane;
	
	private final SortedMap<String, CyColumn> columns;
	private final Set<String> selectedColumnNames;
	private List<Integer> previousSelectedRows;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ColumnSelector(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		this.columns = new TreeMap<String, CyColumn>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return collator.compare(s1, s2);
			}
		});
		this.selectedColumnNames = new HashSet<>();
		
		init();
	}

	public void update(final Collection<CyColumn> columns, final Collection<String> selectedColumnNames) {
		this.columns.clear();
		this.selectedColumnNames.clear();
		
		if (columns != null) {
			for (CyColumn c : columns)
				this.columns.put(c.getName(), c);
		}
		
		if (selectedColumnNames != null)
			this.selectedColumnNames.addAll(selectedColumnNames);
		
		updateComponents();
	}

	public Set<String> getSelectedColumnNames() {
		return new HashSet<>(selectedColumnNames);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		add(getTableScrollPane(), BorderLayout.CENTER);
	}
	
	private void updateComponents() {
		final Object[][] data = new Object[columns.size()][headerNames.length];
		int i = 0;
		
		for (CyColumn c : columns.values()) {
			data[i][SELECTED_COL_IDX] = selectedColumnNames.contains(c.getName());
			data[i][NAME_COL_IDX] = c.getName();
			data[i][TYPE_COL_IDX] = c;
			data[i][SHARED_COL_IDX] = c.getVirtualColumnInfo().isVirtual();
			i++;
		}
		
		final DefaultTableModel model = new DefaultTableModel(data, headerNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		getTable().setModel(model);
		
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setMaxWidth(22);
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setMaxWidth(44);
		getTable().getColumnModel().getColumn(SHARED_COL_IDX).setMaxWidth(22);
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setResizable(false);
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setResizable(false);
		getTable().getColumnModel().getColumn(SHARED_COL_IDX).setResizable(false);
	}
	
	private JTable getTable() {
		if (table == null) {
			final DefaultSelectorTableCellRenderer defRenderer = new DefaultSelectorTableCellRenderer();
			final CheckBoxTableCellRenderer checkBoxRenderer = new CheckBoxTableCellRenderer();
			
			table = new JTable(new DefaultTableModel(headerNames, 0)) {
				@Override
				public TableCellRenderer getCellRenderer(int row, int column) {
					if (column == SELECTED_COL_IDX) return checkBoxRenderer;
					return defRenderer;
				}
			};
			table.setTableHeader(null);
			table.setShowGrid(false);
			
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						// Workaround for preventing a click on the check-box in a selected row
						// from changing the selection when multiple table rows are already selected
						if (table.getSelectedRowCount() > 0)
							previousSelectedRows = Arrays.stream(table.getSelectedRows()).boxed()
									.collect(Collectors.toList());
					}
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					final boolean isMac = LookAndFeelUtil.isMac();
					
					// COMMAND button down on MacOS (or CONTROL button down on another OS) or SHIFT?
					if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown()) || e.isShiftDown())
						return; // Ignore!
					
				    final int col = table.columnAtPoint(e.getPoint());
				    
					if (col == SELECTED_COL_IDX) {
						final int row = table.rowAtPoint(e.getPoint());
						
						// Restore previous multiple-row selection first
					    if (previousSelectedRows != null && previousSelectedRows.contains(row)) {
					    	for (int i : previousSelectedRows)
					    		table.addRowSelectionInterval(i, i);
					    }
						
						toggleSelection(row);
					}
				}
			});
		}
		
		return table;
	}
	
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new JScrollPane();
			tableScrollPane.setPreferredSize(new Dimension(300, 180));
			tableScrollPane.setViewportView(getTable());
			tableScrollPane.setBorder(new EmptyBorder(0, 2, 0, 2));
			
			final Color bg = UIManager.getColor("Table.background");
			tableScrollPane.setBackground(bg);
			tableScrollPane.getViewport().setBackground(bg);
		}

		return tableScrollPane;
	}
	
	private void toggleSelection(final int row) {
		final boolean selected = (boolean) getTable().getValueAt(row, SELECTED_COL_IDX);
		final int[] selectedRows = getTable().getSelectedRows();
		
		if (selectedRows != null) {
			for (int i : selectedRows) {
				final String name = (String) getTable().getValueAt(i, NAME_COL_IDX);
				getTable().setValueAt(!selected, i, SELECTED_COL_IDX);
				
				if (selected)
					selectedColumnNames.remove(name);
				else
					selectedColumnNames.add(name);
			}
			
			getTable().repaint();
		}
	}
	
	private class DefaultSelectorTableCellRenderer extends DefaultTableCellRenderer {
		
		final Font typeFont;
		final Font defFont;
		final IconManager iconManager;
		
		DefaultSelectorTableCellRenderer() {
			typeFont = new Font("Serif", Font.BOLD, 11); // This font is used as an icon--Don't change it!
			defFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
			iconManager = serviceRegistrar.getService(IconManager.class);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			final boolean visible = (boolean) table.getValueAt(row, SELECTED_COL_IDX);
			
			if (!visible && column != SELECTED_COL_IDX)
				setForeground(UIManager.getColor("TextField.inactiveForeground"));
			else
				setForeground(table.getForeground());
			
			final Color bg = UIManager.getColor("Table.background");
			setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			setBorder(CELL_BORDER);
			
			if (value instanceof Boolean) {
				setFont(iconManager.getIconFont(12));
				setHorizontalAlignment(JLabel.CENTER);
				
				if (column == SHARED_COL_IDX) {
					setText((boolean)value ? SHARED_COL_ICON_TEXT : "");
					setToolTipText((boolean)value ? "Network Collection Column" : null);
				} else {
					setText((boolean)value ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O);
				}
			} else {
				 if (column == TYPE_COL_IDX && value instanceof CyColumn) {
					final CyColumn c = (CyColumn) value;
					final AttributeDataType adt = AttributeDataType.getAttributeDataType(c.getType(), c.getListElementType());
					
					if (adt != null) {
						setFont(typeFont);
						setHorizontalAlignment(JLabel.CENTER);
						setText(adt.getText());
						setToolTipText(adt.getDescription());
					}
				} else {
					setFont(defFont);
					setHorizontalAlignment(JLabel.LEFT);
					setToolTipText("" + value);
				}
			}
			
			return this;
		}
	}
	
	private class CheckBoxTableCellRenderer implements TableCellRenderer {
		
		final JPanel panel;
		final JCheckBox chk;
		
		CheckBoxTableCellRenderer() {
			chk = new JCheckBox();
			chk.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua LAF only
			panel = new JPanel(new BorderLayout());
			panel.add(chk, BorderLayout.WEST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			final Color bg = UIManager.getColor("Table.background");
			
			chk.setSelected((boolean)value);
			chk.setToolTipText((boolean)value ? "Show" : "Hide");
			chk.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBorder(CELL_BORDER);
			
			return panel;
		}
	}
	
	enum AttributeDataType {
		TYPE_STRING(String.class, null, "ab", "String"),
		TYPE_INTEGER(Integer.class, null, "1", "Integer"),
		TYPE_LONG(Long.class, null, "123", "Long Integer"),
		TYPE_FLOATING(Double.class, null, "1.0", "Floating Point"),
		TYPE_BOOLEAN(Boolean.class, null, "y/n", "Boolean"),
		TYPE_STRING_LIST(List.class, String.class, "[ ab ]", "List of Strings"),
		TYPE_INTEGER_LIST(List.class, Integer.class, "[ 1 ]", "List of Integers"),
		TYPE_LONG_LIST(List.class, Long.class, "[ 123 ]", "List of Long Integers"),
		TYPE_FLOATING_LIST(List.class, Double.class, "[ 1.0 ]", "List of Floating Point Numbers"),
		TYPE_BOOLEAN_LIST(List.class, Boolean.class, "[ y/n ]", "List of Booleans");
		
		private final Class<?> type;
		private final Class<?> listType;
		private final String text;
		private final String description;
		
	    private AttributeDataType(final Class<?> type, final Class<?> listType, final String text, final String description) {
			this.type = type;
			this.listType = listType;
			this.text = text;
			this.description = description;
		}
	    
	    public Class<?> getType() {
			return type;
		}
	    
	    public Class<?> getListType() {
			return listType;
		}
	    
	    public boolean isList() {
	    	return listType != null;
	    }
	    
	    public String getText() {
			return text;
		}
	    
	    public String getDescription() {
			return description;
		}
	    
	    public static AttributeDataType getAttributeDataType(final Class<?> type, final Class<?> listType) {
	    	for (AttributeDataType adt : AttributeDataType.values()) {
	    		if (adt.getType() == type) {
	    			if (listType == null || listType == adt.getListType())
	    				return adt;
	    		}
	    	}
	    	
	    	return TYPE_STRING;
	    }
	}
}
