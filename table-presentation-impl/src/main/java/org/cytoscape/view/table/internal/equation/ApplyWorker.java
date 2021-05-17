package org.cytoscape.view.table.internal.equation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.cytoscape.equations.Equation;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.service.util.CyServiceRegistrar;

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

public class ApplyWorker extends SwingWorker<InternalResult,Void> {
	
	private final CyServiceRegistrar registrar;
	private final EquationEditorPanel builderPanel;
	
	private final Equation equation;
	private final CyColumn col;
	private final Collection<CyRow> rows;
	private final boolean insert;
	
	
	public ApplyWorker(
			CyServiceRegistrar registrar, 
			EquationEditorPanel builderPanel, 
			Equation equation, 
			CyColumn col, 
			Collection<CyRow> rows, 
			boolean insert
	) {
		this.registrar = registrar;
		this.builderPanel = builderPanel;
		this.insert = insert;
		this.equation = equation;
		this.col = col;
		this.rows = rows;
	}
	
	/**
	 * Run the equation evaluation on Swing's worker background thread.
	 */
	@Override
    protected InternalResult doInBackground() {
    	Set<String> errors = new HashSet<>();
		int numErrors = 0;
		
		CyEventHelper eventHelper = registrar.getService(CyEventHelper.class);
		
		CyTable table = col.getTable();
		eventHelper.silenceEventSource(table);
		
		var rowsChanged = new ArrayList<RowSetRecord>();
		
		try {
			for(CyRow row : rows) {
				boolean error = false;
				String colName = col.getName();
				row.set(colName, equation);
				
				Object x = null;
				try {
					x = row.get(colName, col.getType());
				} catch(Exception e) {
					error = true;
					errors.add(e.getMessage());
				}
				if(x == null) {
					String errorMessage = row.getTable().getLastInternalError();
					if(errorMessage != null && !errorMessage.isBlank()) {
						error = true;
						numErrors++;
						errors.add(errorMessage);
					}
				}
				
				if(insert) {
					rowsChanged.add(new RowSetRecord(row, colName, x, equation));
				} else {
					// replace the equation with the result of the equation
					var value = error ? null : x;
					row.set(colName, value);
					rowsChanged.add(new RowSetRecord(row, colName, value, value));
				}
			}
		} finally {
			eventHelper.unsilenceEventSource(table);
		}
		
		var event = new RowsSetEvent(table, rowsChanged);
		eventHelper.fireEvent(event);
		
		return new InternalResult(errors, numErrors);
	}
    
    
    /**
     * This method runs on the EDT, update the UI with the results.
     */
	@Override
    protected void done() {
		try {
			var result = get();
			if(result.errors.isEmpty()) {
				builderPanel.getSyntaxPanel().showEvalSuccess(rows.size());
			} else {
				builderPanel.getSyntaxPanel().showEvalError(rows.size(), result.numErrors, result.errors);
			}
		} catch (InterruptedException | ExecutionException e) {
			return;
		}
    }
}

class InternalResult {
	
	final Set<String> errors;
	final int numErrors;
	
	public InternalResult(Set<String> errors, int numErrors) {
		this.numErrors = numErrors;
		this.errors = errors;
	}
}
