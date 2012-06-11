package org.cytoscape.cpath2.internal.task;

import java.util.List;

import org.cytoscape.cpath2.internal.schemas.search_response.ExtendedRecordType;
import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.web_service.CPathException;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.EmptySetException;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Controller for Executing a Physical Entity Search.
 *
 * @author Ethan Cerami.
 */
public class ExecutePhysicalEntitySearch implements Task {
	
	public static interface ResultHandler {
		void finished(int matchesFound) throws Exception;
	}
	
	private CPathWebService webApi;
    private String keyword;
    private int ncbiTaxonomyId;
	private ResultHandler result;

    /**
     * Constructor.
     *
     * @param webApi         cPath Web Api.
     * @param keyword        Keyword
     * @param ncbiTaxonomyId NCBI Taxonomy ID.
     * @param result 
     */
    public ExecutePhysicalEntitySearch(CPathWebService webApi, String keyword,
            int ncbiTaxonomyId, ResultHandler result) {
        this.webApi = webApi;
        this.keyword = keyword;
        this.ncbiTaxonomyId = ncbiTaxonomyId;
        this.result = result;
    }

    /**
     * Our implementation of Task.abort()
     */
    public void cancel() {
        webApi.abort();
    }

    /**
     * Our implementation of Task.getTitle.
     *
     * @return Task Title.
     */
    public String getTitle() {
        return "Searching " + CPathProperties.getInstance().getCPathServerName() + "...";
    }

    /**
     * Our implementation of Task.run().
     */
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
    	int numHits = 0;
        try {
            // read the network from cpath instance
            taskMonitor.setProgress(0);
            taskMonitor.setStatusMessage("Executing Search");

            //  Execute the Search
            SearchResponseType searchResponse = webApi.searchPhysicalEntities(keyword,
                    ncbiTaxonomyId, taskMonitor);
            List<ExtendedRecordType> searchHits = searchResponse.getSearchHit();

            numHits = searchHits.size();
            int numRetrieved = 1;
            taskMonitor.setProgress(0.01);
            for (ExtendedRecordType hit:  searchHits) {
                taskMonitor.setStatusMessage("Retrieving interaction details for:  " +
                    hit.getName());
                try {
                    webApi.getParentSummaries(hit.getPrimaryId(), taskMonitor);
                } catch (EmptySetException e) {
                }
                double progress = numRetrieved++ / (double) numHits;
                taskMonitor.setProgress(progress);
            }
        } catch (EmptySetException e) {
        } catch (CPathException e) {
            if (e.getErrorCode() != CPathException.ERROR_CANCELED_BY_USER) {
            	throw e;
            }
        } finally {
            taskMonitor.setStatusMessage("Done");
            taskMonitor.setProgress(1);
            result.finished(numHits);
        }
    }
}