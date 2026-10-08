package org.cytoscape.graph.render.immed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.cytoscape.ding.impl.canvas.NetworkImageBuffer;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.junit.Test;

/**
 * Bitmap custom graphics must be drawn exactly inside the bounds of their (transformed) layers,
 * without any rounding to integer node coordinates (see CYTOSCAPE-12717).
 */
public class GraphGraphicsImageTest {

	private static final int CANVAS_SIZE = 600;
	private static final double ZOOM = 8.0;
	private static final int RED = Color.RED.getRGB();

	@Test
	public void testImageFitsInsideOddSizedNode() {
		assertImageFitsInsideNode(35);
	}

	@Test
	public void testImageFitsInsideEvenSizedNode() {
		assertImageFitsInsideNode(30);
	}

	@Test
	public void testImageWithFractionalBoundsIsNotEnlarged() {
		// Not centered on a node unit boundary
		var bounds = new Rectangle2D.Double(-10.3, -7.6, 20.2, 15.1);
		var canvas = draw(bounds);

		// Expected pixel extent (in image coordinates) of the drawn image
		double minX = CANVAS_SIZE / 2.0 + bounds.getMinX() * ZOOM;
		double maxX = CANVAS_SIZE / 2.0 + bounds.getMaxX() * ZOOM;
		double minY = CANVAS_SIZE / 2.0 + bounds.getMinY() * ZOOM;
		double maxY = CANVAS_SIZE / 2.0 + bounds.getMaxY() * ZOOM;

		for (int y = 0; y < CANVAS_SIZE; y++) {
			for (int x = 0; x < CANVAS_SIZE; x++) {
				// Pixels completely outside the expected bounds must not be painted at all
				if (x + 1 <= minX || x >= maxX || y + 1 <= minY || y >= maxY)
					assertEquals("Pixel (" + x + "," + y + ") should be transparent", 0, alpha(canvas, x, y));
				// Pixels completely inside the expected bounds must be fully painted
				else if (x >= Math.ceil(minX) && x + 1 <= Math.floor(maxX) && y >= Math.ceil(minY) && y + 1 <= Math.floor(maxY))
					assertEquals("Pixel (" + x + "," + y + ") should be red", RED, canvas.getRGB(x, y));
			}
		}
	}

	private void assertImageFitsInsideNode(int nodeSize) {
		// Same as CustomGraphicsInfo.syncSize() + CustomGraphicsPositionCalculator.transform() would do with
		// a 100x80 image inside a square node: fit the width and center the image in the node
		var img = new Rectangle2D.Double(-50.0, -40.0, 100.0, 80.0);
		double scale = Math.min(nodeSize / img.getWidth(), nodeSize / img.getHeight());
		var bounds = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(img).getBounds2D();

		var canvas = draw(bounds);

		int nodeMin = (int) Math.round(CANVAS_SIZE / 2.0 - nodeSize * ZOOM / 2.0);
		int nodeMax = (int) Math.round(CANVAS_SIZE / 2.0 + nodeSize * ZOOM / 2.0); // exclusive
		int cy = CANVAS_SIZE / 2;

		// The image must fill the whole node width...
		for (int x = nodeMin; x < nodeMax; x++)
			assertEquals("Pixel (" + x + "," + cy + ") should be red", RED, canvas.getRGB(x, cy));

		// ...but must not overflow it
		for (int y = 0; y < CANVAS_SIZE; y++) {
			for (int x = 0; x < CANVAS_SIZE; x++) {
				if (x < nodeMin || x >= nodeMax || y < nodeMin || y >= nodeMax)
					assertEquals("Pixel (" + x + "," + y + ") is outside the node", 0, alpha(canvas, x, y));
			}
		}

		// Sanity check: the image is shorter than the node, but vertically centered
		assertEquals(0, alpha(canvas, CANVAS_SIZE / 2, nodeMin));
		assertEquals(0, alpha(canvas, CANVAS_SIZE / 2, nodeMax - 1));
		assertTrue(alpha(canvas, CANVAS_SIZE / 2, cy) > 0);
	}

	private static BufferedImage draw(Rectangle2D layerBounds) {
		var transform = new NetworkTransform(CANVAS_SIZE, CANVAS_SIZE, 0.0, 0.0, ZOOM);
		var buffer = new NetworkImageBuffer(transform);
		var grafx = new GraphGraphics(buffer);

		var layer = new TestImageLayer(layerBounds, createSolidImage(100, 80, Color.RED));
		var nodeShape = new Rectangle2D.Double(-50.0, -50.0, 100.0, 100.0);
		grafx.drawCustomGraphicFull(null, null, nodeShape, layer, 0.0f, 0.0f);

		return buffer.getImage();
	}

	private static BufferedImage createSolidImage(int w, int h, Color color) {
		var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var g = img.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();

		return img;
	}

	private static int alpha(BufferedImage img, int x, int y) {
		return (img.getRGB(x, y) >>> 24) & 0xFF;
	}

	private static class TestImageLayer implements ImageCustomGraphicLayer {

		private final Rectangle2D bounds;
		private final BufferedImage img;

		TestImageLayer(Rectangle2D bounds, BufferedImage img) {
			this.bounds = bounds;
			this.img = img;
		}

		@Override
		public Rectangle2D getBounds2D() {
			return bounds;
		}

		@Override
		public TexturePaint getPaint(Rectangle2D r) {
			return new TexturePaint(img, r);
		}

		@Override
		public CustomGraphicLayer transform(AffineTransform xform) {
			return new TestImageLayer(xform.createTransformedShape(bounds).getBounds2D(), img);
		}
	}
}
