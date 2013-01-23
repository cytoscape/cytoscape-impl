package org.cytoscape.internal.dialogs;

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


import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;

/**
 */
public class PreferencesDialogFactoryImpl {

	private CyEventHelper eh;

	public PreferencesDialogFactoryImpl(CyEventHelper eh) {
		this.eh = eh;
	}

	public PreferencesDialogImpl getPreferencesDialog(Frame parent, Map<String, Properties> propMap,  Map<String, CyProperty> cyPropMap) {
		return new PreferencesDialogImpl(parent,eh, propMap, cyPropMap);	
	}
	
	
}
