package org.cytoscape.cpath2.internal.task;

import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExecutePhysicalEntitySearchTaskFactory extends AbstractTaskFactory {
	private final CPathWebService webApi;
	private final String keyword;
	private final int ncbiTaxonomyId;
	private ExecutePhysicalEntitySearch task;
	private ResultHandler result;

	public ExecutePhysicalEntitySearchTaskFactory(CPathWebService webApi, String keyword, int ncbiTaxonomyId, ResultHandler result) {
		this.webApi = webApi;
		this.keyword = keyword;
		this.ncbiTaxonomyId = ncbiTaxonomyId;
		this.result = result;
	}

	@Override
	public TaskIterator createTaskIterator() {
		task = new ExecutePhysicalEntitySearch(webApi, keyword, ncbiTaxonomyId, result);
		return new TaskIterator(task);
	}

	public interface ResultHandler {
		void finished(int matchesFound) throws Exception;
	}
}
