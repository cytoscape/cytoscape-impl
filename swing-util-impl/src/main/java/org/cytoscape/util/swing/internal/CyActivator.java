package org.cytoscape.util.swing.internal;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.osgi.framework.BundleContext;

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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final PaletteProviderManager paletteProviderManager = getService(bc, PaletteProviderManager.class);

		final OpenBrowserImpl openBrowser = new OpenBrowserImpl(serviceRegistrar);
		registerService(bc, openBrowser, OpenBrowser.class, new Properties());

		final FileUtilImpl fileUtil = new FileUtilImpl(serviceRegistrar);
		registerService(bc, fileUtil, FileUtil.class, new Properties());

		final IconManagerImpl iconManager;
		try {
			iconManager = new IconManagerImpl();
			registerService(bc, iconManager, IconManager.class, new Properties());
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

		final CyColorPaletteChooserFactory paletteChooser = new CyColorPaletteChooserFactoryImpl(paletteProviderManager);
		registerService(bc, paletteChooser, CyColorPaletteChooserFactory.class, new Properties());
		
		// Register the font used for custom Cytoscape icons
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/cytoscape-3.ttf"));
			iconManager.addIconFont(font);
		} catch (FontFormatException|IOException e) {
			throw new RuntimeException("Cannot Load Cytoscape font", e);
		}
	}
}
