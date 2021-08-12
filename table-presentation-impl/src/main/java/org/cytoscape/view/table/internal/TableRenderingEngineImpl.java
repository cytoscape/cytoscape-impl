package org.cytoscape.view.table.internal;

import static org.cytoscape.view.table.internal.util.ViewUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewChangedListener;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.BrowserTableRowHeader;
import org.cytoscape.view.table.internal.impl.BrowserTableRowHeaderRenderer;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.cytoscape.view.table.internal.impl.icon.VisualPropertyIconFactory;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class TableRenderingEngineImpl implements RenderingEngine<CyTable> {
	
	private static float SMALL_ICON_FONT_SIZE = 14.0f;
	private static int SMALL_ICON_SIZE = 16;
	
	private final CyTableView tableView;
	private final VisualLexicon lexicon;
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar registrar;
	
	private BrowserTable browserTable;
	private BrowserTableRowHeader rowHeader;
	private CornerPanel cornerPanel;
	private VisualPropertyChangeListener vpChangeListener;
	
	private final Icon copyIcon;
	
	private boolean ignoreHeaderSelectionEvents;
	private boolean ignoreTableSelectionEvents;
	
	private final DebounceTimer rowSelectionTimer;
	
	public TableRenderingEngineImpl(
			CyTableView tableView,
			VisualLexicon lexicon,
			PopupMenuHelper popupMenuHelper,
			CyServiceRegistrar registrar
	) {
		this.tableView = tableView;
		this.lexicon = lexicon;
		this.popupMenuHelper = popupMenuHelper;
		this.registrar = registrar;
		
		var iconManager = registrar.getService(IconManager.class);
		var iconFont = iconManager.getIconFont(SMALL_ICON_FONT_SIZE);
		copyIcon = new TextIcon(IconManager.ICON_COPY, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
		
		rowSelectionTimer = new DebounceTimer(25);
	}
	
	public void install(JComponent component) {
		vpChangeListener = new VisualPropertyChangeListener(getBrowserTable(), tableView);
		
		var scrollPane = new JScrollPane();
		scrollPane.setViewportView(getBrowserTable());
		scrollPane.setRowHeaderView(getRowHeader());
		scrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, getCornerPanel());
		scrollPane.getViewport().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (!evt.isPopupTrigger())
					getBrowserTable().clearSelection();
			}
		});
		
		component.setLayout(new BorderLayout());
		component.add(scrollPane);
		
		getBrowserTable().addPropertyChangeListener("rowHeight", evt -> getRowHeader().update());
		getBrowserTable().addPropertyChangeListener("rowHeightChanged", evt -> getRowHeader().update());
		getBrowserTable().getModel().addTableModelListener(evt -> {
			// Update the row header when the table model changes (e.g. added/removed rows)
			getRowHeader().updateModel();
			getCornerPanel().update();
		});
		getBrowserTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent evt) {
				getRowHeader().update();
				getCornerPanel().update();
			}
			@Override
			public void columnRemoved(TableColumnModelEvent evt) {
				getRowHeader().update();
				getCornerPanel().update();
			}
			@Override
			public void columnSelectionChanged(ListSelectionEvent evt) {
				if (!ignoreTableSelectionEvents)
					updateHeader();
			}
			@Override
			public void columnMoved(TableColumnModelEvent evt) {
				// Ignore...
			}
			@Override
			public void columnMarginChanged(ChangeEvent evt) {
				// Ignore...
			}
		});
		getBrowserTable().getSelectionModel().addListSelectionListener(evt -> {
			if (!ignoreTableSelectionEvents) {
				if (!rowSelectionTimer.isShutdown()) {
					rowSelectionTimer.debounce(() -> {
						invokeOnEDT(() -> {
							ignoreHeaderSelectionEvents = true;
							
							try {
								boolean allColumnsSelected = isAllTableColumnsSelected();
								var tbl = getBrowserTable();
								var rh = getRowHeader();
								int rowCount = tbl.getRowCount();
								
								// Only select the row header if all table columns are selected
								for (int i = 0; i < rowCount; i++) {
									if (allColumnsSelected && tbl.isRowSelected(i)) {
										if (!rh.isSelectedIndex(i))
											rh.addSelectionInterval(i, i);
									} else {
										if (rh.isSelectedIndex(i))
											rh.removeSelectionInterval(i, i);
									}
								}
							} finally {
								ignoreHeaderSelectionEvents = false;
							}
						
							// Always update the row header, because it also indicates partial row selection (i.e. not all columns)
							updateHeader();
						});
					});
				}
			}
		});
		
		getRowHeader().getSelectionModel().addListSelectionListener(evt -> {
			if (!evt.getValueIsAdjusting() && !ignoreHeaderSelectionEvents) {
				int rowCount = getBrowserTable().getRowCount();
				int columnCount = getBrowserTable().getColumnCount();
				
				if (rowCount > 0 && columnCount > 0) {
					var indices = getRowHeader().getSelectionModel().getSelectedIndices();
					ignoreTableSelectionEvents = true;
					
					try {
						for (int i = 0; i < rowCount; i++) {
							if (Arrays.binarySearch(indices, i) >= 0) {
								if (!getBrowserTable().isRowSelected(i))
									getBrowserTable().addRowSelectionInterval(i, i);
							} else {
								if (getBrowserTable().isRowSelected(i))
									getBrowserTable().removeRowSelectionInterval(i, i);
							}
						}
						
						getBrowserTable().getColumnModel().getSelectionModel().addSelectionInterval(0, columnCount - 1);
					} finally {
						ignoreTableSelectionEvents = false;
					}
					
					updateHeader();
				}
			}
		});
		
		registerServices();
	}

	@Override
	public void dispose() {
		rowSelectionTimer.shutdown();
		unregisterServices();
	}
	
	// MKTODO is this needed?
	public Collection<View<CyRow>> getSelectedRows() {
		int selectedRow = getBrowserTable().getSelectedRow();
		
		if (selectedRow >= 0) {
			var model = getBrowserTable().getModel();
			
			if (model instanceof BrowserTableModel) {
				var row = ((BrowserTableModel) model).getCyRow(selectedRow);
				var rowView = tableView.getRowView(row);
				
				if (rowView != null)
					return Collections.singletonList(rowView);
			}
		}
		
		return Collections.emptyList();
	}

	// MKTODO is this needed?
	public View<CyColumn> getSelectedColumn() {
		int selectedColumn = getBrowserTable().getSelectedColumn();
		
		if (selectedColumn >= 0) {
			int cellColum = getBrowserTable().convertColumnIndexToModel(selectedColumn);
			var colName = getBrowserTable().getColumnName(cellColum);
			var column = tableView.getModel().getColumn(colName);
			
			return tableView.getColumnView(column);
		}
		
		return null;
	}
	
	@Override
	public CyTableView getViewModel() {
		return tableView;
	}
	
	@Override
	public String getRendererId() {
		return TableViewRendererImpl.ID;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}

	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public Printable createPrintable() {
		return null;
	}

	@Override
	public Image createImage(int width, int height) {
		return null;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return VisualPropertyIconFactory.createIcon(value, width, height);
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		// Ignore...
	}
	
	public BrowserTable getBrowserTable() {
		if (browserTable == null) {
			var compiler = registrar.getService(EquationCompiler.class);
			browserTable = new BrowserTable(popupMenuHelper, registrar);
			var model = new BrowserTableModel(tableView, compiler); // why does it need the element type? 
			
			browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			browserTable.setModel(model);
			
			// So the drop event can go straight through the table to the drop target associated with this panel
			if (browserTable.getDropTarget() != null)
				browserTable.getDropTarget().setActive(false);
		}
		
		return browserTable;
	}
	
	@SuppressWarnings("serial")
	private BrowserTableRowHeader getRowHeader() {
		if (rowHeader == null) {
			rowHeader = new BrowserTableRowHeader(getBrowserTable());
			rowHeader.setOpaque(false);
			rowHeader.setFixedCellWidth(20);
	
			rowHeader.setCellRenderer(new BrowserTableRowHeaderRenderer(getBrowserTable()));
			rowHeader.setBackground(getBrowserTable().getBackground());
			rowHeader.setForeground(getBrowserTable().getForeground());
			rowHeader.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			
			// Redirect the copy action (Control-C) from this JList to the table;
			// otherwise the "Control-C" key input would copy the row header values.
			rowHeader.getActionMap().put("copy", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					copyFromTable(evt.getID());
				}
			});
			
			rowHeader.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					maybeShowPopup(evt);
				}
				@Override
				public void mouseReleased(MouseEvent evt) {
					maybeShowPopup(evt);
				}
				private void maybeShowPopup(MouseEvent evt) {
					if (evt.isPopupTrigger()) {
						// Make sure the row is selected
						int index = rowHeader.locationToIndex(evt.getPoint());
						
						if (index >= 0 && !rowHeader.isSelectedIndex(index))
							rowHeader.setSelectedIndex(index);
						
						// Show popup
						var popup = new JPopupMenu();
						{
							var mi = new JMenuItem("Copy", copyIcon);
							mi.addActionListener(mie -> copyFromTable(mie.getID()));
							popup.add(mi);
						}
						popup.show(rowHeader, evt.getX(), evt.getY());
					}
				}
			});
		}
		
		return rowHeader;
	}
	
	private CornerPanel getCornerPanel() {
		if (cornerPanel == null) {
			cornerPanel = new CornerPanel();
			cornerPanel.setToolTipText("Select All");
			cornerPanel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			cornerPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			cornerPanel.update();
			
			cornerPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (cornerPanel.isEnabled()) {
						// Select all cells
						getBrowserTable().selectAll();
						updateHeader();
						// Maybe show context-menu
						maybeShowPopup(evt);
					}
				}
				@Override
				public void mouseReleased(MouseEvent evt) {
					maybeShowPopup(evt);
				}
				private void maybeShowPopup(MouseEvent evt) {
					if (cornerPanel.isEnabled() && evt.isPopupTrigger()) {
						if (LookAndFeelUtil.isWindows()) {
							// Make sure all cells are selected
							getBrowserTable().selectAll();
							updateHeader();
						}
						// Show popup
						var popup = new JPopupMenu();
						{
							var mi = new JMenuItem("Copy", copyIcon);
							mi.addActionListener(mie -> copyFromTable(mie.getID()));
							popup.add(mi);
						}
						popup.show(cornerPanel, evt.getX(), evt.getY());
					}
				}
			});
		}
		
		return cornerPanel;
	}
	
	private void registerServices() {
		registrar.registerAllServices(getBrowserTable(), new Properties());
		registrar.registerAllServices(getBrowserTable().getModel(), new Properties());
		registrar.registerService(vpChangeListener, TableViewChangedListener.class, new Properties());
	}
	
	private void unregisterServices() {
		registrar.unregisterAllServices(getBrowserTable());
		registrar.unregisterAllServices(getBrowserTable().getModel());
		registrar.unregisterService(vpChangeListener, TableViewChangedListener.class);
	}
	
	private void copyFromTable(int eventId) {
		var action = getBrowserTable().getActionMap().get("copy");
		
		if (action != null)
			action.actionPerformed(new ActionEvent(getBrowserTable(), eventId, "copy"));
	}
	
	private void updateHeader() {
		getRowHeader().update();
		getBrowserTable().getTableHeader().repaint();
		getCornerPanel().update();
	}
	
	private boolean isAllTableColumnsSelected() {
		return getBrowserTable().getColumnModel().getSelectedColumnCount() == getBrowserTable().getColumnModel().getColumnCount();
	}
	
	private boolean isAllTableCellsSelected() {
		return getBrowserTable().getSelectedRowCount() == getBrowserTable().getRowCount() && isAllTableColumnsSelected();
	}
	
	@SuppressWarnings("serial")
	private class CornerPanel extends JPanel {
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if (!isEnabled())
				return;
			
			// Draw triangle that indicates when all cells selected
			var g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			Color fg = null;
			
			if (isAllTableCellsSelected()) {
				fg = UIManager.getColor("Table.focusCellBackground");
			} else {
				fg = UIManager.getColor("Label.foreground");
				fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60);
			}
			
			g2.setColor(fg);
			
			int w = getWidth();
			int h = getHeight();
			int pad = 3;
			g2.fillPolygon(new int[]{ w - pad, w - pad, pad }, new int[]{ pad, h - pad, h - pad }, 3);
			
			g2.dispose();
		}
		
		void update() {
			setEnabled(getBrowserTable().getRowCount() > 0 && getBrowserTable().getColumnCount() > 0);
			
			var allSelected = isAllTableCellsSelected();
			setBackground(UIManager.getColor(allSelected ? "Table.background" : "TableHeader.background"));
			repaint();
		}
	}
}
