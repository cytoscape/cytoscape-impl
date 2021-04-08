package org.cytoscape.tableimport.internal;

import static org.cytoscape.io.DataCategory.NETWORK;
import static org.cytoscape.io.DataCategory.TABLE;
import static org.cytoscape.tableimport.internal.util.IconUtil.COLORS_3;
import static org.cytoscape.tableimport.internal.util.IconUtil.LAYERED_IMPORT_TABLE;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.INSERT_TOOLBAR_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOLTIP_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.awt.Font;
import java.util.Properties;

import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.io.WildCardCyFileFilter;
import org.cytoscape.tableimport.internal.task.ImportAttributeTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNetworkTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNoGuiNetworkReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportNoGuiTableReaderFactory;
import org.cytoscape.tableimport.internal.task.ImportTableDataTaskFactoryImpl;
import org.cytoscape.tableimport.internal.task.LoadTableFileTaskFactoryImpl;
import org.cytoscape.tableimport.internal.task.LoadTableURLTaskFactoryImpl;
import org.cytoscape.tableimport.internal.task.TableImportContext;
import org.cytoscape.tableimport.internal.tunable.AttributeMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.tunable.NetworkTableMappingParametersHandlerFactory;
import org.cytoscape.tableimport.internal.util.IconUtil;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.LoadTableURLTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

	private static float LARGE_ICON_FONT_SIZE = 32f;
	private static int LARGE_ICON_SIZE = 32;
	
	private Font iconFont;
	
    @Override
    public void start(BundleContext bc) {
        var serviceRegistrar = getService(bc, CyServiceRegistrar.class);
        var streamUtil = getService(bc, StreamUtil.class);
        var iconManager = getService(bc, IconManager.class);

        iconFont = iconManager.getIconFont("cytoscape-3", LARGE_ICON_FONT_SIZE);
        
        var tableImportContext = new TableImportContext();
        
		{
			// ".xls"
			var filter = new WildCardCyFileFilter(
					new String[] { "xls", "xlsx" },
					new String[] { "application/excel" },
					"Excel",
					TABLE,
					streamUtil
			);
			var factory = new ImportAttributeTableReaderFactory(filter, tableImportContext, serviceRegistrar);
			var props = new Properties();
			props.setProperty("readerDescription", "Attribute Table file reader");
			props.setProperty("readerId", "attributeTableReader");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			// ".txt"
			var filter = new WildCardCyFileFilter(
					new String[] { "csv", "tsv", "txt", "tab", "net", "" },
					new String[] { "text/csv", "text/tab-separated-values", "text/plain", "" },
					"Comma or Tab Separated Value",
					TABLE,
					streamUtil
			);
			filter.setBlacklist("xml", "xgmml", "rdf", "owl", "zip", "rar", "jar", "doc", "docx", "ppt", "pptx",
					"pdf", "jpg", "jpeg", "gif", "png", "svg", "tiff", "ttf", "mp3", "mp4", "mpg", "mpeg",
					"exe", "dmg", "iso", "cys");

			var factory = new ImportAttributeTableReaderFactory(filter, tableImportContext, serviceRegistrar);
			var props = new Properties();
			props.setProperty("readerDescription", "Attribute Table file reader");
			props.setProperty("readerId", "attributeTableReader_txt");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
//			var filter = new BasicCyFileFilter(
//					new String[] { "obo" },
//					new String[] { "text/obo" },
//					"OBO",
//					NETWORK,
//					streamUtil
//			);
//			var factory = new OBONetworkReaderFactory(filter, serviceRegistrar);
//			var props = new Properties();
//			props.setProperty("readerDescription", "Open Biomedical Ontology (OBO) file reader");
//			props.setProperty("readerId", "oboReader");
//			registerService(bc, factory, InputStreamTaskFactory.class, props);
//
//			var action = new ImportOntologyAndAnnotationAction(factory, serviceRegistrar);
//			registerService(bc, action, CyAction.class);
		}
		{
			// "txt"
			var filter = new WildCardCyFileFilter(
					new String[] { "csv", "tsv", "txt", "" },
					new String[] { "text/csv", "text/tab-separated-values", "text/plain", "" },
					"Comma or Tab Separated Value", NETWORK,
					streamUtil
			);
			filter.setBlacklist("xml", "xgmml", "rdf", "owl", "zip", "rar", "jar", "doc", "docx", "ppt", "pptx",
					"pdf", "jpg", "jpeg", "gif", "png", "svg", "tiff", "ttf", "mp3", "mp4", "mpg", "mpeg",
					"exe", "dmg", "iso", "cys");

			var factory = new ImportNetworkTableReaderFactory(filter, serviceRegistrar);
			var props = new Properties();
			props.setProperty("readerDescription", "Network Table file reader");
			props.setProperty("readerId", "networkTableReader_txt");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			// ".xls"
			var filter = new WildCardCyFileFilter(
					new String[] { "xls", "xlsx" },
					new String[] { "application/excel" },
					"Excel",
					NETWORK,
					streamUtil
			);
			var factory = new ImportNetworkTableReaderFactory(filter, serviceRegistrar);
			var props = new Properties();
			props.setProperty("readerDescription", "Network Table file reader");
			props.setProperty("readerId", "networkTableReader_xls");
			registerService(bc, factory, InputStreamTaskFactory.class, props);
		}
		{
			var factory = new AttributeMappingParametersHandlerFactory(ImportType.TABLE_IMPORT, tableImportContext, serviceRegistrar);
			registerService(bc, factory, GUITunableHandlerFactory.class);
		}
		{
			var factory = new NetworkTableMappingParametersHandlerFactory(ImportType.NETWORK_IMPORT, tableImportContext, serviceRegistrar);
			registerService(bc, factory, GUITunableHandlerFactory.class);
		}
		{
			var factory = new ImportNoGuiTableReaderFactory(false, tableImportContext, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "import file");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Import a table from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "This uses a long list of input parameters to specify the attributes of the table, the mapping keys, and the destination table for the input.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"mappedTables\":[101,102]}");
			// Register the service as a TaskFactory for commands
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var importURLTableFactory = new ImportNoGuiTableReaderFactory(true, tableImportContext, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "import url");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Import a table from a URL");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Similar to Import Table this uses a long list of input parameters to specify the attributes of the table, the mapping keys, and the destination table for the input.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"mappedTables\":[101,102]}");
			// Register the service as a TaskFactory for commands
			registerService(bc, importURLTableFactory, TaskFactory.class, props);
		}
