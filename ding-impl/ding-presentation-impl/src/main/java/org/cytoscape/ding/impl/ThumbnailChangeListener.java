package org.cytoscape.ding.impl;

import java.awt.Image;

@FunctionalInterface
public interface ThumbnailChangeListener {

	void thumbnailChanged(Image image);
}
