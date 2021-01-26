package org.cytoscape.graph.render.stateful;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LabelInfoCache implements LabelInfoProvider {
	
	private final Cache<Key,LabelInfo> labelCache;
	
	public LabelInfoCache(int maxSize, boolean recordStats) {
		var builder = CacheBuilder.newBuilder().maximumSize(maxSize);
		if(recordStats)
			builder = builder.recordStats();
		this.labelCache = builder.build();
	}

	
	public static class Key {
		private final String text;
		private final Font font;
		private final double labelWidth;
		
		private Key(String text, Font font, double labelWidth) {
			this.text = text;
			this.font = font;
			this.labelWidth = labelWidth;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(font, labelWidth, text);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Key))
				return false;
			Key other = (Key) obj;
			return Objects.equals(font, other.font)
					&& Double.doubleToLongBits(labelWidth) == Double.doubleToLongBits(other.labelWidth)
					&& Objects.equals(text, other.text);
		}
	}

	@Override
	public LabelInfo getLabelInfo(String text, Font font, double labelWidth, FontRenderContext frc) {
		try {
			return getLabelInfoImpl(text, font, labelWidth, frc);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private LabelInfo getLabelInfoImpl(String text, Font font, double labelWidth, FontRenderContext frc) throws ExecutionException {
		// MKTODO add textAsShape parameter
		return labelCache.get(new Key(text, font, labelWidth), () -> new LabelInfo(text, font, frc, false, labelWidth));
	}
	
	@Override
	public String getStats() {
		return String.valueOf(labelCache.stats());
	}
}
