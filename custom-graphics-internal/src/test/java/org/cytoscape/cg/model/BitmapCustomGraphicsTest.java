package org.cytoscape.cg.model;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.cytoscape.view.model.CyNetworkView;
import org.junit.Test;

public class BitmapCustomGraphicsTest {

	private static final double DELTA = 1e-9;

	@Test
	public void testLayerIsCenteredWithEvenSize() {
		assertLayerIsCentered(40, 30);
	}

	@Test
	public void testLayerIsCenteredWithOddSize() {
		// CYTOSCAPE-12717: integer division used to shift odd-sized images by half a pixel
		assertLayerIsCentered(35, 27);
	}

	@Test
	public void testLayerIsCenteredAfterResize() {
		var cg = new BitmapCustomGraphics(1L, "test", new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB));
		cg.setWidth(35);
		cg.setHeight(29);
		cg.getRenderedImage(); // Rebuilds the layer with the scaled image

		assertCentered(cg, 35, 29);
	}

	private static void assertLayerIsCentered(int w, int h) {
		var cg = new BitmapCustomGraphics(1L, "test", new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
		assertCentered(cg, w, h);
	}

	private static void assertCentered(BitmapCustomGraphics cg, int w, int h) {
		var layers = cg.getLayers((CyNetworkView) null, null);
		assertEquals(1, layers.size());

		var bounds = layers.get(0).getBounds2D();
		assertEquals(w, bounds.getWidth(), DELTA);
		assertEquals(h, bounds.getHeight(), DELTA);
		assertEquals(0.0, bounds.getCenterX(), DELTA);
		assertEquals(0.0, bounds.getCenterY(), DELTA);
		assertEquals(-w / 2.0, bounds.getMinX(), DELTA);
		assertEquals(-h / 2.0, bounds.getMinY(), DELTA);
	}
}
