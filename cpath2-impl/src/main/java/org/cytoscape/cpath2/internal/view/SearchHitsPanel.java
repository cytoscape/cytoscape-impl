package org.cytoscape.cpath2.internal.view;

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.cpath2.internal.CPathFactory;
import org.cytoscape.cpath2.internal.CPathWebService;
import org.cytoscape.cpath2.internal.CPathWebServiceListener;
import org.cytoscape.cpath2.internal.schemas.search_response.ExtendedRecordType;
import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.cpath2.internal.task.SelectPhysicalEntity;

/**
 * Search Hits Panel.
 *
 * @author Ethan Cerami.
 */
public class SearchHitsPanel extends JPanel implements CPathWebServiceListener, CytoPanelStateChangedListener {
    private DefaultListModel peListModel;
    private JList peList;
    private SearchResponseType peSearchResponse;
    private Document summaryDocument;
    private String currentKeyword;
    private InteractionBundleModel interactionBundleModel;
    private PathwayTableModel pathwayTableModel;
    private JTextPane summaryTextPane;
    private PhysicalEntityDetailsPanel peDetailsPanel;
	private JLayeredPane appLayeredPane;
    private HashMap <Long, RecordList> parentRecordsMap;
	private CytoPanelState cytoPanelState;
    private JFrame detailsFrame;
	private final CPathFactory factory;

    /**
     * Constructor.
     * @param interactionBundleModel    Interaction Table Model.
     * @param pathwayTableModel         Pathway Table Model.
     * @param webApi                    cPath Web API.
     */
    public SearchHitsPanel(InteractionBundleModel interactionBundleModel, PathwayTableModel
            pathwayTableModel, CPathWebService webApi, CPathFactory factory) {
    	this.factory = factory;
        this.interactionBundleModel = interactionBundleModel;
        this.pathwayTableModel = pathwayTableModel;
        CySwingApplication application = factory.getCySwingApplication();
		appLayeredPane = application.getJFrame().getRootPane().getLayeredPane();
        webApi.addApiListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //  Create the Summary Panel, but don't show it yet
        peDetailsPanel = factory.createPhysicalEntityDetailsPanel(this);
        summaryDocument = peDetailsPanel.getDocument();
        summaryTextPane = peDetailsPanel.getTextPane();

		// create popup window
//		modalPanel = new ModalPanel();
//		popup = new PopupPanel(appLayeredPane, peDetailsPanel, modalPanel);
//		appLayeredPane.add(modalPanel, 1000);
//		appLayeredPane.add(popup, 1000);

        //  Create the Hit List
        peListModel = new DefaultListModel();
        peList = createHitJList(peListModel);

        JPanel hitListPane = new JPanel();
        hitListPane.setLayout(new BorderLayout());
        JScrollPane hitListScrollPane = new JScrollPane(peList);
        hitListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        GradientHeader header = new GradientHeader("Step 2:  Select");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        hitListPane.add(header, BorderLayout.NORTH);
        JSplitPane internalPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, hitListScrollPane,
                peDetailsPanel);
        internalPanel.setDividerLocation(100);
        hitListPane.add(internalPanel, BorderLayout.CENTER);

        //  Create Search Details Panel
        SearchDetailsPanel detailsPanel = factory.createSearchDetailsPanel(interactionBundleModel,
                pathwayTableModel);

        //  Create the Split Pane
        JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, hitListPane,
                detailsPanel);
        splitPane.setDividerLocation(200);
        splitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(splitPane);
        createListener(interactionBundleModel, pathwayTableModel, summaryTextPane);

		// listener for cytopanel events
		CytoPanel cytoPanel = application.getCytoPanel(CytoPanelName.EAST);
		cytoPanelState = cytoPanel.getState();
    }

    private JList createHitJList(DefaultListModel peListModel) {
        JList peList = new JListWithToolTips(peListModel);
        peList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        peList.setPrototypeCellValue("12345678901234567890");
        return peList;
    }

    /**
     * Indicates that user has initiated a phsyical entity search.
     *
     * @param keyword        Keyword.
     * @param ncbiTaxonomyId NCBI Taxonomy ID.
     */
    public void searchInitiatedForPhysicalEntities(String keyword, int ncbiTaxonomyId) {
        this.currentKeyword = keyword;
    }

    /**
     * Indicates that a search for physical entities has just completed.
     *
     * @param peSearchResponse PhysicalEntitySearchResponse Object.
     */
    public void searchCompletedForPhysicalEntities(final SearchResponseType peSearchResponse) {

        if (peSearchResponse.getTotalNumHits() > 0) {

            //  Reset parent summary map
            parentRecordsMap = new HashMap<Long, RecordList>();
            
            //  store for later reference
            this.peSearchResponse = peSearchResponse;

            //  Populate the hit list
            List<ExtendedRecordType> searchHits = peSearchResponse.getSearchHit();
            peListModel.setSize(searchHits.size());
            int i = 0;
            for (ExtendedRecordType searchHit : searchHits) {
                ExtendedRecordWrapper wrapper = new ExtendedRecordWrapper (searchHit);
                peListModel.setElementAt(wrapper, i++);
            }
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    Window window = SwingUtilities.getWindowAncestor(SearchHitsPanel.this);
                    JOptionPane.showMessageDialog(window, "No matches found for:  "
                            + currentKeyword + ".  Please try again.", "Search Results",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }

    public void requestInitiatedForParentSummaries(long primaryId) {
        //  Currently no-op
    }

    public void requestCompletedForParentSummaries(long primaryId,
            SummaryResponseType summaryResponse) {
        //  Store parent summaries for later reference

        RecordList recordList = new RecordList(summaryResponse);
        parentRecordsMap.put(primaryId, recordList);

        //  If we have just received parent summaries for the first search hit, select it.
        if (peSearchResponse != null) {
            List <ExtendedRecordType> searchHits = peSearchResponse.getSearchHit();
            if (searchHits.size() > 0) {
                ExtendedRecordType searchHit = searchHits.get(0);
                if (primaryId == searchHit.getPrimaryId()) {
                    peList.setSelectedIndex(0);
                    SelectPhysicalEntity selectTask = new SelectPhysicalEntity(parentRecordsMap);
                    selectTask.selectPhysicalEntity(peSearchResponse, 0,
                            interactionBundleModel, pathwayTableModel, summaryDocument,
                                                    summaryTextPane, appLayeredPane);
                }
            }
        }
    }

    /**
     * Listen for list selection events.
     *
     * @param interactionBundleModel InteractionBundleModel.
     * @param pathwayTableModel     PathwayTableModel.
     */
    private void createListener(final InteractionBundleModel interactionBundleModel,
            final PathwayTableModel pathwayTableModel, final JTextPane textPane) {
        peList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                int selectedIndex = peList.getSelectedIndex();
                //  Ignore the "unselect" event.
                if (!listSelectionEvent.getValueIsAdjusting()) {
                    if (selectedIndex >=0) {
                        SelectPhysicalEntity selectTask = new SelectPhysicalEntity(parentRecordsMap);
                        selectTask.selectPhysicalEntity(peSearchResponse, selectedIndex,
                                interactionBundleModel, pathwayTableModel, summaryDocument,
                                textPane, appLayeredPane);
                    }
                }
            }
        });
    }

	@Override
	public void handleEvent(CytoPanelStateChangedEvent e) {
		cytoPanelState = e.getNewState();
	}
}