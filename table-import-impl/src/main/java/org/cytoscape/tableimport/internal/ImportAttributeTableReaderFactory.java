package org.cytoscape.tableimport.internal;

import java.io.InputStream;

import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;

public class ImportAttributeTableReaderFactory extends
		AbstractTableReaderFactory {
	private final static long serialVersionUID = 12023139869460898L;

	private final String fileFormat;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportAttributeTableReaderFactory(CyFileFilter filter,
			CySwingApplication desktop, CyApplicationManager appMgr,
			CyNetworkManager netMgr, CyProperty<Bookmarks> bookmarksProp,
			BookmarksUtil bookmarksUtil,
			GUITaskManager guiTaskManagerServiceRef,
			CyProperty<?> cytoscapePropertiesServiceRef, CyTableManager tblMgr,
			FileUtil fileUtilService, OpenBrowser openBrowserService,
			CyTableFactory tableFactory, String fileFormat) {
		super(filter, tableFactory);

		CytoscapeServices.desktop = desktop;
		CytoscapeServices.bookmarksUtil = bookmarksUtil;
		CytoscapeServices.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		CytoscapeServices.guiTaskManagerServiceRef = guiTaskManagerServiceRef;
		CytoscapeServices.tblMgr = tblMgr;
		CytoscapeServices.theBookmarks = bookmarksProp.getProperties();
		CytoscapeServices.openBrowser = openBrowserService;
		CytoscapeServices.fileUtil = fileUtilService;
		CytoscapeServices.appMgr = appMgr;
		CytoscapeServices.netMgr = netMgr;
		CytoscapeServices.tableFactory = tableFactory;
		this.fileFormat = fileFormat;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportAttributeTableReaderTask(
				this.inputStream, fileFormat));
	}

	@Override
	public void setInputStream(InputStream is, String inputName) {
		this.inputStream = is;
	}
}