//		{
//			var mapColumnTaskFactory = new ImportNoGuiTableReaderFactory(true, serviceRegistrar);
//			var props = new Properties();
//			props.setProperty(COMMAND, "map column");
//			props.setProperty(COMMAND_NAMESPACE, "table");
//			props.setProperty(COMMAND_DESCRIPTION, "Map column content from one namespace to another");
//			props.setProperty(COMMAND_LONG_DESCRIPTION, "Uses the BridgeDB service to look up analogous identifiers from a wide selection of other databases");
//			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
//			// Register the service as a TaskFactory for commands
//			registerService(bc, mapColumnTaskFactory, MapColumnTaskFactory.class, props);
//		}
		{
			var factory = new ImportNoGuiNetworkReaderFactory(false, tableImportContext, serviceRegistrar);
			var props = new Properties();
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
			var factory = new ImportNoGuiNetworkReaderFactory(true, tableImportContext, serviceRegistrar);
			var props = new Properties();
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
		{
			// Import -- Main Menu, Main Toolbar and Command
			var factory = new LoadTableFileTaskFactoryImpl(tableImportContext, serviceRegistrar);
			
			var icon = new TextIcon(LAYERED_IMPORT_TABLE, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::IMPORT_FILE_TABLE";
			iconManager.addIcon(iconId, icon);
			
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Import[23.0]");
			props.setProperty(TITLE, "Table from File...");
			props.setProperty(MENU_GRAVITY, "5.1");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOL_BAR_GRAVITY, "2.1");
			props.setProperty(TOOLTIP, "Import Table from File");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Reads a table from the file system and adds it to the current session.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Reads a table from the file system.  Requires a string containing the absolute path of the file. Returns the SUID of the table created.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"mappedTables\": [101,102]}");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadTableFileTaskFactory.class, props);
			registerService(bc, factory, CytoPanelComponentSelectedListener.class);
		}
		{
			// Import -- Table Toolbar
			var factory = new LoadTableFileTaskFactoryImpl(tableImportContext, serviceRegistrar);
			
			var icon = new TextIcon(IconUtil.FILE_IMPORT, iconFont.deriveFont(22.0f), 32, 31);
			var iconId = "cy::IMPORT_TABLE";
			iconManager.addIcon(iconId, icon);
			
			var props = new Properties();
			props.setProperty(IN_TABLE_TOOL_BAR, "true");
			props.setProperty(TOOL_BAR_GRAVITY, "0.006");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TOOLTIP, "Import Table from File...");
			props.setProperty(INSERT_TOOLBAR_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new LoadTableURLTaskFactoryImpl(tableImportContext, serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Import[23.0]");
			props.setProperty(MENU_GRAVITY, "6.0");
			props.setProperty(TITLE, "Table from URL...");
			props.setProperty(TOOLTIP, "Import Table From URL");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Reads a table from the Internet.  Requires a valid URL pointing to the file. Returns the SUID of the table created.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"mappedTables\": [101,102]}");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadTableURLTaskFactory.class, props);
		}
		{
			var factory = new ImportTableDataTaskFactoryImpl(tableImportContext, serviceRegistrar);
			registerService(bc, factory, ImportDataTableTaskFactory.class);
		}
    }
}
