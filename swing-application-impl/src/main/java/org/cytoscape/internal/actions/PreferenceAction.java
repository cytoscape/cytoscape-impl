/*
  File: PreferenceAction.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

//-------------------------------------------------------------------------
// $Revision: 12984 $
// $Date: 2008-02-08 13:12:37 -0800 (Fri, 08 Feb 2008) $
// $Author: mes $
//-------------------------------------------------------------------------
package org.cytoscape.internal.actions;

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
		setMenuGravity(10.0f);
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
			System.out.println("PreferenceAction: Do not know what kind of properties it is!");
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
