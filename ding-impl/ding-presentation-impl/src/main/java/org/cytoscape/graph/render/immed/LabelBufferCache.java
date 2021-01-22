package org.cytoscape.graph.render.immed;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LabelBufferCache {
	
	private static final AffineTransform identity = new AffineTransform();
	
	private final Cache<Key,CachedImage> imageCache;
	private final FontRenderContext fontRenderContextFull;
	private char[] charBuff = new char[20];
	
	
	public LabelBufferCache(FontRenderContext fontRenderContextFull, int maxSize) {
		this.fontRenderContextFull = fontRenderContextFull;
		this.imageCache = CacheBuilder.newBuilder()
				.maximumSize(maxSize)
				.build();
	}
	
	
	private static class CachedImage {
		private final Image buffer;
		private final Rectangle2D glyphBounds;
		
		public CachedImage(Image buffer, Rectangle2D glyphBounds) {
			this.buffer = buffer;
			this.glyphBounds = glyphBounds;
		}
	}
	
	
	private static class Key {
		private final String text;
		private final Font font;
		private final Color color;
		private final double scaleFactor;
		
		public Key(double scaleFactor, String text, Font font, Color color) {
			this.scaleFactor = scaleFactor;
			this.text = text;
			this.font = font;
			this.color = color;
		}

		@Override
		public int hashCode() {
			return Objects.hash(color, font, scaleFactor, text);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Key))
				return false;
			Key other = (Key) obj;
			return Objects.equals(color, other.color) 
					&& Objects.equals(font, other.font)
					&& Double.doubleToLongBits(scaleFactor) == Double.doubleToLongBits(other.scaleFactor)
					&& Objects.equals(text, other.text);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Key [text=");
			builder.append(text);
			builder.append(", font=");
			builder.append(font);
			builder.append(", color=");
			builder.append(color);
			builder.append(", scaleFactor=");
			builder.append(scaleFactor);
			builder.append("]");
			return builder.toString();
		}
	}
	
	
	public void drawText(Graphics2D g, double xCenter, double yCenter, String text, Font font, Color color) {
		try {
			drawTextImpl(g, xCenter, yCenter, text, font, color);
		} catch(ExecutionException e) {
			throw new RuntimeException(e); // MKTODO I think this is safe
		}
	}
	
	
	private void drawTextImpl(Graphics2D g, double xCenter, double yCenter, String text, Font font, Color color) throws ExecutionException {
		AffineTransform currentTransform = g.getTransform();
		
		double scale = currentTransform.getScaleX(); // assume scaleX and scaleY are the same
		Key key = new Key(scale, text, font, color);
		
		CachedImage cachedImage = imageCache.get(key, () -> {
			GlyphVector glyphV = createGlyphVector(text, font);
			Rectangle2D glyphBounds = glyphV.getLogicalBounds();
			
			AffineTransform t = new AffineTransform();
			t.scale(scale, scale);
			t.translate(-glyphBounds.getWidth()/2, -glyphBounds.getHeight()/2);
			
			Rectangle2D pixelBounds = t.createTransformedShape(glyphBounds).getBounds2D();
			int w = (int) Math.ceil(pixelBounds.getWidth());
			int h = (int) Math.ceil(pixelBounds.getHeight());
			// assume newly created buffer is initialized to be transparent
			BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D gBuff = buffer.createGraphics();
			GraphGraphics.setGraphicsFlags(gBuff);
			
			double scalex = w / glyphBounds.getWidth();
			double scaley = h / glyphBounds.getHeight();
			gBuff.scale(scalex, scaley);
			gBuff.translate(-glyphBounds.getX(), -glyphBounds.getY());
			gBuff.setPaint(color);
			gBuff.fill(glyphV.getOutline());
			
			return new CachedImage(buffer, glyphBounds);
		});
		
		// Get the components of the current transformation matrix, without creating any new objects.
		double tx = currentTransform.getTranslateX();
		double ty = currentTransform.getTranslateY();
		
		// Translate to the center of the buffer area
		double gtx = -cachedImage.glyphBounds.getWidth()  / 2;
		double gty = -cachedImage.glyphBounds.getHeight() / 2;
		tx = gtx * scale + tx;
		ty = gty * scale + ty;
         
		// Translate to the pixel location where the label should be drawn
		double x = xCenter * scale + tx;
		double y = yCenter * scale + ty;
		
		g.setTransform(identity);
		g.drawImage(cachedImage.buffer, (int)x, (int)y, null);		
		g.setTransform(currentTransform);
	}
	
	
	private GlyphVector createGlyphVector(String text, Font font) {
		if(text.length() > charBuff.length) {
			charBuff = new char[Math.max(charBuff.length * 2, text.length())];
		}
		text.getChars(0, text.length(), charBuff, 0);
		return font.layoutGlyphVector(fontRenderContextFull, charBuff, 0, text.length(), Font.LAYOUT_NO_LIMIT_CONTEXT);
	}

}
