package org.cytoscape.view.table.internal;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.TableRenderingEngine;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModel;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;

public class BrowserTableRenderingEngine implements TableRenderingEngine {
	
	private final CyTableView tableView;
	private final VisualLexicon lexicon;
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar registrar;
	
	private BrowserTable browserTable;
	
	
	public BrowserTableRenderingEngine(CyTableView tableView, VisualLexicon lexicon, PopupMenuHelper popupMenuHelper, CyServiceRegistrar registrar) {
		this.tableView = tableView;
		this.lexicon = lexicon;
		this.popupMenuHelper = popupMenuHelper;
		this.registrar = registrar;
	}
	
	private BrowserTable createBrowserTable() {
		var compiler = registrar.getService(EquationCompiler.class);
		var browserTable = new BrowserTable(compiler, popupMenuHelper, registrar);
		var model = new BrowserTableModel(tableView, null, compiler); // why does it need the element type? 
		browserTable.setModel(model);
		
		//move and hide SUID and selected by default
		final List<String> attrList = model.getAllAttributeNames();

		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) browserTable.getColumnModel();
		
		if (attrList.contains(CyNetwork.SUID))
			columnModel.moveColumn(browserTable.convertColumnIndexToView(
					model.mapColumnNameToColumnIndex(CyNetwork.SUID)), 0);
		
		if (attrList.contains(CyNetwork.SELECTED))
			columnModel.moveColumn(browserTable.convertColumnIndexToView(
					model.mapColumnNameToColumnIndex(CyNetwork.SELECTED)), 1);
		
		attrList.remove(CyNetwork.SUID);
		attrList.remove(CyNetwork.SELECTED);
		browserTable.setVisibleAttributeNames(attrList);
		
		// So the drop event can go straight through the table to the drop target associated with this panel
		if (browserTable.getDropTarget() != null)
			browserTable.getDropTarget().setActive(false);
		
		ColumnResizer.adjustColumnPreferredWidths(browserTable, false);
	}
	
	@Override
	public Collection<View<CyRow>> getSelectedRows() {
		int selectedRow = browserTable.getSelectedRow();
		if(selectedRow >= 0) {
			CyRow row = browserTable.getModel().getCyRow(selectedRow);
			View<CyRow> rowView = tableView.getRowView(row);
			return Collections.singletonList(rowView);
		} else {
			return Collections.emptyList();
		}
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
	
	public void install(JComponent component) {
		// MKTODO there's more to it than this, there's a bunch of swing listeners to register and stuff
		this.browserTable = createBrowserTable();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(browserTable);
		
		component.setLayout(new BorderLayout());
		component.add(scrollPane);
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
	public void dispose() {
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
