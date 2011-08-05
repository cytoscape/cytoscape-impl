package org.cytoscape.task.internal.quickstart;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class SelectTableMappingKeyTask extends AbstractTask {
	
	@Tunable(description="Select key column")
	public ListSingleSelection<Integer> keyColumn = new ListSingleSelection<Integer>(1, 2, 3);
	
	
	@Tunable(description="Select Column ID Type")
	public ListSingleSelection<IDType> selection = new ListSingleSelection<IDType>(IDType.ENSEMBL,IDType.ENTREZ_GENE, IDType.UNIPROT);
	
	private final QuickStartState state;
	
	public SelectTableMappingKeyTask(QuickStartState state) {
		this.state = state;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
