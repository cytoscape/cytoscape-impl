package org.cytoscape.cg.internal.vector;

import static org.junit.Assert.assertEquals;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.junit.Test;

public class GradientLayerCustomGraphicsTest {

	private static final double DELTA = 1e-9;

	@Test
	public void testOvalLayerIsCentered() {
		var cg = new GradientOvalLayer(1L);
		assertCentered(cg, 40, 30, cg::update);
		// CYTOSCAPE-12717: integer division used to shift odd-sized shapes by half a unit
		assertCentered(cg, 35, 27, cg::update);
	}

	@Test
	public void testRoundRectangleLayerIsCentered() {
		var cg = new GradientRoundRectangleLayer(1L);
		assertCentered(cg, 40, 30, cg::update);
		assertCentered(cg, 35, 27, cg::update);
	}

	@SuppressWarnings("unchecked")
	private static void assertCentered(GradientLayerCustomGraphics cg, int w, int h, Runnable update) {
		cg.setWidth(w);
		cg.setHeight(h);
		update.run();

		var layers = cg.getLayers((CyNetworkView) null, null);
		assertEquals(1, layers.size());

		var bounds = ((CustomGraphicLayer) layers.get(0)).getBounds2D();
		assertEquals(w, bounds.getWidth(), DELTA);
		assertEquals(h, bounds.getHeight(), DELTA);
		assertEquals(0.0, bounds.getCenterX(), DELTA);
		assertEquals(0.0, bounds.getCenterY(), DELTA);
	}
}
