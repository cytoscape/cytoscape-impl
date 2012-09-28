package org.cytoscape.tableimport.internal;


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
import org.cytoscape.tableimport.internal.reader.ExcelNetworkSheetReader;
import org.cytoscape.tableimport.internal.reader.GraphReader;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.reader.NetworkTableReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;


public class ImportNetworkTableReaderTask extends AbstractTask implements CyNetworkReader, TunableValidator {
	private  InputStream is;
	private final String fileType;
	private CyNetwork[] networks;
	private final String inputName;
	private GraphReader reader;
	
	@Tunable(description="Network table Mapping Parameter")
	public NetworkTableMappingParameters ntmp;

	
	public ImportNetworkTableReaderTask(final InputStream is, final String fileType,
					    final String inputName)
	{
		this.is           = is;
		this.fileType     = fileType;
		this.inputName    = inputName;

		try{
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
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public void run(TaskMonitor monitor) throws Exception {

		monitor.setTitle("Loading network from table");
		monitor.setProgress(0.0);
		monitor.setStatusMessage("Loading network...");

		Workbook workbook = null;
		// Load Spreadsheet data for preview.
		if(fileType != null && (fileType.equalsIgnoreCase(
				SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(
						SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(is);
			} catch (InvalidFormatException e) {
				//e.printStackTrace();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?" , e);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		
		String networkName;

		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
		    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()))
		{
			Sheet sheet = workbook.getSheetAt(0);
			networkName = workbook.getSheetName(0);
			
			reader = new ExcelNetworkSheetReader(networkName, sheet, ntmp, this.nMap, this.rootNetwork);
		} else {
			networkName = this.inputName;
			reader = new NetworkTableReader(networkName, this.is, ntmp, this.nMap, this.rootNetwork);
		}
		loadNetwork(monitor);

		monitor.setProgress(1.0);
	}

	private void loadNetwork(TaskMonitor tm) throws IOException {
		
		
		final CyNetwork network = this.rootNetwork.addSubNetwork(); //CytoscapeServices.cyNetworkFactory.createNetwork();
		tm.setProgress(0.10);
		this.reader.setNetwork(network);

		if (this.cancelled){
			return;
		}

		this.reader.read();

		tm.setProgress(0.80);

		if (this.cancelled){
			return;
		}
		
		networks = new CyNetwork[]{network};

		tm.setProgress(1.0);
		
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork arg0) {
		final CyNetworkView view = CytoscapeServices.cyNetworkViewFactory.createNetworkView(arg0);
		return view;
	}

	@Override
	public CyNetwork[] getNetworks() {
		return networks;
	}


	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		try{
		if (ntmp.getSourceIndex() == -1){
			if (ntmp.getTargetIndex() == -1){
				errMsg.append("The network cannot be created without selecting the source and target columns.");
				return ValidationState.INVALID;
			}else{
				errMsg.append("No edges will be created in the network; the target column is not selected.\nDo you want to continue?");
				return ValidationState.REQUEST_CONFIRMATION;
			}
		}else{
			if (ntmp.getTargetIndex() == -1){
				errMsg.append("No edges will be created in the network; the source column is not selected.\nDo you want to continue?");
				return ValidationState.REQUEST_CONFIRMATION;
			}else
				return ValidationState.OK;
		}
		}catch(IOException ioe){
			ioe.printStackTrace();
			return ValidationState.INVALID;
		}
	}
	
	//
	// support import network in different collection
	private CyRootNetwork rootNetwork;
	public void setRootNetwork(CyRootNetwork rootNetwork){
		this.rootNetwork = rootNetwork;
	}
	
	private Map<Object, CyNode> nMap;
	public void setNodeMap(Map<Object, CyNode> nMap){
		this.nMap = nMap;
	}

}
