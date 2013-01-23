package org.cytoscape.cpath2.internal.task;

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

import java.awt.Window;
import java.util.List;

import javax.swing.JPanel;

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
	private final JPanel parentPanel;

    /**
     * Constructor.
     *
     * @param webApi         cPath Web Api.
     * @param keyword        Keyword
     * @param ncbiTaxonomyId NCBI Taxonomy ID.
     * @param result 
     * @param parentPanel 
     */
    public ExecutePhysicalEntitySearch(CPathWebService webApi, String keyword,
            int ncbiTaxonomyId, ResultHandler result, final JPanel parentPanel) {
        this.webApi = webApi;
        this.keyword = keyword;
        this.ncbiTaxonomyId = ncbiTaxonomyId;
        this.result = result;
        this.parentPanel = parentPanel;
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
			Window parentWindow = ((Window) parentPanel.getRootPane().getParent());
			parentPanel.repaint();
			parentWindow.toFront();
        }
    }
}