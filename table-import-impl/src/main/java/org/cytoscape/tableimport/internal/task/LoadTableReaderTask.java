package org.cytoscape.tableimport.internal.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
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
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
	
	@ContainsTunables
	public DelimitersTunable delimiters = new DelimitersTunable();
	
	@Tunable(description="Text Delimiters for data list type", 
	         longDescription="The delimiters between elements of list columns in the table.",
	         exampleStringValue="\'|\'",
	         context="both")
	public ListSingleSelection<String> delimitersForDataList;
	
	@Tunable(description="Start Load Row", 
	         longDescription="The first row of the input table to load.  This allows the skipping of headers that are not part of the import.",
	         exampleStringValue="10",
	         context="both")
	public int startLoadRow = -1;
	
	@Tunable(description="Key Column Index", 
	         longDescription="The column that contains the key values for this import.  These values will be used to match with the key values in the network.",
	         exampleStringValue="1",
	         context="both")
	public int keyColumnIndex = -1;
	
	@Tunable(description="First row used for column names", 
	         longDescription="If the first imported row contains column names, set this to ```true```.",
	         exampleStringValue="true",
	         context="both")
	public boolean firstRowAsColumnNames;
	
	@Tunable(description="List of column data types ordered by column index (e.g. \"string,int,long,double,boolean,intlist\" or just \"s,i,l,d,b,il\")", 
	         longDescription="List of column data types ordered by column index (e.g. \"string,int,long,double,boolean,intlist\" or just \"s,i,l,d,b,il\")",
	         exampleStringValue="s,s,i",
	         context="nongui")
	public String dataTypeList;
	
	private final TableImportContext tableImportContext;
	private final CyServiceRegistrar serviceRegistrar;

	public LoadTableReaderTask(TableImportContext tableImportContext, CyServiceRegistrar serviceRegistrar) {
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
		
		List<String> tempList = new ArrayList<>();
		tempList.add(TextDelimiter.PIPE.getDelimiter());
		tempList.add(TextDelimiter.BACKSLASH.getDelimiter());
		tempList.add(TextDelimiter.SLASH.getDelimiter());
		tempList.add(TextDelimiter.COMMA.getDelimiter());
		delimitersForDataList = new ListSingleSelection<String>(tempList);
	}
	
	public LoadTableReaderTask(
			final InputStream is,
			final String fileType,
			final String inputName,
			final TableImportContext tableImportContext,
			final CyServiceRegistrar serviceRegistrar
	) {
		this(tableImportContext, serviceRegistrar);
		setInputFile(is, fileType, inputName);
	}
	
	public void setInputFile(final InputStream is, final String fileType, final String inputName) {
		this.fileType = fileType;
		this.inputName = inputName;
		this.isStart = is;

		previewPanel = new PreviewTablePanel(ImportType.TABLE_IMPORT, tableImportContext, serviceRegistrar.getService(IconManager.class));
				
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
		
		List<TextDelimiter> tempList = new ArrayList<>();
		if (fileType.equals(".csv"))
			tempList.add(TextDelimiter.COMMA);
		else
			tempList.add(TextDelimiter.TAB);
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
					
					// In case of an Excel sheet, the reader will use String.valueOf() to format numbers
					// In this case, the decimal separator does not depend on the Locale
					// It is the dot
					decimalSeparator = '.';
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
		
		if(decimalSeparator == null) {
			decimalSeparator = AbstractMappingParameters.DEF_DECIMAL_SEPARATOR;
		}
		
		if (startLoadRow > 0)
			startLoadRow--;
		
		final int startLoadRowTemp = firstRowAsColumnNames ? 0 : startLoadRow;
		
		previewPanel.update(
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
		
		// Semantic Types:
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
					throw new Exception("Duplicate column name \""+curName+"\".");
				}
			}

			if (curName == null)
				attrNameList.add("Column " + i);
			else
				attrNameList.add(curName.toString());
		}
		
		attributeNames = attrNameList.toArray(new String[0]);
		
		final SourceColumnSemantic[] typesCopy = Arrays.copyOf(types, types.length);
		
		// Data Types:
		final AttributeDataType[] dataTypes = previewPanel.getDataTypes();
		final AttributeDataType[] dataTypesCopy = Arrays.copyOf(dataTypes, dataTypes.length);
		
		AttributeDataType[] tunableDataTypes = null;
		
		if (dataTypeList != null && !dataTypeList.trim().isEmpty())
			tunableDataTypes = TypeUtil.parseDataTypeList(dataTypeList);
		
		if (tunableDataTypes != null && tunableDataTypes.length > 0)
			System.arraycopy(
					tunableDataTypes, 0,
					dataTypesCopy, 0, 
					Math.min(tunableDataTypes.length, dataTypesCopy.length));
		
		String[] listDelimiters = previewPanel.getListDelimiters();
		
		if (listDelimiters == null || listDelimiters.length == 0) {
			listDelimiters = new String[dataTypes.length];
			
			if (delimitersForDataList.getSelectedValue() != null)
				Arrays.fill(listDelimiters, delimitersForDataList.getSelectedValue());
		}
		
		// Namespaces:
		final String[] namespaces = previewPanel.getNamespaces();
		final String[] namespacesCopy = Arrays.copyOf(namespaces, namespaces.length);

// TODO Set namespaces though Tunables as well
//		String[] tunableNamespaces = null;
//		
//		if (namespaceList != null && !namespaceList.trim().isEmpty())
//			tunableNamespaces = TypeUtil.parseDataTypeList(namespaceList);
//		
//		if (tunableNamespaces != null && tunableNamespaces.length > 0)
//			System.arraycopy(
//					tunableNamespaces, 0,
//					namespacesCopy, 0, 
//					Math.min(tunableNamespaces.length, namespacesCopy.length));
		
		if (keyColumnIndex > 0)
			keyColumnIndex--;

		amp = new AttributeMappingParameters(sourceName, delimiters.getSelectedValues(), listDelimiters,
				keyColumnIndex, attributeNames, dataTypesCopy, typesCopy, namespacesCopy, startLoadRow, null);
		
		if (this.fileType.equalsIgnoreCase(SupportedFileType.EXCEL.getExtension()) ||
		    this.fileType.equalsIgnoreCase(SupportedFileType.OOXML.getExtension())) {
			
			// Fixed bug# 1668, Only load data from the first sheet, ignore the rest sheets
			// UPDATE: From the user perspective it makes more sense to get the selected tab/sheet than the first one.
			final Sheet sheet = workbook.getSheet(sourceName);
			
			if (sheet != null) {
				reader = new ExcelAttributeSheetReader(sheet, amp, serviceRegistrar);
				loadAnnotation(tm);
			}
		} else {
			reader = new DefaultAttributeTableReader(null, amp, this.isEnd, serviceRegistrar); 
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
		
		final int keyIndex = readerAMP.getKeyIndex();
		final String pk = keyIndex >= 0 ? readerAMP.getAttributeNames()[keyIndex] : CyTable.SUID;
		final Class<?> pkType = keyIndex >= 0 ? String.class : Long.class;
		
		tm.setProgress(0.1);

		final CyTableFactory tableFactory = serviceRegistrar.getService(CyTableFactory.class);
		final CyTable table = tableFactory.createTable(
				"AttrTable " + inputName.substring(inputName.lastIndexOf('/') + 1) + " " + Integer.toString(numImports++),
			    pk, pkType, true, true);
		
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
		if (tableImportContext.isKeyRequired() && keyColumnIndex <= 0) {
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
