package org.cytoscape.task.internal.quickstart;

import javax.swing.JOptionPane;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

class ReportTableLoadSummaryTask extends AbstractTask {
	
	private QuickStartState state;
	private ImportTaskUtil util;
	
	public ReportTableLoadSummaryTask(QuickStartState state, ImportTaskUtil util){
		this.state = state;
		this.util = util;
	}
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {	
		JOptionPane.showMessageDialog(null, state.getTableLoadSummaryMessage(), "Table load summary", JOptionPane.INFORMATION_MESSAGE);
	}
}
