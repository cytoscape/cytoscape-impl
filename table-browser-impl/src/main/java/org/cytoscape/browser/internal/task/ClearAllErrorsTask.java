package org.cytoscape.browser.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class ClearAllErrorsTask extends AbstractTableColumnTask {

	private final List<ErrorEquation> deletedEquations = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ClearAllErrorsTask(CyColumn column, CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		final CyTable table = column.getTable();
		final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
		final List<ErrorEquation> errorEquations = new ArrayList<>();
				
		for (CyRow row : table.getAllRows()) {
			if (cancelled)
				return;
			
			final Object raw = row.getRaw(column.getName());

			if (raw instanceof Equation) {
				final Equation eq = (Equation) raw;
				final boolean success =
						compiler.compile(eq.toString(), TableBrowserUtil.getAttNameToTypeMap(table, null));
				//TODO: success is incorrectly set to yes on broken equations [=ABS(String)]
				if (!success || row.get(column.getName(), column.getType()) == null)
					errorEquations.add(new ErrorEquation(row, column.getName(), eq));
			}
		}
		
		for (ErrorEquation err : errorEquations) {
			if (cancelled) {
				restoreDeletedEquations();
				return;
			}
			
			deletedEquations.add(err);
			err.clear();
		}
		
		if (!deletedEquations.isEmpty()) {
			final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
			undoSupport.postEdit(new ClearErrorsEdit(column.getName(), deletedEquations));
		}
	}

	private void restoreDeletedEquations() {
		for (ErrorEquation err : deletedEquations)
			err.restore();
	}
}

class ErrorEquation {
	
	private CyRow row;
	private String columnName;
	private Equation equation;
	
	ErrorEquation(final CyRow row, final String columnName, final Equation equation) {
		this.row = row;
		this.columnName = columnName;
		this.equation = equation;
	}
	
	void clear() {
		row.set(columnName, null);
	}
	
	void restore() {
		row.set(columnName, equation);
	}
}

class ClearErrorsEdit extends AbstractCyEdit {

	private List<ErrorEquation> errEquations;

	public ClearErrorsEdit(final String columnName, final List<ErrorEquation> errEquations) {
		super("Clear all errors in column \"" + columnName + "\"");
		this.errEquations = errEquations;
	}

	@Override
	public void undo() {
		for (ErrorEquation err : errEquations)
			err.restore();
	}

	@Override
	public void redo() {
		for (ErrorEquation err : errEquations)
			err.clear();
	}
}
