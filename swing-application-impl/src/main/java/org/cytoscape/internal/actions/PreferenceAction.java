package org.cytoscape.internal.actions;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.dialogs.PreferencesDialogImpl;
import org.cytoscape.internal.dialogs.PreferencesDialogFactoryImpl;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Properties;
import java.util.Dictionary;
import java.util.HashMap;

/**
 *
 */
public class PreferenceAction extends AbstractCyAction {
	private final static long serialVersionUID = 1202339870248574L;
	/**
	 * Creates a new PreferenceAction object.
	 */
	private CySwingApplication desktop;
	private PreferencesDialogFactoryImpl pdf;
	private BookmarksUtil bkUtil;
	private PreferencesDialogImpl preferencesDialog = null;
	private Map<String, Properties> propMap = new HashMap<String,Properties>();
	private Map<String, Bookmarks> bookmarkMap = new HashMap<String,Bookmarks>();
	private  Map<String, CyProperty> cyPropMap = new HashMap<String, CyProperty>();
	
	public PreferenceAction(CySwingApplication desktop, PreferencesDialogFactoryImpl pdf,
			BookmarksUtil bkUtil) {
		super("Properties...");
		this.desktop = desktop;
		this.pdf = pdf;

		this.bkUtil = bkUtil;
		
		setPreferredMenu("Edit.Preferences");
		setMenuGravity(1.0f);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		preferencesDialog = pdf.getPreferencesDialog(desktop.getJFrame(), propMap, cyPropMap); 
		preferencesDialog.setVisible(true);
	} 
	
	public void addCyProperty(CyProperty<?> p, Dictionary d){
		String propertyName = p.getName();
		Object obj = p.getProperties();
		
		if (obj instanceof Properties){		
			propMap.put(propertyName, (Properties)obj);
			cyPropMap.put(propertyName, p);
		} else if (obj instanceof Bookmarks){
			bookmarkMap.put(propertyName, (Bookmarks)obj);
		} else {
			System.out.println("PreferenceAction: Do not know what kind of properties it is.");
		}
	}
	
	public void removeCyProperty(CyProperty<?> p, Dictionary d){
		String propertyName = p.getName();
		Object obj = p.getProperties();
		
		if (obj instanceof Properties){
			propMap.remove(propertyName);
		} else if (obj instanceof Bookmarks){
			bookmarkMap.remove(propertyName);
		}
	}
}
