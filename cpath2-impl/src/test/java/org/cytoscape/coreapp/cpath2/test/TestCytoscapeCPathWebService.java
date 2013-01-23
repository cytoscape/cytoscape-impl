package org.cytoscape.coreapp.cpath2.test;

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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

/**
 * Test Harness for the CytosacpeCPathWebService.
 *
 * NB:  The test below connect to the live instance of Pathway Commons.
 * As such, the data is likely to change, and these tests are likely to break.
 * 
 */
public class TestCytoscapeCPathWebService extends TestCase {

    public void testWebService() { assertTrue(true); } 
//    public void DO_NOT_testWebService() throws EmptySetException, CPathException {
//        try {
//            CyMain.main(new String[0]);
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        //  First, create the client and register it.
//        WebServiceClient wsClient = CytoscapeCPathWebService.getClient();
//        WebServiceClientManager.registerClient(wsClient);
//
//        //  Get the client back from the manager
//        wsClient = WebServiceClientManager.getClient(wsClient.getClientID());
//        //validateStub(wsClient);
//
//        CyWebServiceEventSupport eventManager =
//                WebServiceClientManager.getCyWebServiceEventSupport();
//        validateSearchEvent(wsClient, eventManager);
//        validateImportEvent(eventManager);
//
//        System.out.println("Press <ENTER> to exit: ");
//        InputStreamReader converter = new InputStreamReader(System.in);
//        BufferedReader in = new BufferedReader(converter);
//        try {
//            in.readLine();
//        } catch (IOException e) {
//        }
//    }
//
//    private void validateImportEvent(CyWebServiceEventSupport eventManager) {
//        CyWebServiceEvent wsEvent = new CyWebServiceEvent
//                (CPathProperties.getInstance().getWebServicesId(),
//                        CyWebServiceEvent.WSEventType.IMPORT_NETWORK, "1");
//        try {
//            eventManager.fireCyWebServiceEvent(wsEvent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void validateSearchEvent(WebServiceClient wsClient, CyWebServiceEventSupport eventManager) {
//        //  Set Organism Filter
//        ModuleProperties props = wsClient.getProps();
//        props.add(new Tunable(CytoscapeCPathWebService.NCBI_TAXONOMY_ID_FILTER,
//                "Filter by Organism - NCBI Taxonomy ID",
//                Tunable.INTEGER, new Integer(9606)));
//        CyWebServiceEvent wsEvent = new CyWebServiceEvent
//                (CPathProperties.getInstance().getWebServicesId(),
//                        CyWebServiceEvent.WSEventType.SEARCH_DATABASE, "brca1");
//
//        // TODO:  Recmd:  fire should throw something narrower than Exception
//        Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(new MiniListener());
//        try {
//            eventManager.fireCyWebServiceEvent(wsEvent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void validateStub(WebServiceClient wsClient) throws CPathException, EmptySetException {
//        //  1.  try getting the stub and executing calls that way.
//        CPathWebService webApi = (CPathWebService) wsClient.getClientStub();
//
//        //  a.  Search Physical Entities
//        SearchResponseType responseType =
//                webApi.searchPhysicalEntities("brca1", -1, null);
//        assertTrue (responseType.getTotalNumHits() > 0);
//
//        //  b.  Get Records by ID
//        long ids[] = new long[1];
//        ids[0] = 1;
//        String response = webApi.getRecordsByIds(ids, CPathResponseFormat.BIOPAX, null);
//        assertTrue (response.length() > 100);
//        response = webApi.getRecordsByIds(ids, CPathResponseFormat.BINARY_SIF, null);
//        assertTrue (response.length() > 100);
//
//        //  c. Get Organism List, which is not (yet) implemented.
//        try {
//            webApi.getOrganismList();
//            fail ("UnsupportedOperationException should have been thrown.");
//        } catch (UnsupportedOperationException e) {
//        }
//
//        //  d.  Get Parent Summaries.
//        SummaryResponseType summaryResponseType = webApi.getParentSummaries(100, null);
//        List<BasicRecordType> list = summaryResponseType.getRecord();
//        assertTrue (list.size() > 1) ;
//    }
}

class MiniListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//        if (propertyChangeEvent.getPropertyName().equals("SEARCH_RESULT")) {
//            DatabaseSearchResult result = (DatabaseSearchResult) propertyChangeEvent.getNewValue();
//            System.out.println(result.getResultSize());
//        }
    }
}
