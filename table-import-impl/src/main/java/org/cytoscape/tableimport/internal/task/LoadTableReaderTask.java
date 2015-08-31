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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.DefaultAttributeTableReader;
import org.cytoscape.tableimport.internal.reader.ExcelAttributeSheetReader;
import org.cytoscape.tableimport.internal.reader.SupportedFileType;
import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class LoadTableReaderTask extends AbstractTask implements CyTableReader, TunableValidator {
	
	private InputStream isStart;
	private InputStream isEnd;
	private String fileType;
	protected CyNetworkView[] cyNetworkViews;
	protected VisualStyle[] visualstyles;
	private String inputName;
	private PreviewTablePanel previewPanel;

	private CyTable[] cyTables;
	private static int numImports = 0;
	
	public AttributeMappingParameters amp;
	
	TextTableReader reader;
	
	@Tunable(description="Text Delimiters:", context="both")
	public ListMultipleSelection<String> delimiters;
	
	@Tunable(description="Text Delimiters for data list type:", context="both")
	public ListSingleSelection<String> delimitersForDataList;
	
	@Tunable(description="Start Load Row:", context="both")
	public int startLoadRow = -1;
	
	@Tunable(description="Key Column Index", context="both")
	public int keyColumnIndex = -1;
	
	@Tunable(description="First row used for column names:", context="both")
	public boolean firstRowAsColumnNames = false;
	
	private final CyServiceRegistrar serviceRegistrar;

	public LoadTableReaderTask(final CyServiceRegistrar serviceRegistrar) {
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
		delimitersForDataList = new ListSingleSelection<String>(tempList);
	}
	
	public LoadTableReaderTask(final InputStream is, final String fileType,final String inputName,
			final CyServiceRegistrar serviceRegistrar) {
		this(serviceRegistrar);
		setInputFile(is, fileType, inputName);
	}
	
	public void setInputFile(final InputStream is, final String fileType, final String inputName) {
		this.fileType = fileType;
		this.inputName = inputName;
		this.isStart = is;
		
		previewPanel = new PreviewTablePanel(serviceRegistrar.getService(IconManager.class));
				
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
		
		List<String> tempList = new ArrayList<>();
		tempList.add(TextDelimiter.TAB.getDelimiter());
		tempList.add(TextDelimiter.SPACE.getDelimiter());
		delimiters.setSelectedValues(tempList);
		delimitersForDataList.setSelectedValue(TextDelimiter.PIPE.getDelimiter());
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		tm.setTitle("Loading table data");
		tm.setProgress(0.0);
		tm.setStatusMessage("Loading table...");
		
		List<String> attrNameList = new ArrayList<>();
		int colCount;
		String[] attributeNames;
		
		Workbook workbook = null;
		// Load Spreadsheet data for preview.
		try {
			if (fileType != null && 
					(fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension())
					|| fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) &&
					workbook == null) {
				try {
					workbook = WorkbookFactory.create(isStart);
				} catch (InvalidFormatException e) {
					e.printStackTrace();
					throw new IllegalArgumentException("Could not read Excel file.  Maybe the file is broken?");
				} finally {
					if (isStart != null)
						isStart.close();
				}
			}
		} catch (Exception ioe) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to read table: "+ioe.getMessage());
			return;
		}
		
		if (startLoadRow > 0)
			startLoadRow--;
		
		final int startLoadRowTemp = firstRowAsColumnNames ? 0 : startLoadRow;
		
		previewPanel.updatePreviewTable(
				workbook,
				fileType,
				inputName,
				isStart,
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

		final String sourceName = previewPanel.getSourceName();
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

				if (!TypeUtil.allowsDuplicateName(ImportType.TABLE_IMPORT, types[i], types[dupIndex])) {
//TODO add message to user
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
		
		if (keyColumnIndex > 0)
			keyColumnIndex--;

		amp = new AttributeMappingParameters(sourceName, delimiters.getSelectedValues(), listDelimiters,
				keyColumnIndex, attributeNames, dataTypesCopy, typesCopy, startLoadRow, null);
		
		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
		    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
			
			// Fixed bug# 1668, Only load data from the first sheet, ignore the rest sheets
			// UPDATE: From the user perspective it makes more sense to get the selected tab/sheet than the first one.
			final Sheet sheet = workbook.getSheet(sourceName);
			
			if (sheet != null) {
				reader = new ExcelAttributeSheetReader(sheet, amp);
				loadAnnotation(tm);
			}
		} else {
			reader = new DefaultAttributeTableReader(null, amp, this.isEnd); 
			loadAnnotation(tm);
		}
	}

	@Override
	public CyTable[] getTables() {
		return cyTables;
	}
	
	private void loadAnnotation(TaskMonitor tm) {
		tm.setProgress(0.0);
		
		TextTableReader reader = this.reader;
		AttributeMappingParameters readerAMP = (AttributeMappingParameters) reader.getMappingParameter();
		String primaryKey = readerAMP.getAttributeNames()[readerAMP.getKeyIndex()];
		tm.setProgress(0.1);

		final CyTableFactory tableFactory = serviceRegistrar.getService(CyTableFactory.class);
		final CyTable table = tableFactory.createTable(
				"AttrTable " + inputName.substring(inputName.lastIndexOf('/') + 1) + " " + Integer.toString(numImports++),
			    primaryKey, String.class, true, true);
		
		cyTables = new CyTable[] { table };
		tm.setProgress(0.3);
		
		try {
			this.reader.readTable(table);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tm.setProgress(1.0);
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (keyColumnIndex <= 0) {
			try {
				errMsg.append("The primary key column needs to be selected. Please select values from 1 to the number of columns");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return ValidationState.INVALID;
		}
		
		if (startLoadRow < 0) {
			try {
				errMsg.append("The row that will be used as starting point needs to be selected.");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return ValidationState.INVALID;
		}
		
		return ValidationState.OK;
	}
}
