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

import static org.cytoscape.io.DataCategory.NETWORK;
import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;

import java.util.Properties;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.io.WildCardCyFileFilter;
import org.cytoscape.tableimport.internal.reader.ontology.OBONetworkReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationAction;
import org.cytoscape.tableimport.internal.tunable.AttributeMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.tunable.NetworkTableMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {

    @Override
    public void start(BundleContext bc) {
        final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
        final StreamUtil streamUtil = serviceRegistrar.getService(StreamUtil.class);

		WildCardCyFileFilter attrsTableFilterTXT = new WildCardCyFileFilter(new String[]{"csv","tsv", "txt", "tab", "net", ""}, new String[]{"text/csv","text/tab-separated-values"}, "Comma or Tab Separated Value", TABLE, streamUtil);
		attrsTableFilterTXT.setBlacklist("xml","rdf","owl","cys");
		
		WildCardCyFileFilter attrsTableFilterXLS = new WildCardCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"}, "Excel", TABLE, streamUtil);
        BasicCyFileFilter oboFilter = new BasicCyFileFilter(new String[]{"obo"}, new String[]{"text/obo"}, "OBO", NETWORK, streamUtil);
        
        final OBONetworkReaderFactory oboReaderFactory = new OBONetworkReaderFactory(oboFilter, serviceRegistrar);

        ImportAttributeTableReaderFactory importAttributeTableReaderFactoryTXT = new ImportAttributeTableReaderFactory(attrsTableFilterTXT, serviceRegistrar); // ".txt"
        ImportAttributeTableReaderFactory importAttributeTableReaderFactoryXLS = new ImportAttributeTableReaderFactory(attrsTableFilterXLS, serviceRegistrar); // ".xls"

        // Action to add menu item to the Desktop Menu
        ImportOntologyAndAnnotationAction ontologyAction = new ImportOntologyAndAnnotationAction(oboReaderFactory, serviceRegistrar);
        
        WildCardCyFileFilter networkTableFilterTXT = new WildCardCyFileFilter(new String[]{"csv","tsv", "txt",""}, new String[]{"text/csv","text/tab-separated-values"}, "Comma or Tab Separated Value", NETWORK, streamUtil);
		networkTableFilterTXT.setBlacklist("xml","rdf","owl","cys");
		
		WildCardCyFileFilter networkTableFilterXLS = new WildCardCyFileFilter(new String[]{"xls","xlsx"}, new String[]{"application/excel"}, "Excel", NETWORK, streamUtil);
		
		ImportNetworkTableReaderFactory importNetworkTableReaderFactoryTXT = new ImportNetworkTableReaderFactory(networkTableFilterTXT, serviceRegistrar); // "txt"
        ImportNetworkTableReaderFactory importNetworkTableReaderFactoryXLS = new ImportNetworkTableReaderFactory(networkTableFilterXLS, serviceRegistrar); // ".xls"

        Properties importAttributeTableReaderFactoryXLSProps = new Properties();
        importAttributeTableReaderFactoryXLSProps.setProperty("readerDescription","Attribute Table file reader");
        importAttributeTableReaderFactoryXLSProps.setProperty("readerId","attributeTableReader");
        registerService(bc,importAttributeTableReaderFactoryXLS,InputStreamTaskFactory.class, importAttributeTableReaderFactoryXLSProps);

        Properties importAttributeTableReaderFactoryTXTProps = new Properties();
        importAttributeTableReaderFactoryTXTProps.setProperty("readerDescription","Attribute Table file reader");
        importAttributeTableReaderFactoryTXTProps.setProperty("readerId","attributeTableReader_txt");
        registerService(bc,importAttributeTableReaderFactoryTXT,InputStreamTaskFactory.class, importAttributeTableReaderFactoryTXTProps);

        Properties oboReaderFactoryProps = new Properties();
        oboReaderFactoryProps.setProperty("readerDescription","Open Biomedical Ontology (OBO) file reader");
        oboReaderFactoryProps.setProperty("readerId","oboReader");
        registerService(bc,oboReaderFactory,InputStreamTaskFactory.class, oboReaderFactoryProps);
        registerService(bc,ontologyAction,CyAction.class, new Properties());

        Properties importNetworkTableReaderFactoryTXTProps = new Properties();
        importNetworkTableReaderFactoryTXTProps.setProperty("readerDescription","Network Table file reader");
        importNetworkTableReaderFactoryTXTProps.setProperty("readerId","networkTableReader_txt");
        registerService(bc,importNetworkTableReaderFactoryTXT,InputStreamTaskFactory.class, importNetworkTableReaderFactoryTXTProps);

        Properties importNetworkTableReaderFactoryXLSProps = new Properties();
        importNetworkTableReaderFactoryXLSProps.setProperty("readerDescription","Network Table file reader");
        importNetworkTableReaderFactoryXLSProps.setProperty("readerId","networkTableReader_xls");
        registerService(bc,importNetworkTableReaderFactoryXLS,InputStreamTaskFactory.class, importNetworkTableReaderFactoryXLSProps);

        AttributeMappingParametersHandlerFactory attributeMappingParametersHandlerFactory = new AttributeMappingParametersHandlerFactory(ImportType.TABLE_IMPORT, serviceRegistrar);
        registerService(bc, attributeMappingParametersHandlerFactory, GUITunableHandlerFactory.class, new Properties());

        NetworkTableMappingParametersHandlerFactory networkTableMappingParametersHandlerFactory = new NetworkTableMappingParametersHandlerFactory(ImportType.NETWORK_IMPORT, serviceRegistrar);
        registerService(bc, networkTableMappingParametersHandlerFactory, GUITunableHandlerFactory.class, new Properties());

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
        TaskFactory importFileTableFactory = new ImportNoGuiTableReaderFactory(false, serviceRegistrar);
        // Register the service as a TaskFactory for commands
        registerService(bc,importFileTableFactory, TaskFactory.class, importFileTablesProps);

        Properties importURLTablesProps = new Properties();
        importURLTablesProps.setProperty(COMMAND, "import url");
        importURLTablesProps.setProperty(COMMAND_NAMESPACE, "table");
        importURLTablesProps.setProperty(COMMAND_DESCRIPTION, "Import a table from a URL");
        TaskFactory importURLTableFactory = new ImportNoGuiTableReaderFactory(true, serviceRegistrar);
        // Register the service as a TaskFactory for commands
        registerService(bc,importURLTableFactory, TaskFactory.class, importURLTablesProps);

        Properties importFileNetworksProps = new Properties();
        importFileNetworksProps.setProperty(COMMAND, "import file");
        importFileNetworksProps.setProperty(COMMAND_NAMESPACE, "network");
        importFileNetworksProps.setProperty(COMMAND_DESCRIPTION, "Import a network from a file");
        TaskFactory importFileNetworkFactory = new ImportNoGuiNetworkReaderFactory(false, serviceRegistrar);
        // Register the service as a TaskFactory for commands
        registerService(bc,importFileNetworkFactory, TaskFactory.class, importFileNetworksProps);

        Properties importURLNetworksProps = new Properties();
        importURLNetworksProps.setProperty(COMMAND, "import url");
        importURLNetworksProps.setProperty(COMMAND_NAMESPACE, "network");
        importURLNetworksProps.setProperty(COMMAND_DESCRIPTION, "Import a network from a URL");
        TaskFactory importURLNetworkFactory = new ImportNoGuiNetworkReaderFactory(true, serviceRegistrar);
        // Register the service as a TaskFactory for commands
        registerService(bc,importURLNetworkFactory, TaskFactory.class, importURLNetworksProps);
    }
}
