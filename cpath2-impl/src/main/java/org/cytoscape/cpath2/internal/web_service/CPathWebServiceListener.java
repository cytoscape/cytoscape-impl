package org.cytoscape.cpath2.internal.web_service;

import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;

/**
 * Listener for listener to Requests made to the cPath Web API.
 *
 * @author Ethan Cerami
 */
public interface CPathWebServiceListener {

    /**
     * Indicates that someone has initiated a search for physical entities.
     *
     * @param keyword        Keyword Term(s)
     * @param ncbiTaxonomyId NCBI Texonomy ID.
     */
    public void searchInitiatedForPhysicalEntities(String keyword, int ncbiTaxonomyId);

    /**
     * Indicates that a search for physical entities has completed.
     *
     * @param peSearchResponse Search Response Object.
     */
    public void searchCompletedForPhysicalEntities(SearchResponseType peSearchResponse);

    /**
     * Indicates that someone has initiated a request for parent summaries.
     *
     * @param primaryId     Primary ID of Child.
     */
    public void requestInitiatedForParentSummaries (long primaryId);

    /**
     * Indicates that a request for parent summaries has completed.
     *
     * @param primaryId         Primary ID of Child.
     * @param summaryResponse   Summary Response Object.
     */
    public void requestCompletedForParentSummaries (long primaryId,
            SummaryResponseType summaryResponse);
}
