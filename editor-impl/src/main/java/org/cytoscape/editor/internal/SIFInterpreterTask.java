
package org.cytoscape.editor.internal;

import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import java.util.ArrayList;
import java.util.List;

public class SIFInterpreterTask extends AbstractNetworkViewTask {
	private CyNetwork network;

	@Tunable(description="Type in a nodes/edges expression in SIF format, e.g. A inhibits B")
	public String sifString;

	public SIFInterpreterTask(CyNetworkView v) {
		super(v);
		network = view.getModel();
	}

	@Override
	public void run(TaskMonitor tm) {
		if ( sifString == null )
			throw new NullPointerException("SIF input string is null");
		
		String[] terms = sifString.split("\\s");
		if (terms != null) {
			if (terms.length > 0) {
				String name1 = terms[0].trim();
				if (!name1.equals(null)) {
					
					CyNode node1 = findNode(terms[0]);
					if (node1 == null) {

						node1 = network.addNode();
						network.getRow(node1).set("name",terms[0]);


						//nv1 = view.getNodeView(node1);

						//double[] nextLocn = new double[2];
						//nextLocn[0] = p.getX();
						//nextLocn[1] = p.getY();
						//view.xformComponentToNodeCoords(nextLocn);
						//nv1.setOffset(nextLocn[0], nextLocn[1]);
					} else {
						//nv1 = view.getNodeView(node1);
					}
					System.out.println("Node 1 = " + node1);
					//System.out.println("NodeView 1 = " + nv1);

				//	double spacing = 3.0 *view.getNodeView(node1).getWidth();

					if (terms.length == 3) // simple case of 'A interaction B'
					{
						CyNode node2 = findNode(terms[2]);
						if (node2 == null) {
							node2 = network.addNode();
							network.getRow(node2).set("name",terms[2]);

							//nv2 = view.getNodeView(node2);

							//nv2.setOffset(nv1.getXPosition() + spacing, nv1
							//		.getYPosition());
						}

						CyEdge edge = network.addEdge(node1, node2, true);
						network.getRow(edge).set("name",terms[1]);

					} else if (terms.length > 3) {
						// process multiple targets and one source
						List<View<CyNode>> nodeViews = new ArrayList<View<CyNode>>();
						String interactionType = terms[1];
						for (int i = 2; i < terms.length; i++)
						{
							CyNode node2 = findNode(terms[i]);
							if (node2 == null) {
								node2 = network.addNode();
								network.getRow(node2).set("name",terms[i]);

								//nv2 = view.getNodeView(node2);

								//nv2.setOffset(nv1.getXPosition() + spacing, nv1
								//		.getYPosition());
							}

							CyEdge edge = network.addEdge(node1, node2, true);
							network.getRow(edge).set("name",terms[1]);
							//doCircleLayout(nodeViews, nv1);
					}
				}
			}
		}
		}
	}

	@Override
	public void cancel() {
	}

	private void doCircleLayout(List<View<CyNode>> nodeViews, View<CyNode> nv1) {

/*
		// // Compute Radius

		int r = (int) Math.max((nodeViews.size() * nv1.getWidth()) / Math.PI, 30);

		// Compute angle step
		double phi = (2 * Math.PI) / nodeViews.size();

		// Arrange vertices in a circle
		for (int i = 0; i < nodeViews.size(); i++) {

			View<CyNode> nv = nodeViews.get(i);
			nv.setOffset(
					nv1.getXPosition() + (int) (r * Math.sin(i * phi)), 
					nv1.getYPosition() + (int) (r * Math.cos(i * phi)));
		}

		// update view
*/
	}

	private CyNode findNode(String name) {
		for ( CyNode node : network.getNodeList() ) 
			if ( network.getRow(node).get("name",String.class).equals(name) ) 
				return node;	
		return null;
	}
}

