package org.cytoscape.util.swing.internal;

import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooser;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
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

class CyColorPaletteChooserFactoryImpl implements CyColorPaletteChooserFactory {
	
	private final PaletteProviderManager paletteManager;
	
	public CyColorPaletteChooserFactoryImpl(PaletteProviderManager paletteManager) {
		this.paletteManager = paletteManager;
	}

	@Override
	public CyColorPaletteChooser getColorPaletteChooser(PaletteType type, boolean paletteOnly) {
		return new CyColorPaletteChooserImpl(paletteManager, type, paletteOnly);
	}
}
