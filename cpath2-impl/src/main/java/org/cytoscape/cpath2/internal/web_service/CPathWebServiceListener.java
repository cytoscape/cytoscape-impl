package org.cytoscape.cpath2.internal.web_service;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
