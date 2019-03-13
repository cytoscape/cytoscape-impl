package org.cytoscape.view.model.internal.debug;

import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

@SuppressWarnings("serial")
public class PrintSpacialIndexAction extends AbstractCyAction {

	private final CyServiceRegistrar registrar;
	
	public PrintSpacialIndexAction(CyServiceRegistrar registrar) {
		super("Print Spacial Index");
		this.registrar = registrar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CyApplicationManager applicationManager = registrar.getService(CyApplicationManager.class);
		
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		VisualLexicon lexicon = applicationManager.getCurrentRenderingEngine().getVisualLexicon();
		if(networkView == null || lexicon == null) {
			System.out.println("no current network");
			return;
		}
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		if(snapshot == null) {
			System.out.println("snapshot is null");
			return;
		}
		
		SpacialIndex2D<Long> spacialIndex2D = snapshot.getSpacialIndex2D();
		SpacialIndex2DEnumerator<Long> enumerator = spacialIndex2D.queryAll();
		
		System.out.println("Node Extents");
		float[] extents = new float[4];
		while(enumerator.hasNext()) {
			long nodeSuid = enumerator.nextExtents(extents);
			System.out.println("Node: " + nodeSuid + " " + getCanvasCoords(extents));
		}
	}
	
	
	private static String getCanvasCoords(float[] extents) {
		float w = extents[SpacialIndex2D.X_MAX] - extents[SpacialIndex2D.X_MIN];
		float h = extents[SpacialIndex2D.Y_MAX] - extents[SpacialIndex2D.Y_MIN];
		float x = extents[SpacialIndex2D.X_MIN] + (w / 2f);
		float y = extents[SpacialIndex2D.Y_MIN] + (h / 2f);
		return String.format("x:%.1f y:%.1f w:%.1f h:%.1f", x, y, w, h);
	}
}
