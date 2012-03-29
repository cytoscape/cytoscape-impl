package org.cytoscape.tableimport.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.DefaultAttributeTableReader;
import org.cytoscape.tableimport.internal.reader.ExcelAttributeSheetReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.task.MapNetworkAttrTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;


public class ImportAttributeTableReaderTask extends AbstractTask implements CyTableReader , TunableValidator {
	private  InputStream is;
	private final String fileType;
	private final CyTableManager tableManager;
	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;
	private final String inputName;


	private CyTable[] cyTables;
	private static int numImports = 0;
	
	@Tunable(description="Attribute Mapping Parameters")
	public AttributeMappingParameters amp ;
	
	TextTableReader reader;

	public ImportAttributeTableReaderTask(final InputStream is, final String fileType,
			final String inputName, final CyTableManager tableManager)
	{
		
		this.fileType     = fileType;
		this.tableManager = tableManager;
		this.inputName = inputName;
		this.is = is;
		
		try {
	
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
			
			amp = new AttributeMappingParameters(new FileInputStream(tempFile), fileType);
			this.is = new FileInputStream(tempFile);
		} catch (IOException e) {
			e.printStackTrace();
			this.is = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	@Override
	public void run(TaskMonitor tm) throws Exception {
		
		tm.setTitle("Loading attribute table data");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading table...");
		
		Workbook workbook = null;
		// Load Spreadsheet data for preview.
		if(fileType != null && (fileType.equalsIgnoreCase(
				SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(
						SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(is);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?");
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		
		
		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
			    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()))
			{
				
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					final Sheet sheet = workbook.getSheetAt(i);

					this.reader = new ExcelAttributeSheetReader(sheet, amp);
					loadAnnotation(tm);
				}
			} else {
				this.reader = new DefaultAttributeTableReader(null,amp,this.is); 
				loadAnnotation(tm);
			}
		insertTasksAfterCurrentTask(CytoscapeServices.mapGlobalToLocalTableTaskFactory.createTaskIterator());
	}


	@Override
	public CyTable[] getTables() {
		return cyTables;
	}
	
	
	private void loadAnnotation( TaskMonitor tm){

		tm.setProgress(0.0);
		
		TextTableReader reader = this.reader;
		AttributeMappingParameters readerAMP = (AttributeMappingParameters) reader.getMappingParameter();
		String primaryKey = readerAMP.getAttributeNames()[readerAMP.getKeyIndex()];
		tm.setProgress(0.1);

		final CyTable table =
			CytoscapeServices.cyTableFactory.createTable("AttrTable " + inputName.substring(inputName.lastIndexOf('/') + 1) + " "
			                                           + Integer.toString(numImports++),
			                                           primaryKey, String.class, true,
			                                           true);
		cyTables = new CyTable[] { table };
		tm.setProgress(0.2);

		tm.setProgress(0.3);
		try {
			this.reader.readTable(table);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tm.setProgress(0.9);
		tableManager.addTable(table);
		tm.setProgress(1.0);
	}


	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (amp.getKeyIndex() == -1){
			try {
				errMsg.append("The primary key column needs to be selected!");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
			return ValidationState.OK;
	}
	
}
