package org.cytoscape.browser.internal.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.CellFormat;
import org.cytoscape.view.presentation.property.table.TableMode;

/**
 * This class just holds on to the RenderingEngine and its associated JComponenet
 * and makes it easier to pass them around.
 */
public class TableRenderer implements CyDisposable {

	private RenderingEngine<CyTable> renderingEngine;
	private JComponent component;
	
	public TableRenderer(RenderingEngine<CyTable> renderingEngine, JComponent component) {
		this.renderingEngine = renderingEngine;
		this.component = component;
	}

	public RenderingEngine<CyTable> getRenderingEngine() {
		return renderingEngine;
	}

	public JComponent getComponent() {
		return component;
	}
	
	public CyTableView getTableView() {
		return (CyTableView) renderingEngine.getViewModel();
	}

	public CyTable getDataTable() {
		return renderingEngine.getViewModel().getModel();
	}

	public void setColumnVisible(String name, boolean visible) {
		View<CyColumn> colView = getTableView().getColumnView(name);
		setColumnVisible(colView, visible);
	}
	
	public static void setColumnVisible(View<CyColumn> colView, boolean visible) {
		if(colView != null) {
			colView.setLockedValue(BasicTableVisualLexicon.COLUMN_VISIBLE, visible);
		}
	}
	
	public boolean getColumnVisible(View<CyColumn> colView) {
		if(colView == null)
			return BasicTableVisualLexicon.COLUMN_VISIBLE.getDefault();
		return colView.getVisualProperty(BasicTableVisualLexicon.COLUMN_VISIBLE);
	}
	
	public void setColumnGravity(String name, int grav) {
		View<CyColumn> colView = getTableView().getColumnView(name);
		if(colView != null) {
			colView.setLockedValue(BasicTableVisualLexicon.COLUMN_GRAVITY, grav);
		}
	}
	
	public List<View<CyColumn>> getColumnViewsSortedByGravity() {
		var sortedColViews = new ArrayList<>(getTableView().getColumnViews());
		sortedColViews.sort((cv1,cv2) -> {
			double grav1 = cv1.getVisualProperty(BasicTableVisualLexicon.COLUMN_GRAVITY);
			double grav2 = cv2.getVisualProperty(BasicTableVisualLexicon.COLUMN_GRAVITY);
			return Double.compare(grav1, grav2);
		});
		return sortedColViews;
	}
	
	public TableMode getTableMode() {
		return getTableView().getVisualProperty(BasicTableVisualLexicon.TABLE_VIEW_MODE);
	}
	
	public void setTableMode(TableMode mode) {
		getTableView().setVisualProperty(BasicTableVisualLexicon.TABLE_VIEW_MODE, mode);
	}
	
	public static TableMode getDefaultTableMode() {
		return BasicTableVisualLexicon.TABLE_VIEW_MODE.getDefault();
	}
	
	public String getColumnFormat(String name) {
		View<CyColumn> colView = getTableView().getColumnView(name);
		CellFormat f = colView.getVisualProperty(BasicTableVisualLexicon.CELL_FORMAT);
		if(f == null)
			return null;
		String format = f.getFormat();
		if(format == null)
			return null;
		if(format.isEmpty())
			return null;
		return f.getFormat();
	}
	
	public void setColumnFormat(String name, String format) {
		View<CyColumn> colView = getTableView().getColumnView(name);
		if(colView != null) {
			colView.setLockedValue(BasicTableVisualLexicon.CELL_FORMAT, format);
		}		
	}
	

	@Override
	public void dispose() {
		try {
			renderingEngine.dispose();
		} finally {
			component = null;
			renderingEngine = null;
		}
	}
	
}
