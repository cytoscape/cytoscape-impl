package org.cytoscape.cpath2.internal.task;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.Document;

import org.cytoscape.cpath2.internal.schemas.search_response.ExtendedRecordType;
import org.cytoscape.cpath2.internal.schemas.search_response.OrganismType;
import org.cytoscape.cpath2.internal.schemas.search_response.PathwayType;
import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.schemas.search_response.XRefType;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.cpath2.internal.view.model.InteractionBundleModel;
import org.cytoscape.cpath2.internal.view.model.PathwayTableModel;
import org.cytoscape.cpath2.internal.view.model.RecordList;

/**
 * Indicates that the user has selected a physical entity from the list of search results.
 *
 * @author Ethan Cerami.
 */
public class SelectPhysicalEntity {
    private HashMap<Long, RecordList> parentRecordsMap;

    /**
     * Constructor.
     *
     * @param parentRecordsMap  RecordList.
     */
    public SelectPhysicalEntity (HashMap<Long, RecordList> parentRecordsMap) {
        this.parentRecordsMap = parentRecordsMap;
    }

    /**
     * Select the Phsyical Entity specified by the selected index.
     *
     * @param peSearchResponse      SearchResponseType peSearchResponse.
     * @param selectedIndex         Selected Index.
     * @param interactionBundleModel Interaction Table Model.
     * @param pathwayTableModel     Pathway Table Model.
     * @param summaryDocumentModel  Summary Document Model.
     */
    public void selectPhysicalEntity(SearchResponseType peSearchResponse,
            int selectedIndex, InteractionBundleModel interactionBundleModel, PathwayTableModel
            pathwayTableModel, Document summaryDocumentModel,
            JTextPane textPane, JComponent textPaneOwner) {
        if (peSearchResponse != null) {
            java.util.List<ExtendedRecordType> searchHits = peSearchResponse.getSearchHit();
            ExtendedRecordType searchHit = searchHits.get(selectedIndex);

            StringBuffer html = new StringBuffer();
            html.append("<html>");

            html.append ("<h2>" + searchHit.getName() + "</h2>");

            OrganismType organism = searchHit.getOrganism();
            if (organism != null) {
                String speciesName = organism.getSpeciesName();
                html.append ("<H3>" + speciesName + "</H3>");
            }

            //  Next, add synonyms
            List <String> synList = searchHit.getSynonym();
            StringBuffer synBuffer = new StringBuffer();
            if (synList != null && synList.size() > 0) {
                for (String synonym:  synList) {
                    if (!synonym.equalsIgnoreCase(searchHit.getName())) {
                        synBuffer.append("<LI>- " + synonym + "</LI>");
                    }
                }
                if (synBuffer.length() > 0) {
                    html.append("<h4>Synonyms:</h4>");
                    html.append("<UL>");
                    html.append(synBuffer.toString());
                    html.append("</UL>");
                }
            }

            //  Next, add XRefs
            List <XRefType> xrefList = searchHit.getXref();
            if (xrefList != null && xrefList.size() > 0) {
                html.append("<H4>Links:</H4>");
                html.append("<UL>");
                for (XRefType xref:  xrefList) {
                    String url = xref.getUrl();
                    if (url != null && url.length() > 0) {
                        html.append("<LI>- <a class=\"link\" href=\"" + url + "\">"
                                + xref.getDb() + ":  "
                                + xref.getId() + "</a></LI>") ;
                    } else {
                        html.append("<LI>- " + xref.getDb() + ":  " + xref.getId() + "</LI>");
                    }
                }
                html.append("</UL>");
            }

            List <String> excerptList = searchHit.getExcerpt();
            if (excerptList != null && excerptList.size() > 0) {
                String primeExcerpt = null;
                for (String excerpt:  excerptList) {
                    if (primeExcerpt == null || excerpt.length() > primeExcerpt.length()) {
                        if (!excerpt.equalsIgnoreCase(searchHit.getName())) {
                            primeExcerpt = excerpt;
                        }
                    }
                }
                if (primeExcerpt != null) {
                    html.append("<H4>Matching Excerpt(s):</H4>");
                    html.append("<span class='excerpt'>" + primeExcerpt + "</span><BR>") ;
                }
            }

            //  Temporarily removed comments.
            //java.util.List<String> commentList = searchHit.getComment();
            //if (commentList != null) {
            //    html.append("<BR><B>Description:</B>");
            //    for (int i = commentList.size() - 1; i >= 0; i--) {
            //        html.append("<BR>" + commentList.get(i) + "<BR>");
            //   }
            //}

            html.append ("</html>");
            textPane.setText(html.toString());
            textPane.setCaretPosition(0);
            updatePathwayData(searchHit, pathwayTableModel);
            updateInteractionData(searchHit, interactionBundleModel);
			textPaneOwner.repaint();
        }
    }

    /**
     * Updates Interaction Data.
     *
     * @param searchHit             Search Hit Object.
     * @param interactionBundleModel Interaction Bundle Model.
     */
    private void updateInteractionData(ExtendedRecordType searchHit, InteractionBundleModel
            interactionBundleModel) {
        RecordList recordList = parentRecordsMap.get(searchHit.getPrimaryId());
        if (recordList != null) {
            interactionBundleModel.setRecordList(recordList);
        } else {
            SummaryResponseType summaryResponseType = new SummaryResponseType();
            recordList = new RecordList(summaryResponseType);
            interactionBundleModel.setRecordList(recordList);
        }
        interactionBundleModel.setPhysicalEntityName(searchHit.getName());
    }

    /**
     * Updates Pathway Data.
     *
     * @param searchHit         SearchHit Object.
     * @param pathwayTableModel Pathway Table Model.
     */
    private void updatePathwayData(ExtendedRecordType searchHit, PathwayTableModel
            pathwayTableModel) {
        List<PathwayType> pathwayList = searchHit.getPathwayList().getPathway();

        Vector dataVector = pathwayTableModel.getDataVector();
        dataVector.removeAllElements();

        if (pathwayList != null) {
            pathwayTableModel.setRowCount(pathwayList.size());
            pathwayTableModel.resetInternalIds(pathwayList.size());
            //  Only set the column count, if it is not already set.
            //  If we reset the column count, the user-modified column widths are lost.
            if (pathwayTableModel.getColumnCount() != 2) {
                pathwayTableModel.setColumnCount(2);
            }
            if (pathwayList.size() == 0) {
                pathwayTableModel.setRowCount(1);
                pathwayTableModel.setValueAt("No pathways found.", 0, 0);    
            } else {
                for (int i = 0; i < pathwayList.size(); i++) {
                    PathwayType pathway = pathwayList.get(i);
                    pathwayTableModel.setValueAt(pathway.getName(), i, 0);
                    pathwayTableModel.setValueAt(pathway.getDataSource().getName(), i, 1);
                    pathwayTableModel.setInternalId(i, pathway.getPrimaryId());
                }
            }
        }
    }
}