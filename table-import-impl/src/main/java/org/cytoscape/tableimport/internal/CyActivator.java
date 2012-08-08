



package org.cytoscape.tableimport.internal;

import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
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
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.util.swing.OpenBrowser;

import org.cytoscape.tableimport.internal.ImportAttributeTableReaderFactory;
import static org.cytoscape.io.DataCategory.*;
import org.cytoscape.tableimport.internal.ImportNetworkTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationAction;
import org.cytoscape.tableimport.internal.tunable.AttributeMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.tunable.NetworkTableMappingParametersHandlerFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.tableimport.internal.reader.ontology.OBONetworkReaderFactory;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import org.cytoscape.service.util.AbstractCyActivator;

import java.io.InputStream;
import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CytoscapeServices.cyLayouts = getService(bc,CyLayoutAlgorithmManager.class);
		CytoscapeServices.cyNetworkFactory = getService(bc,CyNetworkFactory.class);
		CytoscapeServices.cyRootNetworkFactory = getService(bc,CyRootNetworkManager.class);
		CytoscapeServices.cyNetworkViewFactory = getService(bc,CyNetworkViewFactory.class);
		CytoscapeServices.cySwingApplication = getService(bc,CySwingApplication.class);
		CytoscapeServices.cyApplicationManager = getService(bc,CyApplicationManager.class);
		CytoscapeServices.cyNetworkManager = getService(bc,CyNetworkManager.class);
		CytoscapeServices.cyTableManager = getService(bc,CyTableManager.class);
		CytoscapeServices.dialogTaskManager = getService(bc,DialogTaskManager.class);
		CytoscapeServices.bookmark = getService(bc,CyProperty.class,"(cyPropertyName=bookmarks)");
		CytoscapeServices.bookmarksUtil = getService(bc,BookmarksUtil.class);
		CytoscapeServices.cyProperties = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CytoscapeServices.fileUtil = getService(bc,FileUtil.class);
		CytoscapeServices.openBrowser = getService(bc,OpenBrowser.class);
		CytoscapeServices.cyNetworkNaming = getService(bc,CyNetworkNaming.class);
		CytoscapeServices.cyNetworkViewManager = getService(bc,CyNetworkViewManager.class);
		CytoscapeServices.cyTableFactory = getService(bc,CyTableFactory.class);
		CytoscapeServices.streamUtil = getService(bc,StreamUtil.class);
		CytoscapeServices.cyEventHelper = getService(bc,CyEventHelper.class);
		CytoscapeServices.mapGlobalToLocalTableTaskFactory = getService(bc, MapGlobalToLocalTableTaskFactory.class);
		CytoscapeServices.eventAdmin = getService(bc, EventAdmin.class);

		BasicCyFileFilter attrsTableFilter_txt = new BasicCyFileFilter(new String[]{"csv","tsv", "txt", "tab", "net"}, new String[]{"text/csv","text/tab-separated-values"},"Comma or Tab Separated Value Files",TABLE,CytoscapeServices.streamUtil);
		BasicCyFileFilter attrsTableFilter_xls = new BasicCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",TABLE,CytoscapeServices.streamUtil);
		BasicCyFileFilter oboFilter = new BasicCyFileFilter(new String[]{"obo"}, new String[]{"text/obo"},"OBO Files",NETWORK,CytoscapeServices.streamUtil);
		final OBONetworkReaderFactory oboReaderFactory = new OBONetworkReaderFactory(oboFilter);
		CytoscapeServices.inputStreamTaskFactory = oboReaderFactory;
		
		ImportAttributeTableReaderFactory importAttributeTableReaderFactory_txt = new ImportAttributeTableReaderFactory(attrsTableFilter_txt); //,".txt");
		ImportAttributeTableReaderFactory importAttributeTableReaderFactory_xls = new ImportAttributeTableReaderFactory(attrsTableFilter_xls); //,".xls");
		
		// Action to add menu item to the Desktop Menu
		ImportOntologyAndAnnotationAction ontologyAction = new ImportOntologyAndAnnotationAction();
		BasicCyFileFilter networkTableFilter_txt = new BasicCyFileFilter(new String[]{"csv","tsv", "txt"}, new String[]{"text/csv","text/tab-separated-values"},"Comma or Tab Separated Value Files",NETWORK,CytoscapeServices.streamUtil);
		BasicCyFileFilter networkTableFilter_xls = new BasicCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",NETWORK,CytoscapeServices.streamUtil);
		ImportNetworkTableReaderFactory importNetworkTableReaderFactory_txt = new ImportNetworkTableReaderFactory(networkTableFilter_txt);//, "txt");
		ImportNetworkTableReaderFactory importNetworkTableReaderFactory_xls = new ImportNetworkTableReaderFactory(networkTableFilter_xls); //,".xls");
		
		
		Properties importAttributeTableReaderFactory_xlsProps = new Properties();
		importAttributeTableReaderFactory_xlsProps.setProperty("readerDescription","Attribute Table file reader");
		importAttributeTableReaderFactory_xlsProps.setProperty("readerId","attributeTableReader");
		registerService(bc,importAttributeTableReaderFactory_xls,InputStreamTaskFactory.class, importAttributeTableReaderFactory_xlsProps);

		Properties importAttributeTableReaderFactory_txtProps = new Properties();
		importAttributeTableReaderFactory_txtProps.setProperty("readerDescription","Attribute Table file reader");
		importAttributeTableReaderFactory_txtProps.setProperty("readerId","attributeTableReader_txt");
		registerService(bc,importAttributeTableReaderFactory_txt,InputStreamTaskFactory.class, importAttributeTableReaderFactory_txtProps);

		Properties oboReaderFactoryProps = new Properties();
		oboReaderFactoryProps.setProperty("readerDescription","Open Biomedical Ontology (OBO) file reader");
		oboReaderFactoryProps.setProperty("readerId","oboReader");
		registerService(bc,oboReaderFactory,InputStreamTaskFactory.class, oboReaderFactoryProps);
		registerService(bc,ontologyAction,CyAction.class, new Properties());

		Properties importNetworkTableReaderFactory_txtProps = new Properties();
		importNetworkTableReaderFactory_txtProps.setProperty("readerDescription","Network Table file reader");
		importNetworkTableReaderFactory_txtProps.setProperty("readerId","networkTableReader_txt");
		registerService(bc,importNetworkTableReaderFactory_txt,InputStreamTaskFactory.class, importNetworkTableReaderFactory_txtProps);

		Properties importNetworkTableReaderFactory_xlsProps = new Properties();
		importNetworkTableReaderFactory_xlsProps.setProperty("readerDescription","Network Table file reader");
		importNetworkTableReaderFactory_xlsProps.setProperty("readerId","networkTableReader_xls");
		registerService(bc,importNetworkTableReaderFactory_xls,InputStreamTaskFactory.class, importNetworkTableReaderFactory_xlsProps);
		
		int dialogTypeAttribute = ImportTablePanel.SIMPLE_ATTRIBUTE_IMPORT;
		CyTableManager tableManager= CytoscapeServices.cyTableManager;
		AttributeMappingParametersHandlerFactory attributeMappingParametersHandlerFactory = new AttributeMappingParametersHandlerFactory(dialogTypeAttribute, tableManager);
		registerService(bc,attributeMappingParametersHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		
		int dialogTypeNetwork = ImportTablePanel.NETWORK_IMPORT;
		CyTableManager tableManagerNetwork= CytoscapeServices.cyTableManager;
		NetworkTableMappingParametersHandlerFactory networkTableMappingParametersHandlerFactory = new NetworkTableMappingParametersHandlerFactory(dialogTypeNetwork, tableManagerNetwork);
		registerService(bc,networkTableMappingParametersHandlerFactory,GUITunableHandlerFactory.class, new Properties());
	}
}

