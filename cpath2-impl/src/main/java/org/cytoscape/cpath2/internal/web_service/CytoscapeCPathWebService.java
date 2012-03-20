package org.cytoscape.cpath2.internal.web_service;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.CPathNetworkImportTask;
import org.cytoscape.cpath2.internal.plugin.CPathPlugIn2;
import org.cytoscape.cpath2.internal.view.TabUi;
import org.cytoscape.cpath2.internal.view.cPathSearchPanel;
import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;

/**
 * CPath Web Service, integrated into the Cytoscape Web Services Framework.
 */
public class CytoscapeCPathWebService extends AbstractWebServiceGUIClient 
	implements NetworkImportWebServiceClient, SearchWebServiceClient<Object> 
{
    // Display name of this client.
    private static final String DISPLAY_NAME = CPathProperties.getInstance().getCPathServerName() +
            " Web Service Client";

    /**
     * NCBI Taxonomy ID Filter.
     */
    public static final String NCBI_TAXONOMY_ID_FILTER = "ncbi_taxonomy_id_filter";

    /**
     * Response Format.
     */
    public static final String RESPONSE_FORMAT = "response_format";

	@Tunable(description="Filter by Organism - NCBI Taxonomy ID")
	Integer taxonomyId = -1;
	
    private JPanel mainPanel;

	private final CPath2Factory factory;

	private CPathWebService webApi;

    public List<JMenuItem> getNodeContextMenuItems(View<CyNode> nodeView) {
    	// TODO: Figure out how we're going to wire this up with OSGi
//        CyNetworkView networkView = (CyNetworkView) nodeView.getGraphView();
//        CyNetwork cyNetwork = networkView.getNetwork();
//        CyAttributes networkAttributes  = Cytoscape.getNetworkAttributes();
//        Boolean b = networkAttributes.getBooleanAttribute(cyNetwork.getIdentifier(), 
//                BinarySifVisualStyleUtil.BINARY_NETWORK);
//        if (b != null) {
//            List<JMenuItem> menuList = new ArrayList<JMenuItem>();
//            JMenuItem menuItem = new JMenuItem ("Get Neighbors");
//            menuItem.addActionListener(new ExpandNode(nodeView));
//            menuList.add(menuItem);
//            return menuList;
//        }
        return null;
    }

    @Override
    public Container getQueryBuilderGUI() {
    	return mainPanel;
    }
    
    @Override
    public Set<CyNetwork> getNetworks() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public Object getSearchResult() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public TaskIterator createTaskIterator(Object query) {
		CPathResponseFormat format = CPathResponseFormat.BINARY_SIF;
		CPathNetworkImportTask task = factory.createCPathNetworkImportTask((String) query, webApi, format);
    	return new TaskIterator(task);
    }
    
    /**
     * Creates a new Web Services client.
     * @param factory 
     */
    public CytoscapeCPathWebService(CPath2Factory factory) {
    	super(CPathProperties.getInstance().getCPathUrl(), DISPLAY_NAME, makeDescription());
    	this.factory = factory;
    	
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension (500,400));
        mainPanel.setLayout (new BorderLayout());

        webApi = CPathWebServiceImpl.getInstance();
        cPathSearchPanel cpathPanel = new cPathSearchPanel(webApi, factory);

        TabUi tabbedPane = TabUi.getInstance();
        tabbedPane.add("Search", cpathPanel);

        JScrollPane configPanel = CPathPlugIn2.createConfigPanel();
        tabbedPane.add("Options", configPanel);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private static String makeDescription() {
        String desc = CPathProperties.getInstance().getCPathBlurb();
        desc = desc.replaceAll("<span class='bold'>", "<B>");
        desc = desc.replaceAll("</span>", "</B>");
        return "<html><body>" + desc + "</body></html>";
	}

}

