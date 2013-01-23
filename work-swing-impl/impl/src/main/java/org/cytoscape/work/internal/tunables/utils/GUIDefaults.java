package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * This class is for setting an initial default size for the tunable text boxes.
 * @author rozagh
 *
 */
public class GUIDefaults {

	private static final int TEXT_BOX_WIDTH = 150;
	private static final int TEXT_BOX_HEIGHT = 12;
	public static final Dimension TEXT_BOX_DIMENSION = new Dimension(TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
	
	public static final int hGap = 10;
	public static final int vGap = 10;
}
