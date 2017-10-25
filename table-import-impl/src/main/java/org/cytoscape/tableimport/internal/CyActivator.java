package org.cytoscape.tableimport.internal;

import static org.cytoscape.io.DataCategory.NETWORK;
import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;

import java.util.Properties;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.io.WildCardCyFileFilter;
import org.cytoscape.tableimport.internal.reader.ontology.OBONetworkReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportAttributeTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNetworkTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNoGuiNetworkReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNoGuiTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportOntologyAndAnnotationAction;
import org.cytoscape.tableimport.internal.tunable.AttributeMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.tunable.NetworkTableMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {

    @Override
    public void start(BundleContext bc) {
        final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
        final StreamUtil streamUtil = getService(bc, StreamUtil.class);

		{
			// ".xls"
			WildCardCyFileFilter filter = new WildCardCyFileFilter(
					new String[] { "xls", "xlsx" },
					new String[] { "application/excel" },
					"Excel",
					TABLE,
					streamUtil
			);
			ImportAttributeTableReaderFactory factory = new ImportAttributeTableReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Attribute Table file reader");
			props.setProperty("readerId", "attributeTableReader");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			// ".txt"
			WildCardCyFileFilter filter = new WildCardCyFileFilter(
					new String[] { "csv", "tsv", "txt", "tab", "net", "" },
					new String[] { "text/csv", "text/tab-separated-values", "text/plain", "" },
					"Comma or Tab Separated Value",
					TABLE,
					streamUtil
			);
			filter.setBlacklist("xml", "xgmml", "rdf", "owl", "zip", "rar", "jar", "doc", "docx", "ppt", "pptx",
					"pdf", "jpg", "jpeg", "gif", "png", "svg", "tiff", "ttf", "mp3", "mp4", "mpg", "mpeg",
					"exe", "dmg", "iso", "cys");

			ImportAttributeTableReaderFactory factory = new ImportAttributeTableReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Attribute Table file reader");
			props.setProperty("readerId", "attributeTableReader_txt");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			BasicCyFileFilter filter = new BasicCyFileFilter(
					new String[] { "obo" },
					new String[] { "text/obo" },
					"OBO",
					NETWORK,
					streamUtil
			);
			OBONetworkReaderFactory factory = new OBONetworkReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Open Biomedical Ontology (OBO) file reader");
			props.setProperty("readerId", "oboReader");
			registerService(bc, factory, InputStreamTaskFactory.class, props);

			ImportOntologyAndAnnotationAction action = new ImportOntologyAndAnnotationAction(factory, serviceRegistrar);
			registerService(bc, action, CyAction.class);
		}
		{
			// "txt"
			WildCardCyFileFilter filter = new WildCardCyFileFilter(
					new String[] { "csv", "tsv", "txt", "" },
					new String[] { "text/csv", "text/tab-separated-values", "text/plain", "" },
					"Comma or Tab Separated Value", NETWORK,
					streamUtil
			);
			filter.setBlacklist("xml", "xgmml", "rdf", "owl", "zip", "rar", "jar", "doc", "docx", "ppt", "pptx",
					"pdf", "jpg", "jpeg", "gif", "png", "svg", "tiff", "ttf", "mp3", "mp4", "mpg", "mpeg",
					"exe", "dmg", "iso", "cys");

			ImportNetworkTableReaderFactory factory = new ImportNetworkTableReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Network Table file reader");
			props.setProperty("readerId", "networkTableReader_txt");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			// ".xls"
			WildCardCyFileFilter filter = new WildCardCyFileFilter(
					new String[] { "xls", "xlsx" },
					new String[] { "application/excel" },
					"Excel",
					NETWORK,
					streamUtil
			);
			ImportNetworkTableReaderFactory factory = new ImportNetworkTableReaderFactory(filter, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty("readerDescription", "Network Table file reader");
			props.setProperty("readerId", "networkTableReader_xls");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			AttributeMappingParametersHandlerFactory factory =
					new AttributeMappingParametersHandlerFactory(ImportType.TABLE_IMPORT, serviceRegistrar);
			registerService(bc, factory, GUITunableHandlerFactory.class);
		}
		{
			NetworkTableMappingParametersHandlerFactory factory = 
					new NetworkTableMappingParametersHandlerFactory(ImportType.NETWORK_IMPORT, serviceRegistrar);
			registerService(bc, factory, GUITunableHandlerFactory.class);
		}
		{
			TaskFactory factory = new ImportNoGuiTableReaderFactory(false, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "import file");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Import a table from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "This uses a long list of input parameters to specify the attributes of the table, the mapping keys, and the destination table for the input.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// Register the service as a TaskFactory for commands
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			TaskFactory importURLTableFactory = new ImportNoGuiTableReaderFactory(true, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "import url");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Import a table from a URL");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Similar to Import Table this uses a long list of input parameters to specify the attributes of the table, the mapping keys, and the destination table for the input.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// Register the service as a TaskFactory for commands
			registerService(bc, importURLTableFactory, TaskFactory.class, props);
		}
//		{
//			TaskFactory mapColumnTaskFactory = new ImportNoGuiTableReaderFactory(true, serviceRegistrar);
//			Properties props = new Properties();
//			props.setProperty(COMMAND, "map column");
//			props.setProperty(COMMAND_NAMESPACE, "table");
//			props.setProperty(COMMAND_DESCRIPTION, "Map column content from one namespace to another");
//			props.setProperty(COMMAND_LONG_DESCRIPTION, "Uses the BridgeDB service to look up analogous identifiers from a wide selection of other databases");
//			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
//			// Register the service as a TaskFactory for commands
//			registerService(bc, mapColumnTaskFactory, MapColumnTaskFactory.class, props);
//		}
		{
			TaskFactory factory = new ImportNoGuiNetworkReaderFactory(false, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "import file");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Import a network from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
			                  "Import a new network from a tabular formatted file type "+
                        "(e.g. ``csv``, ``tsv``, ``Excel``, etc.).  Use ``network load file`` "+
                        "to load network formatted files.  This command will create a "+
                        "new network collection if no current network collection is selected, otherwise "+
                        "it will add the network to the current collection. The SUIDs of the new networks "+
                        "and views are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, ImportNoGuiNetworkReaderFactory.JSON_EXAMPLE);
			// Register the service as a TaskFactory for commands
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			TaskFactory factory = new ImportNoGuiNetworkReaderFactory(true, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "import url");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Import a network from a URL");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
			                  "Import a new network from a URL that points to a tabular formatted file type "+
                        "(e.g. ``csv``, ``tsv``, ``Excel``, etc.).  Use ``network load url`` "+
                        "to load network formatted files.  This command will create a "+
                        "new network collection if no current network collection is selected, otherwise "+
                        "it will add the network to the current collection. The SUIDs of the new networks "+
                        "and views are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, ImportNoGuiNetworkReaderFactory.JSON_EXAMPLE);
			// Register the service as a TaskFactory for commands
			registerService(bc, factory, TaskFactory.class, props);
		}
    }
}
