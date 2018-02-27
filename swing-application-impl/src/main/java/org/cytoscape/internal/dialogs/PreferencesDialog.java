package org.cytoscape.internal.dialogs;

import java.awt.Dimension;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.internal.prefs.Cy3PreferencesRoot;
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

@SuppressWarnings("serial")
public class PreferencesDialog extends JDialog { //implements ItemListener, ActionListener, ListSelectionListener {
	
	private Map<String, Properties> propMap = new HashMap<>();
	private Map<String, CyProperty<?>> cyPropMap;
	private Map<String, Boolean> itemChangedMap = new HashMap<>();

	private final CyServiceRegistrar serviceRegistrar;
	
	public PreferencesDialog(Window owner, Map<String, Properties> propMap,
			Map<String, CyProperty<?>> cyPropMap, final CyServiceRegistrar serviceRegistrar) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.propMap = propMap;
		this.cyPropMap = cyPropMap;
		this.serviceRegistrar = serviceRegistrar;
		
		for (String key: propMap.keySet())
			itemChangedMap.put(key, false);
		Cy3PreferencesRoot pref = new Cy3PreferencesRoot();
		setContentPane(pref);
		pref.showDlog();
		setTitle("Cytoscape Preferences Editor");
		pack();
		setSize(new Dimension(600, 400));
		// set location relative to owner/parent
		setLocationRelativeTo(owner);
		setResizable(false);
	}
}
