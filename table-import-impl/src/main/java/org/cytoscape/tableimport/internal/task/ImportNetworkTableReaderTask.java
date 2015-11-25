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
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.ExcelNetworkSheetReader;
import org.cytoscape.tableimport.internal.reader.GraphReader;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;


public class ImportNetworkTableReaderTask extends AbstractTask implements CyNetworkReader, TunableValidator {
	
	private  InputStream is;
	private final String fileType;
	private CyNetwork[] networks;
	private final String inputName;
	private GraphReader reader;
	
	@Tunable(description="Network Table Mapping Parameter:")
	public NetworkTableMappingParameters ntmp;
	
	// support import network in different collection
	private CyRootNetwork rootNetwork;
	private Map<Object, CyNode> nMap;
	private CyNetworkViewFactory networkViewFactory;
	private final CyServiceRegistrar serviceRegistrar;
	
	public ImportNetworkTableReaderTask(final InputStream is, final String fileType, final String inputName,
			final CyServiceRegistrar serviceRegistrar) {
		this.is = is;
		this.fileType = fileType;
		this.inputName = inputName;
		this.serviceRegistrar = serviceRegistrar;

		try  {
			File tempFile = File.createTempFile("temp", this.fileType);
			tempFile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(tempFile);
			int read = 0;
			byte[] bytes = new byte[1024];
		 
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
			os.flush();
			os.close();
			
			ntmp = new NetworkTableMappingParameters(new FileInputStream(tempFile) , fileType);
			this.is = new FileInputStream(tempFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Loading network from table");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading network...");

		Workbook workbook = null;
		
		// Load Spreadsheet data for preview.
		if (fileType != null && 
				(fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(is);
			} catch (InvalidFormatException e) {
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?" , e);
			} finally {
				if (is != null)
					is.close();
			}
		}
		
		try {
			if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
			    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
				String networkName = ntmp.getName();
				
				if (networkName == null)
					networkName = workbook.getSheetName(0);
				
				final Sheet sheet = workbook.getSheet(networkName);
				
				reader = new ExcelNetworkSheetReader(networkName, sheet, ntmp, nMap, rootNetwork, serviceRegistrar);
			} else {
				reader = new NetworkTableReader(inputName, is, ntmp, nMap, rootNetwork, serviceRegistrar);
			}
		} catch (Exception ioe) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to read table: "+ioe.getMessage());
			return;
		}
		
		loadNetwork(tm);
		tm.setProgress(1.0);
	}

	private void loadNetwork(TaskMonitor tm) throws IOException {
		final CyNetwork network = this.rootNetwork.addSubNetwork();
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
		final CyNetworkView view = networkViewFactory.createNetworkView(net);
		return view;
	}

	@Override
	public CyNetwork[] getNetworks() {
		return networks;
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		try {
			// TODO: check duplicate columns here!!!
			
			if (ntmp.getSourceIndex() == -1) {
				if (ntmp.getTargetIndex() == -1) {
					errMsg.append("The network cannot be created without selecting the source and target columns.");
					return ValidationState.INVALID;
				} else {
					errMsg.append("No edges will be created in the network; the target column is not selected.\nDo you want to continue?");
					return ValidationState.REQUEST_CONFIRMATION;
				}
			} else {
				if (ntmp.getTargetIndex() == -1){
					errMsg.append("No edges will be created in the network; the source column is not selected.\nDo you want to continue?");
					return ValidationState.REQUEST_CONFIRMATION;
				} else {
					return ValidationState.OK;
				}
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return ValidationState.INVALID;
		}
	}
	
	public void setRootNetwork(CyRootNetwork rootNetwork) {
		this.rootNetwork = rootNetwork;
	}
	
	public void setNodeMap(Map<Object, CyNode> nMap) {
		this.nMap = nMap;
	}

	public void setNetworkViewFactory(CyNetworkViewFactory networkViewFactory) {
		this.networkViewFactory = networkViewFactory;
	}
}
