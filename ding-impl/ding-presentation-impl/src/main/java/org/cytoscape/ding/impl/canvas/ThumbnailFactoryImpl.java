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
		
		// Get configuration properties
		int width  = (int) properties.getOrDefault(WIDTH,  100);
		int height = (int) properties.getOrDefault(HEIGHT, 100);
		boolean fitContent = (boolean) properties.getOrDefault(FIT_CONTENT, true);
		
		// Create snapshot and transform
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		
		NetworkTransform transform = fitContent
			? createTransformFitContent(snapshot, width, height)
			: createTransformFromVPs(snapshot, width, height);

		// Get bg color and level of detail.
		Color bgColor = (Color) networkView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		PrintLOD lod = new PrintLOD(); // hardcode high level of detail

		// Create an image object
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		
		// Paint the image
		CompositeGraphicsCanvas.paintThumbnail(g, bgColor, lod, transform, snapshot);
		
		return image;
	}
	
	
	private static NetworkTransform createTransformFromVPs(CyNetworkViewSnapshot snapshot, int width, int height) {
		NetworkTransform transform = new NetworkTransform(width, height);
		
		Double scaleFactor = snapshot.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		Double centerX = snapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		Double centerY = snapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		
		// This shouldn't happen, just being defensive.
		if(scaleFactor == null)
			scaleFactor = 1.0;
		if(centerX == null)
			centerX = 0.0;
		if(centerY == null)
			centerY = 0.0;
		
		transform.setScaleFactor(scaleFactor);
		transform.setCenter(centerX, centerY);
			
		return transform;
	}
	
	
	private static NetworkTransform createTransformFitContent(CyNetworkViewSnapshot snapshot, int width, int height) {
		NetworkTransform transform = new NetworkTransform(width, height);
		
		double[] extents = new double[4];
		snapshot.getSpacialIndex2D().getMBR(extents); // extents of the network

		double centerX = (extents[0] + extents[2]) / 2.0d;
		double centerY = (extents[1] + extents[3]) / 2.0d;
		double scaleFactor = Math.min(((double) transform.getWidth())  /  (extents[2] - extents[0]), 
                ((double) transform.getHeight()) /  (extents[3] - extents[1])) * 0.98;
		
		transform.setCenter(centerX, centerY);
		transform.setScaleFactor(scaleFactor);
		
		return transform;
	}

}
