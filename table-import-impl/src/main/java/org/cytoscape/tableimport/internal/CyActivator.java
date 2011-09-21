



package org.cytoscape.tableimport.internal;

import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.swing.GUITaskManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.util.swing.OpenBrowser;

import org.cytoscape.tableimport.internal.ImportAttributeTableReaderFactory;
import static org.cytoscape.io.DataCategory.*;
import org.cytoscape.tableimport.internal.ImportNetworkTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationAction;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.tableimport.internal.reader.ontology.OBONetworkReaderFactory;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CySwingApplication cytoscapeDesktopService = getService(bc,CySwingApplication.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		GUITaskManager guiTaskManagerServiceRef = getService(bc,GUITaskManager.class);
		CyProperty bookmarkServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc,BookmarksUtil.class);
		CyProperty cytoscapePropertiesServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		FileUtil fileUtilService = getService(bc,FileUtil.class);
		OpenBrowser openBrowserService = getService(bc,OpenBrowser.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		StreamUtil streamUtilService = getService(bc,StreamUtil.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
		
		BasicCyFileFilter attrsTableFilter_txt = new BasicCyFileFilter(new String[]{"csv","tsv"}, new String[]{"text/plain","text/tab-separated-values"},"Comma or Tab Separated Value Files",TABLE,streamUtilService);
		BasicCyFileFilter attrsTableFilter_xls = new BasicCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",TABLE,streamUtilService);
		BasicCyFileFilter oboFilter = new BasicCyFileFilter(new String[]{"obo"}, new String[]{"text/obo"},"OBO Files",NETWORK,streamUtilService);
		OBONetworkReaderFactory oboReaderFactory = new OBONetworkReaderFactory(oboFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyEventHelperRef);
		ImportAttributeTableReaderFactory importAttributeTableReaderFactory_txt = new ImportAttributeTableReaderFactory(attrsTableFilter_txt,cytoscapeDesktopService,cyApplicationManagerRef,cyNetworkManagerServiceRef,bookmarkServiceRef,bookmarksUtilServiceRef,guiTaskManagerServiceRef,cytoscapePropertiesServiceRef,cyTableManagerServiceRef,fileUtilService,openBrowserService,cyDataTableFactoryServiceRef,".txt");
		ImportAttributeTableReaderFactory importAttributeTableReaderFactory_xls = new ImportAttributeTableReaderFactory(attrsTableFilter_xls,cytoscapeDesktopService,cyApplicationManagerRef,cyNetworkManagerServiceRef,bookmarkServiceRef,bookmarksUtilServiceRef,guiTaskManagerServiceRef,cytoscapePropertiesServiceRef,cyTableManagerServiceRef,fileUtilService,openBrowserService,cyDataTableFactoryServiceRef,".xls");
		ImportOntologyAndAnnotationAction ontologyAction = new ImportOntologyAndAnnotationAction(cyApplicationManagerRef,bookmarkServiceRef,bookmarksUtilServiceRef,guiTaskManagerServiceRef,oboReaderFactory,cyNetworkManagerServiceRef,cyDataTableFactoryServiceRef,cyTableManagerServiceRef);
		BasicCyFileFilter networkTableFilter_txt = new BasicCyFileFilter(new String[]{"csv","tsv"}, new String[]{"text/plain","text/tab-separated-values"},"Comma or Tab Separated Value Files",NETWORK,streamUtilService);
		BasicCyFileFilter networkTableFilter_xls = new BasicCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",NETWORK,streamUtilService);
		ImportNetworkTableReaderFactory importNetworkTableReaderFactory_txt = new ImportNetworkTableReaderFactory(networkTableFilter_txt,cytoscapeDesktopService,cyApplicationManagerRef,cyNetworkManagerServiceRef,guiTaskManagerServiceRef,cytoscapePropertiesServiceRef,fileUtilService,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,".txt",cyNetworkNamingServiceRef,cyNetworkViewManagerServiceRef,cyTableManagerServiceRef);
		ImportNetworkTableReaderFactory importNetworkTableReaderFactory_xls = new ImportNetworkTableReaderFactory(networkTableFilter_xls,cytoscapeDesktopService,cyApplicationManagerRef,cyNetworkManagerServiceRef,guiTaskManagerServiceRef,cytoscapePropertiesServiceRef,fileUtilService,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,".xls",cyNetworkNamingServiceRef,cyNetworkViewManagerServiceRef,cyTableManagerServiceRef);
		
		
		Properties importAttributeTableReaderFactory_xlsProps = new Properties();
		importAttributeTableReaderFactory_xlsProps.setProperty("serviceType","importAttributeTableTaskFactory");
		importAttributeTableReaderFactory_xlsProps.setProperty("readerDescription","Attribute Table file reader");
		importAttributeTableReaderFactory_xlsProps.setProperty("readerId","attributeTableReader");
		registerService(bc,importAttributeTableReaderFactory_xls,InputStreamTaskFactory.class, importAttributeTableReaderFactory_xlsProps);

		Properties importAttributeTableReaderFactory_txtProps = new Properties();
		importAttributeTableReaderFactory_txtProps.setProperty("serviceType","importAttributeTableTaskFactory");
		importAttributeTableReaderFactory_txtProps.setProperty("readerDescription","Attribute Table file reader");
		importAttributeTableReaderFactory_txtProps.setProperty("readerId","attributeTableReader_txt");
		registerService(bc,importAttributeTableReaderFactory_txt,InputStreamTaskFactory.class, importAttributeTableReaderFactory_txtProps);

		Properties oboReaderFactoryProps = new Properties();
		oboReaderFactoryProps.setProperty("serviceType","oboReaderFactory");
		oboReaderFactoryProps.setProperty("readerDescription","Open Biomedical Ontology (OBO) file reader");
		oboReaderFactoryProps.setProperty("readerId","oboReader");
		registerService(bc,oboReaderFactory,InputStreamTaskFactory.class, oboReaderFactoryProps);
		registerService(bc,ontologyAction,CyAction.class, new Properties());

		Properties importNetworkTableReaderFactory_txtProps = new Properties();
		importNetworkTableReaderFactory_txtProps.setProperty("serviceType","importNetworkTableTaskFactory");
		importNetworkTableReaderFactory_txtProps.setProperty("readerDescription","Network Table file reader");
		importNetworkTableReaderFactory_txtProps.setProperty("readerId","networkTableReader_txt");
		registerService(bc,importNetworkTableReaderFactory_txt,InputStreamTaskFactory.class, importNetworkTableReaderFactory_txtProps);

		Properties importNetworkTableReaderFactory_xlsProps = new Properties();
		importNetworkTableReaderFactory_xlsProps.setProperty("serviceType","importNetworkTableTaskFactory");
		importNetworkTableReaderFactory_xlsProps.setProperty("readerDescription","Network Table file reader");
		importNetworkTableReaderFactory_xlsProps.setProperty("readerId","networkTableReader_xls");
		registerService(bc,importNetworkTableReaderFactory_xls,InputStreamTaskFactory.class, importNetworkTableReaderFactory_xlsProps);

	}
}

