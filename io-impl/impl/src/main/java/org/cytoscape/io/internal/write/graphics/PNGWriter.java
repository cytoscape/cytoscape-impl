package org.cytoscape.io.internal.write.graphics;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class PNGWriter extends BitmapWriter {

	@Tunable(
			description = "Transparent Background:",
			longDescription = "If true and the image format supports it, the background will be rendered transparent.",
			exampleStringValue = "true",
			groups = { "_Others" },
			gravity = 2.1
	)
	public boolean transparentBackground;
	
	
	public PNGWriter(RenderingEngine<?> re, OutputStream outStream, Set<String> extensions) {
		super(re, outStream, extensions);
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		re.getProperties().setProperty("exportTransparentBackground", String.valueOf(transparentBackground));
		try {
			super.run(tm);
		} finally {
			re.getProperties().remove("exportTransparentBackground");
		}
	}
	
	@Override
	protected int getImageType() {
		return BufferedImage.TYPE_INT_ARGB;
	}
}
