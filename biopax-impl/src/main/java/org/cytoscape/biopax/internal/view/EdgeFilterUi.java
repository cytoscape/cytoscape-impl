package org.cytoscape.biopax.internal.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

/**
 * Edge Filter Dialog.
 */
public class EdgeFilterUi extends JDialog {
    private CyNetwork cyNetwork;
    private HashSet checkBoxSet;
	private final CySwingApplication application;

    /**
     * Constructor.
     * @param cyNetwork CyNetwork Object.
     */
    public EdgeFilterUi(CyNetwork cyNetwork, CySwingApplication application) {
    	this.application = application;
        this.cyNetwork = cyNetwork;
        initGui();
    }

    /**
     * Initializes GUI.
     */
    private void initGui() {
        this.setModal(true);
        this.setTitle("Edge Filter");
        checkBoxSet = new HashSet();
        Set interactionSet = new TreeSet();
        for (CyEdge edge : cyNetwork.getEdgeList()) {
        	CyRow row = cyNetwork.getCyRow(edge);
        	// TODO: Where did Semantics.INTERACTION go?
            String interactionType = row.get("interaction", String.class);
            interactionSet.add(interactionType);
        }
        Iterator interactionIterator = interactionSet.iterator();

        JPanel edgeSetPanel = new JPanel();

        Border emptyBorder = new EmptyBorder (10,10,10,100);
        Border titledBorder = new TitledBorder ("Edge Filter");
        CompoundBorder compoundBorder = new CompoundBorder (titledBorder, emptyBorder);
        edgeSetPanel.setBorder(compoundBorder);
        edgeSetPanel.setLayout(new BoxLayout(edgeSetPanel, BoxLayout.Y_AXIS));
        while (interactionIterator.hasNext()) {
            String interactionType = (String) interactionIterator.next();
            JCheckBox checkBox = new JCheckBox (interactionType);
            checkBox.setActionCommand(interactionType);
            checkBox.addActionListener(new ApplyEdgeFilter(cyNetwork, checkBoxSet));
            checkBox.setSelected(true);
            edgeSetPanel.add(checkBox);
            checkBoxSet.add(checkBox);
        }

        //  Select all edges
        ApplyEdgeFilter apply = new ApplyEdgeFilter (cyNetwork, checkBoxSet);
        apply.executeFilter();

        Container contentPane = this.getContentPane();
        contentPane.setLayout (new BorderLayout());
        contentPane.add(edgeSetPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton closeButton = new JButton ("Close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                EdgeFilterUi.this.dispose();

            }
        });
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(closeButton);
        contentPane.add(panel, BorderLayout.SOUTH);

        this.setLocationRelativeTo(application.getJFrame());
        this.pack();
        this.setVisible(true);
    }

}

/**
 * Apply Edge Filter.
 */
class ApplyEdgeFilter implements ActionListener {
    private CyNetwork cyNetwork;
    private HashSet checkBoxSet;

    /**
     * Constructor.
     * @param cyNetwork     CyNetwork Object.
     * @param checkBoxSet   Set of JCheckBox Objects.
     */
    public ApplyEdgeFilter (CyNetwork cyNetwork, HashSet checkBoxSet) {
        this.cyNetwork = cyNetwork;
        this.checkBoxSet = checkBoxSet;
    }

    /**
     * Check Box selected or unselected.
     * @param actionEvent Action Event.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        executeFilter();
    }

    /**
     * Executes the Edge Filter.
     */
    public void executeFilter() {
    	// TODO: Port this.  How do we hide things?
//        HashSet selectedInteractionSet = new HashSet();
//        Iterator checkBoxIterator = checkBoxSet.iterator();
//        while (checkBoxIterator.hasNext()) {
//            JCheckBox checkBox = (JCheckBox) checkBoxIterator.next();
//            String action = checkBox.getActionCommand();
//            if (checkBox.isSelected()) {
//                selectedInteractionSet.add(action);
//            }
//        }
//        ArrayList edgeSet = new ArrayList();
//        for (int i=0; i< edgeList.size(); i++) {
//            CyEdge edge = (CyEdge) edgeList.get(i);
//            String interactionType = edgeAttributes.getStringAttribute(edge.getIdentifier(),
//                    Semantics.INTERACTION);
//            if (!selectedInteractionSet.contains(interactionType)) {
//                edgeSet.add(edge);
//            }
//        }
//
//        CyNetworkView networkView = Cytoscape.getCurrentNetworkView();
//        GinyUtils.unHideAll(networkView);
//        ArrayList edgeViewList = new ArrayList();
//        for (int i=0; i<edgeSet.size(); i++) {
//            CyEdge edge = (CyEdge) edgeSet.get(i);
//            EdgeView edgeView = networkView.getEdgeView(edge);
//            edgeViewList.add(edgeView);
//
//        }
//      
//        networkView.hideGraphObjects(edgeViewList);
    }
}
