package org.cytoscape.tableimport.internal;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.work.TaskFactory;

public class ImportNetworkTableReaderFactory extends AbstractNetworkReaderFactory {
	private final static long serialVersionUID = 12023139869460154L;

	private final String fileFormat;

	/**
	 * Creates a new ImportNetworkTableReaderFactory object.
	 */
	public ImportNetworkTableReaderFactory(CyFileFilter filter,
			CySwingApplication desktop, CyApplicationManager appMgr,
			CyNetworkManager netMgr, 
			GUITaskManager guiTaskManagerServiceRef,
			CyProperty<?> cytoscapePropertiesServiceRef,
			FileUtil fileUtilService, 
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory, String fileFormat,
			CyNetworkNaming cyNetworkNaming, CyNetworkViewManager networkViewManager) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);

		    
	    CytoscapeServices.networkViewManager = networkViewManager;
		CytoscapeServices.cyNetworkNaming = cyNetworkNaming;
		CytoscapeServices.cyNetworkViewFactoryServiceRef = cyNetworkViewFactory;
		CytoscapeServices.cyNetworkFactoryServiceRef = cyNetworkFactory;
	      
		CytoscapeServices.desktop = desktop;
		CytoscapeServices.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		CytoscapeServices.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
		CytoscapeServices.fileUtil = fileUtilService;
		CytoscapeServices.appMgr = appMgr;
		CytoscapeServices.netMgr = netMgr;
				
		this.fileFormat = fileFormat;

	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportNetworkTableReaderTask(this.inputStream, fileFormat, this.inputName));
	}

	@Override
	public void setInputStream(InputStream is, String inputName) {
		this.inputStream = is;
		this.inputName = inputName;
	}
}
