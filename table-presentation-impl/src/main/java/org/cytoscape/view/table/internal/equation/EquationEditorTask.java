package org.cytoscape.view.table.internal.equation;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.table.internal.TableRenderingEngineImpl;
import org.cytoscape.view.table.internal.TableViewRendererImpl;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class EquationEditorTask extends AbstractTask {

	private final CyServiceRegistrar registrar;
	private final CyTable table;
	
	public EquationEditorTask(CyServiceRegistrar registrar, CyTable table) {
		this.registrar = registrar;
		this.table = table;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Open Equation Editor");
		var browserTable = getBrowserTable(table, registrar);
		
		if (browserTable == null)
			return;

		// Do not allow opening of the formula builder dialog while a cell is being edited!
		var browserTableModel = browserTable.getBrowserTableModel();
		
		if (browserTableModel == null || browserTable.getCellEditor() != null)
			return;

		int cellRow = browserTable.getSelectedRow();
		int cellCol = browserTable.getSelectedColumn();
		int colIndex = -1;

		// Map the screen index of column to internal index of the table model
		if (cellRow >= 0 && cellCol >= 0) {
			var colName = browserTable.getColumnName(cellCol);
			colIndex = browserTableModel.mapColumnNameToColumnIndex(colName);
		}

		if (cellRow == -1 || cellCol == -1 || !browserTableModel.isCellEditable(cellRow, colIndex)) {
			var rootFrame = registrar.getService(CySwingApplication.class).getJFrame();
			JOptionPane.showMessageDialog(
					rootFrame, 
					"Can't enter a formula w/o a selected cell.",
					"Information", 
					JOptionPane.INFORMATION_MESSAGE
			);
			return;
		}
		
		var mediator = registrar.getService(EquationEditorDialogFactory.class);
		
		SwingUtilities.invokeLater(() -> {
			mediator.openEquationEditorDialog(browserTable);
		});
	}
	
	public static BrowserTable getBrowserTable(CyTable table, CyServiceRegistrar registrar) {
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		var view = tableViewManager.getTableView(table);
		var re = getRenderingEngine(view, registrar);
		
		if (re == null)
			return null;

		return re.getBrowserTable();
	}
	
	private static TableRenderingEngineImpl getRenderingEngine(CyTableView view, CyServiceRegistrar registrar) {
		var renderingEngineManager = registrar.getService(RenderingEngineManager.class);
		var renderingEngines = renderingEngineManager.getRenderingEngines(view);
		
		for (var re : renderingEngines) {
			if (TableViewRendererImpl.ID.equals(re.getRendererId()))
				return (TableRenderingEngineImpl) re;
		}
		
		return null;
	}
}
