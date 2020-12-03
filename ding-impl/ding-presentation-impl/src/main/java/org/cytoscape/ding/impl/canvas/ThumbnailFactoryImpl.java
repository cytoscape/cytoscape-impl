package org.cytoscape.ding.impl.canvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.presentation.ThumbnailFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class ThumbnailFactoryImpl implements ThumbnailFactory {

	@Override
	public Image getThumbnail(CyNetworkView networkView, Map<String,Object> properties) {
		String rendererId = networkView.getRendererId();
		if(!rendererId.equals(DingRenderer.ID)) {
			throw new IllegalArgumentException("networkView was not created by the ding network view factory, got: " + rendererId);
		}
		
		int width  = (int) properties.getOrDefault(WIDTH,  100);
		int height = (int) properties.getOrDefault(HEIGHT, 100);
		boolean fitContent = (boolean) properties.getOrDefault(FIT_CONTENT, true);
		
		NetworkTransform transform = new NetworkTransform(width, height);
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		Color bgColor = (Color) networkView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		PrintLOD lod = new PrintLOD();
		
		if(fitContent) {
			fitContent(snapshot, transform);
		}
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		
		CompositeGraphicsCanvas.paintThumbnail(g, bgColor, lod, transform, snapshot);
		
		return image;
	}
	
	
	private void fitContent(CyNetworkViewSnapshot snapshot, NetworkTransform transform) {
		double[] extents = new double[4];
		snapshot.getSpacialIndex2D().getMBR(extents); // extents of the network

		double centerX = (extents[0] + extents[2]) / 2.0d;
		double centerY = (extents[1] + extents[3]) / 2.0d;
		double scaleFactor = Math.min(((double) transform.getWidth())  /  (extents[2] - extents[0]), 
                ((double) transform.getHeight()) /  (extents[3] - extents[1])) * 0.98;
		
		transform.setCenter(centerX, centerY);
		transform.setScaleFactor(scaleFactor);
	}

}
