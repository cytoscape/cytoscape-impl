package org.cytoscape.tableimport.internal.util;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;

public class CytoscapeServices {

	public static CyLayoutAlgorithmManager cyLayouts;
	public static CyRootNetworkManager cyRootNetworkFactory;
	public static CyNetworkFactory cyNetworkFactory;
	public static CySwingApplication cySwingApplication;
	public static CyApplicationManager cyApplicationManager;
	public static CyNetworkManager cyNetworkManager;
	public static CyTableManager cyTableManager;
	public static DialogTaskManager dialogTaskManager;
	@SuppressWarnings("rawtypes")
	public static CyProperty bookmark;
	public static BookmarksUtil bookmarksUtil;
	@SuppressWarnings("rawtypes")
	public static CyProperty cyProperties;
	public static FileUtil fileUtil;
	public static OpenBrowser openBrowser;
	public static CyNetworkNaming cyNetworkNaming;
	public static CyNetworkViewManager cyNetworkViewManager;
	public static CyNetworkViewFactory cyNetworkViewFactory;
	public static CyTableFactory cyTableFactory;
	public static StreamUtil streamUtil;
	public static CyEventHelper cyEventHelper;
	public static MapGlobalToLocalTableTaskFactory mapGlobalToLocalTableTaskFactory;
	public static InputStreamTaskFactory inputStreamTaskFactory;
}
