package org.cytoscape.task.internal.quickstart.subnetworkbuilder;

import java.util.HashSet;
import java.util.Set;

public class SubnetworkBuilderState {

	private Set<String> queryGenes;
	private Set<String> diseaseGenes;

	private String searchTerms;

	void setQueryGenes(final Set<String> idSet) {
		this.queryGenes = idSet;
	}

	void setDiseaseGenes(final Set<String> idSet) {
		this.diseaseGenes = idSet;
	}

	Set<String> getQueryGenes() {
		if(queryGenes != null)
			return this.queryGenes;
		else
			return new HashSet<String>();
	}

	Set<String> getDiseaseGenes() {
		if(diseaseGenes != null)
			return this.diseaseGenes;
		else
			return new HashSet<String>();
	}

	void setSearchTerms(final String searchTerms) {
		this.searchTerms = searchTerms;
	}

	String getSearchTerms() {
		return this.searchTerms;
	}

}
