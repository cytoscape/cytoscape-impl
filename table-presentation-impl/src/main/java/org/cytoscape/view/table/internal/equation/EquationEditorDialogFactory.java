package org.cytoscape.view.table.internal.equation;

import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.table.internal.impl.BrowserTable;

public class EquationEditorDialogFactory {
	
	private final CyServiceRegistrar registrar;
	
	public EquationEditorDialogFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public void openEquationEditorDialog(BrowserTable browserTable) {
		String equation = getInitialEquation(browserTable);
		EquationEditorMediator.openEquationEditorDialog(browserTable, equation, registrar);
	}
	
	private static String getInitialEquation(BrowserTable browserTable) {
		int cellRow = browserTable.getSelectedRow();
		int cellCol = browserTable.getSelectedColumn();
		
		String colName = browserTable.getColumnName(cellCol);
		CyRow row = browserTable.getBrowserTableModel().getCyRow(cellRow);
		
		Object obj = row.getRaw(colName);
		if(obj instanceof Equation) {
			Equation equation = (Equation) obj;
			return equation.toString().trim().substring(1); // remove '='
		}
		return null;
	}
}
