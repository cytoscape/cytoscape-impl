package org.cytoscape.task.internal.quickstart;


import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.CreateSubnetworkWithoutGeneListTask;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SearchRelatedGenesTask;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderState;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartTask extends ImportNetworkFromPublicDataSetTask {
	private static final Logger logger = LoggerFactory.getLogger(StartTask.class);
	
	@Tunable(description = "Gene Ontology (Optional)")
	public String go;

	@Tunable(description = "Disease/Phenotype (Optional)")
	public String phenotype;
	
	@Tunable(description = "List of genes you are interested in (Optional. Separated by spaces)")
	public String queryGenes;

	@Tunable(description = "Gene ID Type (Optional)")
	public ListSingleSelection<IDType> selection =
		new ListSingleSelection<IDType>(IDType.GENE_SYMBOL, IDType.ENSEMBL,
		                                IDType.ENTREZ_GENE, IDType.UNIPROT);

	protected final QuickStartState state;
	protected final ImportTaskUtil importTaskUtil;
	protected final CyNetworkManager networkManager;
	protected final SubnetworkBuilderUtil subnetworkUtil;
	
	public StartTask(final QuickStartState state, final ImportTaskUtil importTaskUtil,
	                 final CyNetworkManager networkManager,
	                 final SubnetworkBuilderUtil subnetworkUtil)
	{
		super(importTaskUtil.processors, importTaskUtil.mgr, networkManager,
		      importTaskUtil.networkViewManager, importTaskUtil.props,
		      importTaskUtil.cyNetworkNaming, importTaskUtil.streamUtil);
		this.state = state;
		this.importTaskUtil = importTaskUtil;
		this.networkManager = networkManager;
		this.subnetworkUtil = subnetworkUtil;
	}

	public void run(TaskMonitor tm) throws Exception {
		if (go != null && go.trim().length() != 0
		    || phenotype != null && phenotype.trim().length() != 0)
		{
			logger.info("GO = " + go);
			logger.info("Phenotype = " + phenotype);
			insertTasksAfterCurrentTask(
				new SearchRelatedGenesTask(subnetworkUtil,
				                           new SubnetworkBuilderState(), go,
				                           phenotype));
		}
		
		if (queryGenes != null && queryGenes.trim().length() != 0) {
			logger.info("Query Genes: " + queryGenes);
			final IDType selectedType = selection.getSelectedValue();
			insertTasksAfterCurrentTask(
				new CreateSubnetworkWithoutGeneListTask(subnetworkUtil,
				                                        new SubnetworkBuilderState(),
				                                        queryGenes, selectedType));
		}
		
		super.run(tm);
	}
}
