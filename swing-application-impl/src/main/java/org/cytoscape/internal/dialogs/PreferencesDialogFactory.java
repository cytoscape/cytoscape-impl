package org.cytoscape.internal.dialogs;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class PreferencesDialogFactory {

	private PreferencesDialog dialog;
	
	private final CyServiceRegistrar serviceRegistrar;

	public PreferencesDialogFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public PreferencesDialog getPreferencesDialog(Window owner, Map<String, Properties> propMap,
			Map<String, CyProperty<?>> cyPropMap) {
		if (dialog == null) {
			dialog = new PreferencesDialog(owner, propMap, cyPropMap, serviceRegistrar);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dialog = null;
				}
			});
		}
		
		return dialog;
	}
	
	public boolean isDialogVisible() {
		return dialog != null && dialog.isVisible();
	}
}
