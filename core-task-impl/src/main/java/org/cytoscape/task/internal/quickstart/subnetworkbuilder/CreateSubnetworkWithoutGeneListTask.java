package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.task.internal.quickstart.IDType;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSubnetworkWithoutGeneListTask extends CreateSubnetworkFromSearchTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CreateSubnetworkWithoutGeneListTask.class);
	
	private final String queryGenes;
	private final IDType idType;

	public CreateSubnetworkWithoutGeneListTask(SubnetworkBuilderUtil util, SubnetworkBuilderState state, final String queryGenes, final IDType idType) {
		super(util, state);
		this.idType = idType;
		this.queryGenes = queryGenes;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Searching related genes in parent network...");
		taskMonitor.setProgress(-1);

		final List<String> geneList;
		
		if (idType == IDType.ENTREZ_GENE) {
			final String[] genes = queryGenes.split("\\s+");
			logger.debug("Got gene list: " + genes.length);
			geneList = Arrays.asList(genes);
		} else {
			geneList = new ArrayList<String>(convert(idType));
		}
		
		for (final String gene : geneList)
			logger.debug("Gene Found: " + gene);
		
		selectGenes(geneList);

		taskMonitor.setProgress(1.0);

	}
	
	private Set<String> convert(IDType selected) throws IOException {
		final boolean isGeneSymbol;

		if (selected == IDType.GENE_SYMBOL)
			isGeneSymbol = true;
		else
			isGeneSymbol = false;

		final NCBISearchClient client = new NCBISearchClient();

		return client.convert(queryGenes, isGeneSymbol);
	}

}
