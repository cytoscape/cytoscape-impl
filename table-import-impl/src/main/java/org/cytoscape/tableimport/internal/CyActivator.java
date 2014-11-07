package org.cytoscape.tableimport.internal;

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
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.read.*;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.tableimport.internal.io.WildCardCyFileFilter;
import org.cytoscape.tableimport.internal.reader.ontology.OBONetworkReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationAction;
import org.cytoscape.tableimport.internal.tunable.AttributeMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.tunable.NetworkTableMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

import java.util.Properties;

import static org.cytoscape.io.DataCategory.NETWORK;
import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;



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
        
        StreamUtil streamUtilServiceRef = getService(bc, StreamUtil.class);
        ImportDataTableTaskFactory importAttrTFServiceRef = getService(bc,ImportDataTableTaskFactory.class);
        VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
        CyNetworkViewFactory nullNetworkViewFactory = getService(bc, CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)");
        CyNetworkReaderManager networkReaderManagerServiceRef = getService(bc,CyNetworkReaderManager.class);

		WildCardCyFileFilter attrsTableFilter_txt = new WildCardCyFileFilter(new String[]{"csv","tsv", "txt", "tab", "net", ""}, new String[]{"text/csv","text/tab-separated-values"},"Comma or Tab Separated Value Files",TABLE,CytoscapeServices.streamUtil);
		attrsTableFilter_txt.setBlacklist("xml","rdf","owl","cys");
		WildCardCyFileFilter attrsTableFilter_xls = new WildCardCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",TABLE,CytoscapeServices.streamUtil);
        BasicCyFileFilter oboFilter = new BasicCyFileFilter(new String[]{"obo"}, new String[]{"text/obo"},"OBO Files",NETWORK,CytoscapeServices.streamUtil);
        final OBONetworkReaderFactory oboReaderFactory = new OBONetworkReaderFactory(oboFilter);
        CytoscapeServices.inputStreamTaskFactory = oboReaderFactory;

        ImportAttributeTableReaderFactory importAttributeTableReaderFactory_txt = new ImportAttributeTableReaderFactory(attrsTableFilter_txt); //,".txt");
        ImportAttributeTableReaderFactory importAttributeTableReaderFactory_xls = new ImportAttributeTableReaderFactory(attrsTableFilter_xls); //,".xls");

        // Action to add menu item to the Desktop Menu
        ImportOntologyAndAnnotationAction ontologyAction = new ImportOntologyAndAnnotationAction();
        WildCardCyFileFilter networkTableFilter_txt = new WildCardCyFileFilter(new String[]{"csv","tsv", "txt",""}, new String[]{"text/csv","text/tab-separated-values"},"Comma or Tab Separated Value Files",NETWORK,CytoscapeServices.streamUtil);
		networkTableFilter_txt.setBlacklist("xml","rdf","owl","cys");
		WildCardCyFileFilter networkTableFilter_xls = new WildCardCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"},"Excel Files",NETWORK,CytoscapeServices.streamUtil);
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

        //Remove load table from command this option should be available in import command. To be removed if everything works as expected
       /* Properties loadFileTablesProps = new Properties();
        loadFileTablesProps.setProperty(COMMAND, "load file");
        loadFileTablesProps.setProperty(COMMAND_NAMESPACE, "table");
        TaskFactory loadFileTableFactory = new LoadNoGuiTableReaderFactory(streamUtilServiceRef,CytoscapeServices.cyTableManager,false);
        // Register the service as a TaskFactory for commands
        registerService(bc,loadFileTableFactory, TaskFactory.class, loadFileTablesProps);

        Properties loadURLTablesProps = new Properties();
        loadURLTablesProps.setProperty(COMMAND, "load url");
        loadURLTablesProps.setProperty(COMMAND_NAMESPACE, "table");
        TaskFactory loadURLTableFactory = new LoadNoGuiTableReaderFactory(streamUtilServiceRef,CytoscapeServices.cyTableManager,true);
        // Register the service as a TaskFactory for commands
        registerService(bc,loadURLTableFactory, TaskFactory.class, loadURLTablesProps);*/

        Properties importFileTablesProps = new Properties();
        importFileTablesProps.setProperty(COMMAND, "import file");
        importFileTablesProps.setProperty(COMMAND_NAMESPACE, "table");
        importFileTablesProps.setProperty(COMMAND_DESCRIPTION,"Import a table from a file");
        TaskFactory importFileTableFactory = new ImportNoGuiTableReaderFactory(streamUtilServiceRef,importAttrTFServiceRef,false);
        // Register the service as a TaskFactory for commands
        registerService(bc,importFileTableFactory, TaskFactory.class, importFileTablesProps);

        Properties importURLTablesProps = new Properties();
        importURLTablesProps.setProperty(COMMAND, "import url");
        importURLTablesProps.setProperty(COMMAND_NAMESPACE, "table");
        importURLTablesProps.setProperty(COMMAND_DESCRIPTION,"Import a table from a URL");
        TaskFactory importURLTableFactory = new ImportNoGuiTableReaderFactory(streamUtilServiceRef,importAttrTFServiceRef,true);
        // Register the service as a TaskFactory for commands
        registerService(bc,importURLTableFactory, TaskFactory.class, importURLTablesProps);

        Properties importFileNetworksProps = new Properties();
        importFileNetworksProps.setProperty(COMMAND, "import file");
        importFileNetworksProps.setProperty(COMMAND_NAMESPACE, "network");
        importFileNetworksProps.setProperty(COMMAND_DESCRIPTION,"Import a network from a file");
        TaskFactory importFileNetworkFactory = new ImportNoGuiNetworkReaderFactory(streamUtilServiceRef,false,CytoscapeServices.cyNetworkManager,
                CytoscapeServices.cyNetworkViewManager,CytoscapeServices.cyProperties,CytoscapeServices.cyNetworkNaming,
                visualMappingManagerServiceRef,nullNetworkViewFactory,networkReaderManagerServiceRef);
        // Register the service as a TaskFactory for commands
        registerService(bc,importFileNetworkFactory, TaskFactory.class, importFileNetworksProps);

        Properties importURLNetworksProps = new Properties();
        importURLNetworksProps.setProperty(COMMAND, "import url");
        importURLNetworksProps.setProperty(COMMAND_NAMESPACE, "network");
        importURLNetworksProps.setProperty(COMMAND_DESCRIPTION,"Import a network from a URL");
        TaskFactory importURLNetworkFactory = new ImportNoGuiNetworkReaderFactory(streamUtilServiceRef,true,CytoscapeServices.cyNetworkManager,
                CytoscapeServices.cyNetworkViewManager,CytoscapeServices.cyProperties,CytoscapeServices.cyNetworkNaming,
                visualMappingManagerServiceRef,nullNetworkViewFactory, networkReaderManagerServiceRef);
        // Register the service as a TaskFactory for commands
        registerService(bc,importURLNetworkFactory, TaskFactory.class, importURLNetworksProps);
    }
}

