package org.cytoscape.cpath2.internal;

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

import java.util.ArrayList;

import org.cytoscape.cpath2.internal.schemas.search_response.OrganismType;
import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.work.TaskMonitor;

/**
 * Interface for accessing the cPath Web API.
 *
 * @author Ethan Cerami
 */
public interface CPathWebService {

    /**
     * Searches for Physical Entities in cPath Instance.
     * Given a keyword, such as "BRCA1", this method returns the first 10 physical entities
     * which contain this keyword.  For each matching physical entity, you will receive
     * entity details, such as name, synonyms, external links, and list of all pathways in
     * which this entity participates. 
     *
     * @param keyword        Keyword to search for.
     * @param ncbiTaxonomyId Organism filter (-1 to to search all organisms).
     * @param taskMonitor    TaskMonitor Object (can be null);
     * @return SearchResponseType Object.
     * @throws CPathException   CPath Connect Error.
     * @throws EmptySetException    No matches found to specified query.
     */
    public SearchResponseType searchPhysicalEntities(String keyword, int ncbiTaxonomyId,
            TaskMonitor taskMonitor) throws CPathException, EmptySetException;

    /**
     * Gets parent summaries for specified record.
     * For example, if primaryId refers to protein A, the "parent" records are all
     * interactions in which protein A participates.  If primaryId refers to interaction X,
     * the "parent" records are all parent interactions which control or modulate X.
     * To retrieve the full record (instead of just the summary), you must extract the primary
     * ID, and follow-up with a call to getRecordsByIds(). 
     *
     * @param primaryId     Primary ID of Record.
     * @param taskMonitor   Task Monitor Object.
     * @return SummaryResponse Object.
     * @throws CPathException       CPath Error.
     * @throws EmptySetException    Empty Set Error.
     */
    public SummaryResponseType getParentSummaries (long primaryId, TaskMonitor taskMonitor)
            throws CPathException, EmptySetException;

    /**
     * Gets One or more records by primary ID.
     * You can obtain primary IDs for physical entities and/or pathways via the
     * searchPhysicalEntities() method.
     * 
     * @param ids               Array of Primary IDs.
     * @param format            CPathResponseFormat.BIOPAX or CPathResponseFormat.BINARY_SIF.
     * @param taskMonitor       Task Monitor Object.
     * @return  BioPAX XML String or SIF String.
     * @throws CPathException       CPath Error.
     * @throws EmptySetException    Empty Set Error.
     */
    public String getRecordsByIds(long[] ids, CPathResponseFormat format, TaskMonitor taskMonitor)
            throws CPathException, EmptySetException;

    /**
     * Gets a list of all Organisms currently available within the cPath instance.
     *
     * @return ArrayList of Organism Type Objects.
     */
    public ArrayList<OrganismType> getOrganismList();

    /**
     * Abort the Request.
     */
    public void abort();

    /**
     * Registers a new listener.
     *
     * @param listener CPathWebService Listener.
     */
    public void addApiListener(CPathWebServiceListener listener);

    /**
     * Removes the specified listener.
     *
     * @param listener CPathWebService Listener.
     */
    public void removeApiListener(CPathWebServiceListener listener);

    /**
     * Gets the list of all registered listeners.
     *
     * @return ArrayList of CPathWebServiceListener Objects.
     */
    public ArrayList<CPathWebServiceListener> getListeners();
}