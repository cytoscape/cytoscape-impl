package org.cytoscape.tableimport.internal.util;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.util.swing.FileUtil;

public class  CytoscapeServices {
	
	public static CySwingApplication desktop;
	public static Bookmarks theBookmarks;
	public static BookmarksUtil bookmarksUtil;
	public static GUITaskManager guiTaskManagerServiceRef;
	public static CyProperty cytoscapePropertiesServiceRef;
	public static CyTableManager tblMgr;
	public static OpenBrowser openBrowser;
	public static FileUtil fileUtil;
	public static CyApplicationManager appMgr;
	public static CyNetworkManager netMgr;
	public static CyLayoutAlgorithmManager cyLayoutsServiceRef;
	public static CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	public static CyNetworkFactory cyNetworkFactoryServiceRef;
	public static CyNetworkViewManager networkViewManager;
	public static CyNetworkNaming cyNetworkNaming;
	public static CyTableFactory tableFactory;
}
