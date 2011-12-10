/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.session.internal;

import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.property.session.Cytopanel;
import org.cytoscape.property.session.Cytopanels;
import org.cytoscape.property.session.Desktop;
import org.cytoscape.property.session.Network;
import org.cytoscape.property.session.NetworkTree;
import org.cytoscape.property.session.ObjectFactory;
import org.cytoscape.property.session.OntologyServer;
import org.cytoscape.property.session.Parent;
import org.cytoscape.property.session.Server;
import org.cytoscape.property.session.SessionState;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class CysessionFactory {

	private static final String DEFAULT_SESSION_NOTE = "You can add note for this session here.";
	
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager nvMgr;
	private final VisualMappingManager vmMgr;
	private final ObjectFactory factory;

	public CysessionFactory(final CyNetworkManager netMgr,
							final CyNetworkViewManager nvMgr,
							final VisualMappingManager vmMgr) {
		this.netMgr = netMgr;
		this.nvMgr = nvMgr;
		this.vmMgr = vmMgr;
		this.factory = new ObjectFactory();
	}

	public Cysession createDefaultCysession() {
		return createCysession(null, null, null);
	}

	/**
	 * Create a Cysession object from the current Cytoscape session attributes.
	 */
	public Cysession createCysession(Desktop desktop, List<Cytopanel> cytopanels, String note) {
		Cysession session = null;

		// Initialize objects for the marshaller:
		session = factory.createCysession();

		if (note == null) note = DEFAULT_SESSION_NOTE;
		session.setSessionNote(note);

		SessionState sState = factory.createSessionState();
		session.setSessionState(sState);

		if (desktop == null) desktop = factory.createDesktop();
		sState.setDesktop(desktop);

		Cytopanels cps = getCytoPanelStates(cytopanels);
		sState.setCytopanels(cps);
		sState.setServer(getServerState());

		NetworkTree netTree = getNetworkTree();
		session.setNetworkTree(netTree);

		return session;
	}

	/**
	 * Check loaded ontologies and save those states in cysession.xml.
	 * @return Server object
	 */
	private Server getServerState() {
		Server server = factory.createServer();
		OntologyServer os = factory.createOntologyServer();

		// We've omitted the concept of Ontology from the core api in 3.0,
		// so that code will exist in the future as a app (although with a public API)
		//		Set<String> ontoNames = Cytoscape.getOntologyServer().getOntologyNames();
		//		Map<String, URL> sources = Cytoscape.getOntologyServer().getOntologySources();
		//
		//		for (String name : ontoNames) {
		//			Ontology onto = factory.createOntology();
		//			onto.setName(name);
		//			onto.setHref(sources.get(name).toString());
		//			os.getOntology().add(onto);
		//		}

		server.setOntologyServer(os);

		return server;
	}

	/**
	 * Extract states of the 3 Cytopanels.
	 *
	 * @return
	 * Note: We will store the states of apps near future. The location of
	 * those states will be stored here.
	 */
	private Cytopanels getCytoPanelStates(List<Cytopanel> cytopanels) {
		Cytopanels cps = factory.createCytopanels();

		if (cytopanels != null) {
			List<Cytopanel> cytoPanelList = cps.getCytopanel();
			for (Cytopanel panel : cytopanels) cytoPanelList.add(panel);
		}

		return cps;
	}

	private NetworkTree getNetworkTree() {
		NetworkTree tree = factory.createNetworkTree();
		Set<CyNetwork> networks = netMgr.getNetworkSet();
		
		if (networks != null) {
			final String DEF_STYLE_NAME = vmMgr.getDefaultVisualStyle().getTitle();
			
			for (CyNetwork cyNet : networks) {
				String name = cyNet.getCyRow(cyNet).get(CyNetwork.NAME, String.class);
				String styleName = DEF_STYLE_NAME;
				
				CyNetworkView cyNetView = nvMgr.getNetworkView(cyNet.getSUID());
				
				if (cyNetView != null) {
					org.cytoscape.view.vizmap.VisualStyle style = vmMgr.getVisualStyle(cyNetView);
					if (style != null) styleName = style.getTitle();
				}

				Network net = new Network();
				net.setId(name);
				net.setFilename(name + ".xgmml");
				net.setVisualStyle(styleName);
				net.setViewAvailable(nvMgr.getNetworkView(cyNet.getSUID()) != null);
				net.setVisualStyle(styleName);

				if (cyNet instanceof CySubNetwork) {
					CySubNetwork cysSubNet = (CySubNetwork) cyNet;
					String parentName = cysSubNet.getRootNetwork().getCyRow(cysSubNet.getRootNetwork()).get(CyNetwork.NAME, String.class);

					Parent parent = new Parent();
					parent.setId(parentName);

					net.setParent(parent);
				}

				// We don't need to save selected nodes/edges anymore, because this info is saved in the CyTables now!
				//				net.setSelectedNodes(selNodes);
				//				net.setSelectedEdges(selEdges);

				tree.getNetwork().add(net);
			}
		}

		return tree;
	}
}
