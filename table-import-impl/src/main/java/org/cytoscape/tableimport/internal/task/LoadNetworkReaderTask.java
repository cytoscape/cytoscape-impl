package org.cytoscape.tableimport.internal.task;

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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.ExcelNetworkSheetReader;
import org.cytoscape.tableimport.internal.reader.GraphReader;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;


public class LoadNetworkReaderTask extends AbstractTask implements CyNetworkReader, TunableValidator {
	
	private InputStream is;
	private String fileType;
	private CyNetwork[] networks;
	private String inputName;
	private GraphReader reader;
	private CyNetworkReader netReader;
	private CyServiceRegistrar serviceRegistrar;
	private PreviewTablePanel previewPanel;
	private String networkName;
	private URI uri;
	private File tempFile;
	private TaskMonitor taskMonitor;
	
	private static final String DEF_INTERACTION = "pp";
	
	@Tunable(description="Text Delimiters:", context="both")
	public ListMultipleSelection<String> delimiters;
	
	@Tunable(description="Text Delimiters for data list type:", context="both")
	public ListSingleSelection<String> delimitersForDataList;
	
	@Tunable(description="Start Load Row:", context="both")
	public int startLoadRow = -1;
	
	@Tunable(description="First row used for column names:", context="both")
	public boolean firstRowAsColumnNames = false;
	
	@Tunable(description="Column for source interaction:", context="both")
	public int indexColumnSourceInteraction = -1;
	
	@Tunable(description="Column for target interaction:", context="both")
	public int indexColumnTargetInteraction = -1;
	
	@Tunable(description="Column for interaction type:", context="both")
	public int indexColumnTypeInteraction = -1;
	
	@Tunable(description="Default interaction type:", context="both")
	public String defaultInteraction = DEF_INTERACTION;
	
	private NetworkTableMappingParameters ntmp;

	public LoadNetworkReaderTask(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		List<String> tempList = new ArrayList<>();
		tempList.add(TextDelimiter.COMMA.getDelimiter());
		tempList.add(TextDelimiter.SEMICOLON.getDelimiter());
		tempList.add(TextDelimiter.SPACE.getDelimiter());
		tempList.add(TextDelimiter.TAB.getDelimiter());
		delimiters = new ListMultipleSelection<>(tempList);
	    
		tempList = new ArrayList<>();
		tempList.add(TextDelimiter.PIPE.getDelimiter());
		tempList.add(TextDelimiter.BACKSLASH.getDelimiter());
		tempList.add(TextDelimiter.SLASH.getDelimiter());
		tempList.add(TextDelimiter.COMMA.getDelimiter());
		delimitersForDataList = new ListSingleSelection<>(tempList);
	}
	
