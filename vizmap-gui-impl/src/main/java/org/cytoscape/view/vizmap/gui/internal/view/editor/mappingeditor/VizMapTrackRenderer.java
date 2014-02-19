package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.jdesktop.swingx.multislider.TrackRenderer;


/**
 *  Track Renderer for
 *  
 * @param <V>  Visual Property type.
 */
public interface VizMapTrackRenderer extends TrackRenderer {
	/*
	 * Static variables used by the implemented classes.
	 */
	static final Font ICON_FONT = new Font("SansSerif", Font.BOLD, 8);
	static final Font SMALL_FONT = new Font("SansSerif", Font.BOLD, 10);
	static final Font LARGE_FONT = new Font("SansSerif", Font.BOLD, 18);
	static final Color BORDER_COLOR = Color.DARK_GRAY;
	static final BasicStroke STROKE1 = new BasicStroke(1.0f);
	static final BasicStroke STROKE2 = new BasicStroke(2.0f);
}
