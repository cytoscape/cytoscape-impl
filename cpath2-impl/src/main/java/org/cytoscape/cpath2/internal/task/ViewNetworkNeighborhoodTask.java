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
        String biopaxID = nodeRow.get(BioPaxUtil.BIOPAX_URI, String.class);
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
