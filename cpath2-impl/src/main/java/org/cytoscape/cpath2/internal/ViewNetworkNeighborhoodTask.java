package org.cytoscape.cpath2.internal;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.cytoscape.cpath2.internal.util.BioPaxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class ViewNetworkNeighborhoodTask implements Task {

    private static final String PC_WEB_SERVICE_URL = "/webservice.do?version=3.0&cmd=get_neighbors&q=";
	private final View<CyNode> nodeView;
	private final CyNetworkView networkView;

    public ViewNetworkNeighborhoodTask(View<CyNode> nodeView, CyNetworkView networkView) {
    	this.nodeView = nodeView;
    	this.networkView = networkView;
	}
	
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = networkView.getModel();
        CyRow row = network.getRow(network);
        // grab web services url from network attributes
        String webServicesURL = row.get("biopax.web_services_url", String.class);
        if (webServicesURL == null) {
        	return;
        }
        if (webServicesURL.startsWith("http://")) {
            webServicesURL = webServicesURL.substring(7);
        }

        // grab data sources from network attributes - already encoded
        String dataSources = row.get("biopax.data_sources", String.class);

        // generate menu url
        CyNode cyNode = nodeView.getModel();
        CyRow nodeRow = network.getRow(cyNode);
        String biopaxID = nodeRow.get(BioPaxUtil.BIOPAX_RDF_ID, String.class);
        biopaxID = biopaxID.replace("CPATH-", "");
        String neighborhoodParam = "Neighborhood: " + nodeRow.get(CyNetwork.NAME, String.class);

        // encode some parts of the url
        try {
            neighborhoodParam = URLEncoder.encode(neighborhoodParam, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // if exception occurs leave encoded string, but cmon, utf-8 not supported ??
            // anyway, at least encode spaces, and commas (data sources)
            neighborhoodParam = neighborhoodParam.replaceAll(" ", "%20");
        }

        final String urlString = webServicesURL +
                PC_WEB_SERVICE_URL + biopaxID + "&neighborhood_title=" + neighborhoodParam +
                "&data_source=" + dataSources;
        
        new URL(urlString).getContent();
	}
	
	@Override
	public void cancel() {
	}

}
