package org.cytoscape.tableimport.internal;


import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
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
	private final CyTableManager tableManager;

	/**
	 * Creates a new ImportNetworkTableReaderFactory object.
	 */
	public ImportNetworkTableReaderFactory(final CyFileFilter filter,
	                                       final String fileFormat) {
		super(filter, CytoscapeServices.cyNetworkViewFactory, CytoscapeServices.cyNetworkFactory);

		this.tableManager = CytoscapeServices.cyTableManager;
		this.fileFormat = fileFormat;

	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportNetworkTableReaderTask(this.inputStream, fileFormat,
		                                                         this.inputName, tableManager));
	}

	@Override
	public void setInputStream(InputStream is, String inputName) {
		this.inputStream = is;
		this.inputName = inputName;
	}
}
