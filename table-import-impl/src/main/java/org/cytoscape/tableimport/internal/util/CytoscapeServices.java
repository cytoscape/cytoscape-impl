package org.cytoscape.tableimport.internal.util;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.io.util.StreamUtil;

public class  CytoscapeServices {

	public static CyLayoutAlgorithmManager cyLayouts;
	public static CyRootNetworkManager cyRootNetworkFactory;
	public static CyNetworkFactory cyNetworkFactory;
	public static CySwingApplication cySwingApplication;
	public static CyApplicationManager cyApplicationManager;
	public static CyNetworkManager cyNetworkManager;
	public static CyTableManager cyTableManager;
	public static DialogTaskManager dialogTaskManager;
	public static CyProperty bookmark;
	public static BookmarksUtil bookmarksUtil;
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
}
