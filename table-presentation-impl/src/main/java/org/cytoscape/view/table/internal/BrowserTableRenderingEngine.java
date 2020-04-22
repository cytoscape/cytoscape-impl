package org.cytoscape.view.table.internal;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.TableViewChangedListener;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.TableRenderingEngine;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.TableMode;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModel;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.BrowserTableModel.ViewMode;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;

public class BrowserTableRenderingEngine implements TableRenderingEngine, TableViewChangedListener {
	
	private final CyTableView tableView;
	private final VisualLexicon lexicon;
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar registrar;
	
	private BrowserTable browserTable;
	
	
	public BrowserTableRenderingEngine(CyTableView tableView, VisualLexicon lexicon, PopupMenuHelper popupMenuHelper, CyServiceRegistrar registrar) {
		System.out.println("BrowserTableRenderingEngine() created " + tableView.getModel().getTitle());
		
		this.tableView = tableView;
		this.lexicon = lexicon;
		this.popupMenuHelper = popupMenuHelper;
		this.registrar = registrar;
	}
	
	private BrowserTable createBrowserTable() {
		var compiler = registrar.getService(EquationCompiler.class);
		var browserTable = new BrowserTable(compiler, popupMenuHelper, registrar);
		var model = new BrowserTableModel(tableView.getModel(), tableView.getTableType(), compiler); // why does it need the element type? 
		
		browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		browserTable.setModel(model);
		
		//move and hide SUID and selected by default
		final List<String> attrList = model.getAllAttributeNames();

		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) browserTable.getColumnModel();
		
		if (attrList.contains(CyNetwork.SUID))
			columnModel.moveColumn(browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(CyNetwork.SUID)), 0);
		
		if (attrList.contains(CyNetwork.SELECTED))
			columnModel.moveColumn(browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(CyNetwork.SELECTED)), 1);
		
		attrList.remove(CyNetwork.SUID);
		attrList.remove(CyNetwork.SELECTED);
		browserTable.setVisibleAttributeNames(attrList);
		
		// So the drop event can go straight through the table to the drop target associated with this panel
		if (browserTable.getDropTarget() != null)
			browserTable.getDropTarget().setActive(false);
		
		ColumnResizer.adjustColumnPreferredWidths(browserTable, false);
		
		return browserTable;
	}
	
	
	public void install(JComponent component) {
		// MKTODO there's more to it than this, there's a bunch of swing listeners to register and stuff
		this.browserTable = createBrowserTable();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(browserTable);
		
		component.setLayout(new BorderLayout());
		component.add(scrollPane);
		
		registerServices();
	}
	
	
	@Override
	public void dispose() {
		unregisterServices();
	}
	
	
	private void registerServices() {
		registrar.registerAllServices(browserTable, new Properties());
		registrar.registerAllServices(browserTable.getModel(), new Properties());
		registrar.registerService(this, TableViewChangedListener.class, new Properties());
	}
	
	private void unregisterServices() {
		registrar.unregisterAllServices(browserTable);
		registrar.unregisterAllServices(browserTable.getModel());
		registrar.unregisterService(this, TableViewChangedListener.class);
	}
	
	
	@Override
	public void handleEvent(TableViewChangedEvent<?> e) {
		if(e.getSource() != tableView)
			return;
		
		for(var record : e.getPayloadCollection()) {
			VisualProperty<?> vp = record.getVisualProperty();
			
			if(record.getView().getModel() instanceof CyColumn) {
				CyColumnView colView = (CyColumnView) record.getView();
				if(vp == BasicTableVisualLexicon.COLUMN_VISIBLE) {
					boolean visible = Boolean.TRUE.equals(record.getValue());
					browserTable.setColumnVisibility(colView.getModel().getName(), visible);
				}
				
			} else if(record.getView().getModel() instanceof CyTable) {
				if(vp == BasicTableVisualLexicon.TABLE_VIEW_MODE) {
					changeSelectionMode((TableMode)record.getValue());
				}
			}
		}
	}
	
	
	// MKTODO this needs to go in the renderer
	private void changeSelectionMode(TableMode tableMode) {
		BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
		
		ViewMode viewMode = ViewMode.fromVisualPropertyValue(tableMode);
		model.setViewMode(viewMode);
		model.updateViewMode();
		
		if (viewMode == ViewMode.ALL && browserTable.getColumn(CyNetwork.SELECTED) != null) {
			// Show the current selected rows
			final Set<Long> suidSelected = new HashSet<>();
			final Set<Long> suidUnselected = new HashSet<>();
			final Collection<CyRow> selectedRows = tableView.getModel().getMatchingRows(CyNetwork.SELECTED, Boolean.TRUE);
	
			for (final CyRow row : selectedRows) {
				suidSelected.add(row.get(CyIdentifiable.SUID, Long.class));
			}
	
			if (!suidSelected.isEmpty())
				browserTable.changeRowSelection(suidSelected, suidUnselected);
		}
	}
	
	
	
	@Override
	public Collection<View<CyRow>> getSelectedRows() {
		int selectedRow = browserTable.getSelectedRow();
		if(selectedRow >= 0) {
			TableModel model = browserTable.getModel();
			if(model instanceof BrowserTableModel) {
				CyRow row = ((BrowserTableModel)model).getCyRow(selectedRow);
				View<CyRow> rowView = tableView.getRowView(row);
				if(rowView != null) {
					return Collections.singletonList(rowView);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public View<CyColumn> getSelectedColumn() {
		int selectedColumn = browserTable.getSelectedColumn();
		if(selectedColumn >= 0) {
			int cellColum = browserTable.convertColumnIndexToModel(selectedColumn);
			String colName = browserTable.getColumnName(cellColum);
			CyColumn column = tableView.getModel().getColumn(colName);
			return tableView.getColumnView(column);
		}
		return null;
	}
	
	@Override
	public View<CyTable> getViewModel() {
		return tableView;
	}
	
	@Override
	public String getRendererId() {
		return BrowserTableRenderer.ID;
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
		return null;
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
	}

	


}
