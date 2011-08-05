package org.cytoscape.cpath2.internal.task;

import javax.swing.JDialog;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.schemas.summary_response.SummaryResponseType;
import org.cytoscape.cpath2.internal.view.InteractionBundlePanel;
import org.cytoscape.cpath2.internal.view.model.InteractionBundleModel;
import org.cytoscape.cpath2.internal.view.model.RecordList;
import org.cytoscape.cpath2.internal.web_service.CPathException;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.cpath2.internal.web_service.CPathWebServiceImpl;
import org.cytoscape.cpath2.internal.web_service.EmptySetException;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class GetParentInteractions implements Task {
    private long cpathId;
    private CPathWebService webApi = CPathWebServiceImpl.getInstance();
    private InteractionBundleModel interactionBundleModel;
	private final CyNetwork network;
    private final CyNode node;
	private final CPath2Factory factory;

    public GetParentInteractions (CyNetwork network, CyNode node, CPath2Factory factory) {
    	// TODO: Investigate what alternatives we can use for getIdentifier()
        this.cpathId = node.getSUID();
        this.node = node;
        this.network = network;
        this.factory = factory;
    }

    @Override
    public void cancel() {
        webApi.abort();
    }
    
    public void run(TaskMonitor taskMonitor) throws Exception {
    	taskMonitor.setTitle("Getting neighbors...");
        try {
            taskMonitor.setStatusMessage("Retrieving neighborhood summary.");
            SummaryResponseType response = webApi.getParentSummaries(cpathId, taskMonitor);
            RecordList recordList = new RecordList(response);
            interactionBundleModel = new InteractionBundleModel();
            interactionBundleModel.setRecordList(recordList);
            interactionBundleModel.setPhysicalEntityName("Network Neighborhood");

            CySwingApplication application = factory.getCySwingApplication();
            JDialog dialog = new JDialog(application.getJFrame());

            String nodeLabel = node.getCyRow().get(BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, String.class);
            if (nodeLabel != null) {
                dialog.setTitle(nodeLabel);
            } else {
                dialog.setTitle("Neighborhood");
            }
            InteractionBundlePanel interactionBundlePanel = factory.createInteractionBundlePanel(interactionBundleModel, network, dialog);
            dialog.getContentPane().add(interactionBundlePanel);
            interactionBundleModel.setRecordList(recordList);
            interactionBundlePanel.expandAllNodes();
            dialog.pack();
            dialog.setLocationRelativeTo(application.getJFrame());
            dialog.setVisible(true);
        } catch (CPathException e) {
            if (e.getErrorCode() != CPathException.ERROR_CANCELED_BY_USER) {
            	throw e;
            }
        } catch (EmptySetException e) {
        	throw new Exception("No neighbors found for selected node.", e);
        }
    }
    
    public InteractionBundleModel getInteractionBundle() {
        return this.interactionBundleModel;
    }
}
