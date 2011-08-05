package org.cytoscape.task.internal.quickstart;

import java.util.List;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyColumn;
import java.util.Iterator;

///////
public class GetAttributePreviewDataTask extends AbstractTask {
	private String[] previewKey;
	private String[][] previewData;
	private CyTableReader reader;
	private QuickStartState state;
	
	public GetAttributePreviewDataTask(QuickStartState state, CyTableReader reader, String[] previewKey, String[][] previewData){
		this.reader = reader;
		this.previewKey = previewKey;
		this.previewData = previewData;
		this.state = state;
	}
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {
	
		CyTable[] tbls = reader.getCyTables();
		
		CyColumn keyCol = tbls[0].getPrimaryKey();
		
		this.previewKey[0] = keyCol.getName();
		
		List values = keyCol.getValues(keyCol.getType());
		
		int colCount = this.previewData.length;
		if (values.size()< colCount){
			colCount = values.size();
		}
		
		for (int i=0; i< colCount; i++){
			this.previewData[i][0] = values.get(i).toString();
		}
		
		//
		String msg = "column(s): ";
		Iterator<CyColumn> it = tbls[0].getColumns().iterator();
		while (it.hasNext()){
			CyColumn col = it.next();
			msg += col.getName()+ " ";
		}
		msg += "\nTotal rows: "+tbls[0].getRowCount();
		
		state.setTableLoadSummaryMessage(msg);
	}
}