	public void setInputFile(final InputStream is, final String fileType,final String inputName, final URI uriName,
			final IconManager iconManager) {
		this.is = is;
		this.fileType = fileType;
		this.inputName = inputName;
		this.uri = uriName;
		
		previewPanel = new PreviewTablePanel(iconManager);

		try{
			tempFile = File.createTempFile("temp", this.fileType);
			tempFile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(tempFile);
			int read = 0;
			byte[] bytes = new byte[1024];
		 
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
			os.flush();
			os.close();
			
			
			this.is = new FileInputStream(tempFile);
		} catch(Exception e){
			this.is = null;
			e.printStackTrace();
		}
		
		List<String> tempList = new ArrayList<>();
		tempList = new ArrayList<>();
		tempList.add(TextDelimiter.TAB.getDelimiter());
		tempList.add(TextDelimiter.SPACE.getDelimiter());
		tempList.add(TextDelimiter.COMMA.getDelimiter());
		delimiters.setSelectedValues(tempList);
		delimitersForDataList.setSelectedValue(TextDelimiter.PIPE.getDelimiter());
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		tm.setTitle("Loading network from table");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading network...");
		taskMonitor = tm;
		
		final List<String> attrNameList = new ArrayList<>();
		int colCount;
		String[] attributeNames;
		
		final CyNetworkReaderManager networkReaderManager = serviceRegistrar.getService(CyNetworkReaderManager.class);
		
		if (is != null)
			netReader = networkReaderManager.getReader(is, inputName);
		
		if (netReader == null)				
			netReader = networkReaderManager.getReader(uri, inputName);
		
		if (netReader instanceof CombineReaderAndMappingTask) {
			Workbook workbook = null;
			
			// Load Spreadsheet data for preview.
			if (fileType != null && (fileType.equalsIgnoreCase(
					SupportedFileType.EXCEL.getExtension())
					|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) && workbook == null) {
				try {
					workbook = WorkbookFactory.create(new FileInputStream(tempFile));
				} catch (InvalidFormatException e) {
					//e.printStackTrace();
					throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?" , e);
				} finally {
					
				}
			}
			
			netReader = null;
			
			if (startLoadRow > 0)
				startLoadRow--;
			
			final int startLoadRowTemp = firstRowAsColumnNames ? 0 : startLoadRow;
			
			previewPanel.updatePreviewTable(
					workbook,
					fileType,
					tempFile.getAbsolutePath(),
					new FileInputStream(tempFile),
					delimiters.getSelectedValues(),
					null,
					startLoadRowTemp
			);
			
			colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
			Object curName = null;
			
			if (firstRowAsColumnNames) {
				previewPanel.setFirstRowAsColumnNames();
				startLoadRow++;
			}
	
			final SourceColumnSemantic[] types = previewPanel.getTypes();
			
			for (int i = 0; i < colCount; i++) {
				curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();
				
				if (attrNameList.contains(curName)) {
					int dupIndex = 0;
	
					for (int idx = 0; idx < attrNameList.size(); idx++) {
						if (curName.equals(attrNameList.get(idx))) {
							dupIndex = idx;
	
							break;
						}
					}
	
					if (!TypeUtil.allowsDuplicateName(ImportType.NETWORK_IMPORT, types[i], types[dupIndex])) {
						// TODO add message to user (Duplicate Column Name Found)
						return;
					}
				}
	
				if (curName == null)
					attrNameList.add("Column " + i);
				else
					attrNameList.add(curName.toString());
			}
			
			attributeNames = attrNameList.toArray(new String[0]);
			
			final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);
			
			final AttributeDataType[] dataTypes = previewPanel.getDataTypes();
			final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
			
			String[] listDelimiters = previewPanel.getListDelimiters();
			
			if (listDelimiters == null || listDelimiters.length == 0) {
				listDelimiters = new String[dataTypes.length];
				
				if (delimitersForDataList.getSelectedValue() != null)
					Arrays.fill(listDelimiters, delimitersForDataList.getSelectedValue());
			}
			
			if (indexColumnSourceInteraction > 0)
				indexColumnSourceInteraction--;

			if (indexColumnTargetInteraction > 0)
				indexColumnTargetInteraction--;

			if (indexColumnTypeInteraction > 0)
				indexColumnTypeInteraction--;
			
			networkName = previewPanel.getSourceName();
			
			ntmp = new NetworkTableMappingParameters(networkName, delimiters.getSelectedValues(),
					listDelimiters, attributeNames, dataTypesCopy, typesCopy,
					indexColumnSourceInteraction, indexColumnTargetInteraction, indexColumnTypeInteraction,
					defaultInteraction, startLoadRow, null);
			
			try {
				if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
				    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
					final Sheet sheet = workbook.getSheet(networkName);
					
					reader = new ExcelNetworkSheetReader(networkName, sheet, ntmp, nMap, rootNetwork, serviceRegistrar);
				} else {
					networkName = this.inputName;
					reader = new NetworkTableReader(networkName, new FileInputStream(tempFile), ntmp, nMap, rootNetwork, serviceRegistrar);
				}
			} catch (Exception ioe) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Unable to read network: "+ioe.getMessage());
				return;
			}
			
			loadNetwork(tm);
			tm.setProgress(1.0);
		} else {
			networkName = this.inputName;
			insertTasksAfterCurrentTask(netReader);
		}
	}
	
	private void loadNetwork(final TaskMonitor tm) throws IOException {
		final CyNetwork network = this.rootNetwork.addSubNetwork(); //CytoscapeServices.cyNetworkFactory.createNetwork();
		tm.setProgress(0.10);
		this.reader.setNetwork(network);

		if (this.cancelled)
			return;

		this.reader.read();
		tm.setProgress(0.80);

		if (this.cancelled)
			return;
		
		networks = new CyNetwork[] { network };
		tm.setProgress(1.0);
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork net) {
		if (netReader != null) {
			return netReader.buildCyNetworkView(net);
		} else {
			final CyNetworkView view = networkViewFactory.createNetworkView(net);
			final CyLayoutAlgorithm layout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();
			TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,"");
			Task nextTask = itr.next();
			
			try {
				nextTask.run(taskMonitor);
			} catch (Exception e) {
				throw new RuntimeException("Could not finish layout", e);
			}
	
			taskMonitor.setProgress(1.0);
			return view;	
		}
	}

	@Override
	public CyNetwork[] getNetworks() {
		if (netReader != null)
			return netReader.getNetworks();
		else
			return networks;
	}

	public String getName(){
		return networkName;
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		try {
			if (indexColumnSourceInteraction <= 0) {
				if (indexColumnTargetInteraction <= 0) {
					errMsg.append("The network cannot be created without selecting the source and target columns.");
					return ValidationState.INVALID;
				} else {
					errMsg.append("No edges will be created in the network; the source column is not selected.\nDo you want to continue?");
					return ValidationState.REQUEST_CONFIRMATION;
				}
			} else {
				if (indexColumnTargetInteraction <= 0) {
					errMsg.append("No edges will be created in the network; the target column is not selected.\nDo you want to continue?");
					return ValidationState.REQUEST_CONFIRMATION;
				} else {
					return ValidationState.OK;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return ValidationState.INVALID;
		}
	}
	
	// support import network in different collection
	private CyRootNetwork rootNetwork;
	public void setRootNetwork(CyRootNetwork rootNetwork){
		this.rootNetwork = rootNetwork;
	}
	
	private Map<Object, CyNode> nMap;
	public void setNodeMap(Map<Object, CyNode> nMap){
		this.nMap = nMap;
	}

	private CyNetworkViewFactory networkViewFactory;
	public void setNetworkViewFactory(CyNetworkViewFactory networkViewFactory) {
		this.networkViewFactory = networkViewFactory;
	}
}
