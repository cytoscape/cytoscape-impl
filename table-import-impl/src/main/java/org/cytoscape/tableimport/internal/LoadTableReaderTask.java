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



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.DefaultAttributeTableReader;
import org.cytoscape.tableimport.internal.reader.ExcelAttributeSheetReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextFileDelimiters;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;




public class LoadTableReaderTask extends AbstractTask implements CyTableReader , TunableValidator {
	private  InputStream isStart;
	private  InputStream isEnd;
	private String fileType;
	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;
	private String inputName;
	private PreviewTablePanel previewPanel;


	private CyTable[] cyTables;
	private static int numImports = 0;
	private boolean[] importFlag;
	
	public AttributeMappingParameters amp ;
	
	TextTableReader reader;
	
	@Tunable(description="Text Delimiters", context="both")
	public ListMultipleSelection<String> delimiters;
	
	@Tunable(description="Text Delimiters for data list type", context="both")
	public ListSingleSelection<String> delimitersForDataList;
	
	@Tunable(description="Start Load Row:", context="both")
	public int startLoadRow = -1;
	
	@Tunable(description="Key Column Index", context="both")
	public int keyColumnIndex = -1;
	
	@Tunable(description="First row used for column names", context="both")
	public boolean firstRowAsColumnNames = false;
	
	private final IconManager iconManager;

	public LoadTableReaderTask(final IconManager iconManager) {
		this.iconManager = iconManager;
		
		List<String> tempList = new ArrayList<String>();
		tempList.add(TextFileDelimiters.COMMA.toString());
		tempList.add(TextFileDelimiters.SEMICOLON.toString());
		tempList.add(TextFileDelimiters.SPACE.toString());
		tempList.add(TextFileDelimiters.TAB.toString());
		delimiters = new ListMultipleSelection<String>(tempList);
	    tempList = new ArrayList<String>();
		tempList.add(TextFileDelimiters.PIPE.toString());
		tempList.add(TextFileDelimiters.BACKSLASH.toString());
		tempList.add(TextFileDelimiters.SLASH.toString());
		tempList.add(TextFileDelimiters.COMMA.toString());
		delimitersForDataList = new ListSingleSelection<String>(tempList);
	}
	
	public LoadTableReaderTask(final InputStream is, final String fileType,final String inputName,
			final IconManager iconManager) {
		this(iconManager);
		setInputFile(is, fileType, inputName);
	}
	
	public void setInputFile(final InputStream is, final String fileType,final String inputName) {
		this.fileType     = fileType;
		this.inputName = inputName;
		this.isStart = is;
		
		previewPanel = new PreviewTablePanel(iconManager);
				
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
			
			
			this.isStart = new FileInputStream(tempFile);
			this.isEnd = new FileInputStream(tempFile);
		} catch (IOException e) {
			try {
				System.out.println("exceptioon catched!!");
				is.close();
			} catch (IOException e1) {
			}
			
			this.isStart = null;
			throw new IllegalStateException("Could not initialize object", e);
		}
		
		List<String> tempList = new ArrayList<String>();
		tempList = new ArrayList<String>();
		tempList.add(TextFileDelimiters.TAB.toString());
		tempList.add(TextFileDelimiters.SPACE.toString());
		delimiters.setSelectedValues(tempList);
		delimitersForDataList.setSelectedValue(TextFileDelimiters.PIPE.toString());
	}


	@Override
	public void run(TaskMonitor tm) throws Exception {
		
		tm.setTitle("Loading table data");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading table...");
		
		List<String> attrNameList = new ArrayList<String>();
		int colCount;
		int startLoadRowTemp;
		String[] attributeNames;
		
		Workbook workbook = null;
		// Load Spreadsheet data for preview.
		if(fileType != null && (fileType.equalsIgnoreCase(
				SupportedFileType.EXCEL.getExtension())
				|| fileType.equalsIgnoreCase(
						SupportedFileType.OOXML.getExtension())) && workbook == null) {
			try {
				workbook = WorkbookFactory.create(isStart);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?");
			} finally {
				if (isStart != null) {
					isStart.close();
				}
			}
		}
		if(startLoadRow > 0)
			startLoadRow--;
		startLoadRowTemp = startLoadRow;
		if(firstRowAsColumnNames)
			startLoadRowTemp = 0;
		
		previewPanel.setPreviewTable(workbook, fileType,inputName, isStart, delimiters.getSelectedValues(), null, 50, null, startLoadRowTemp);
		
		colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
		importFlag = new boolean[colCount];
		Object curName = null;
		
		if(firstRowAsColumnNames)
		{
			setFirstRowAsColumnNames();
			startLoadRow++;
		}

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = true;
			curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();
			
			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (importFlag[i] && importFlag[dupIndex]) {
//TODO add message to user
					return;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}
		attributeNames = attrNameList.toArray(new String[0]);
		
		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());

		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}
		
		if(keyColumnIndex >0)
			keyColumnIndex--;

		amp = new AttributeMappingParameters(delimiters.getSelectedValues(),delimitersForDataList.getSelectedValue(),keyColumnIndex,attributeNames,attributeTypes,
				previewPanel.getCurrentListDataTypes(),importFlag,true,startLoadRow,null);
		
		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
			    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension()))
			{

				// Fixed bug# 1668, Only load data from the first sheet, ignore the rest sheets
				if (workbook.getNumberOfSheets() >0){
					final Sheet sheet = workbook.getSheetAt(0);
					this.reader = new ExcelAttributeSheetReader(sheet, amp);
					loadAnnotation(tm);
				}
			} else {
				this.reader = new DefaultAttributeTableReader(null,amp,this.isEnd); 
				loadAnnotation(tm);
			}
	}

	public void setFirstRowAsColumnNames()
	{
		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();
		String[] columnHeaders;
		
		if ((previewPanel.getPreviewTable() != null) && (model != null)) {
			columnHeaders = new String[previewPanel.getPreviewTable().getColumnCount()];
	
			for (int i = 0; i < columnHeaders.length; i++) {
				// Save the header
				columnHeaders[i] = model.getValueAt(0, i).toString();
				previewPanel.getPreviewTable().getColumnModel().getColumn(i).setHeaderValue(columnHeaders[i]);
			}
	
			model.removeRow(0);
			
		}
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
		tm.setProgress(1.0);
	}

	

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (keyColumnIndex <= 0){
			try {
				errMsg.append("The primary key column needs to be selected. Please select values from 1 to the number of columns");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
		
		if (startLoadRow < 0){
			try {
				errMsg.append("The row that will be used as starting point needs to be selected.");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
			return ValidationState.OK;
	}
	
}
