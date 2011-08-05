package org.cytoscape.cpath2.internal.view;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListModel;

import org.cytoscape.cpath2.internal.schemas.search_response.ExtendedRecordType;
import org.cytoscape.cpath2.internal.schemas.search_response.OrganismType;
import org.cytoscape.cpath2.internal.view.model.ExtendedRecordWrapper;

/**
 * Extension to JList to Support Tool Tips.
 * <p/>
 * Based on sample code from:
 * http://www.roseindia.net/java/example/java/swing/TooltipTextOfList.shtml
 *
 * @author Ethan Cerami
 */
public class JListWithToolTips extends JList {

    public JListWithToolTips(ListModel listModel) {
        super(listModel);
    }

    /**
     * Impelement Tool Tip Functionality.
     *
     * @param mouseEvent Mouse Event.
     * @return Tool Tip.
     */
    public String getToolTipText(MouseEvent mouseEvent) {
        int index = locationToIndex(mouseEvent.getPoint());
        if (-1 < index) {
            ExtendedRecordWrapper wrapper = (ExtendedRecordWrapper) getModel().getElementAt(index);
            ExtendedRecordType record = wrapper.getRecord();
            StringBuffer html = new StringBuffer();
            html.append("<html>");
            html.append("<table cellpadding=10><tr><td>");
            html.append ("<B>" + record.getName() + "</B>&nbsp;&nbsp;");

            OrganismType organism = record.getOrganism();
            if (organism != null) {
                String speciesName = organism.getSpeciesName();
                html.append ("[" + speciesName + "]");
            }

            //  Next, add synonyms
            List<String> synList = record.getSynonym();
            StringBuffer synBuffer = new StringBuffer();
            if (synList != null && synList.size() > 0) {
                for (String synonym:  synList) {
                    if (!synonym.equalsIgnoreCase(record.getName())) {
                        synBuffer.append("- " + synonym + "<BR>");
                    }
                }
                if (synBuffer.length() > 0) {
                    html.append("<BR><BR>");
                    html.append(synBuffer.toString());
                }
            }

            html.append ("</td></tr></table>");
            html.append ("</html>");
            return html.toString();
        } else {
            return null;
        }
    }


}
