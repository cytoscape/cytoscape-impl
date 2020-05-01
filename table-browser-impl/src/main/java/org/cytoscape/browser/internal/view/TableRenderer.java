package org.cytoscape.browser.internal.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.TableRenderingEngine;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.TableMode;

/**
 * This class just holds on to the RenderingEngine and its associated JComponenet
 * and makes it easier to pass them around.
 */
public class TableRenderer implements CyDisposable {

	private TableRenderingEngine renderingEngine;
	private JComponent component;
	
	public TableRenderer(TableRenderingEngine renderingEngine, JComponent component) {
		this.renderingEngine = renderingEngine;
		this.component = component;
	}

	public TableRenderingEngine getRenderingEngine() {
		return renderingEngine;
	}

	public JComponent getComponent() {
		return component;
	}
	
	public CyTableView getTableView() {
		return renderingEngine.getViewModel();
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
			colView.setVisualProperty(BasicTableVisualLexicon.COLUMN_VISIBLE, visible);
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
			colView.setVisualProperty(BasicTableVisualLexicon.COLUMN_GRAVITY, grav);
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
