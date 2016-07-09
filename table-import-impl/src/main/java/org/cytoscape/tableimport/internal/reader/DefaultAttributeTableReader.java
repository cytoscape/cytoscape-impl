package org.cytoscape.tableimport.internal.reader;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Basic text table reader for attributes.<br>
 *
 * <p>
 * based on the given parameters, map the text table to CyAttributes.
 * </p>
 *
 * @author kono
 */
public class DefaultAttributeTableReader implements TextTableReader {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultAttributeTableReader.class);
	
	private final URL source;
	private AttributeMappingParameters mapping;
	private final AttributeLineParser parser;
	
	/** Number of mapped attributes. */
	private int globalCounter;

	/** Reader will read entries from this line. */
	private final int startLineNumber;
	private String commentChar;
	
	private InputStream is;
	
	public DefaultAttributeTableReader(final URL source, AttributeMappingParameters mapping,
            final int startLineNumber, final String commentChar, final CyServiceRegistrar serviceRegistrar) {
		this.source = source;
		this.mapping = mapping;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(mapping, serviceRegistrar);
		this.commentChar = commentChar;
	}

	public DefaultAttributeTableReader(final URL source, AttributeMappingParameters mapping, InputStream is,
			final CyServiceRegistrar serviceRegistrar) {
		this.source = source;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.parser = new AttributeLineParser(mapping, serviceRegistrar);
		this.commentChar = mapping.getCommentChar();
		this.is = is;
	}
	
	@Override
	public List<String> getColumnNames() {
		return Arrays.asList(mapping.getAttributeNames());
	}

	/**
	 * Read table from the data source.
	 */
	@Override
	public void readTable(CyTable table) throws IOException {
		try {
			BufferedReader bufRd = null;

			if (is == null)
				is = URLUtil.getInputStream(source);				
			
			try {
				// This data is shared by both the OpenCSV and the old method of reading files.
				int lineCount = 0;
				bufRd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8").newDecoder()));
				/*
				 * Read & extract one line at a time. The line can be Tab delimited,
				 */
				final String delimiter = mapping.getDelimiterRegEx();

				//If the delimiter contains a comma, treat the file as a CSV file.
				if (delimiter.contains(TextDelimiter.COMMA.getDelimiter()) && mapping.getDelimiters().size() == 1) {
					//Use OpenCSV.. New method...
					CSVReader reader = new CSVReader(bufRd);
					String [] rowData; //Note that rowData is roughly equivalent to "parts" in the old code.
					
					while ((rowData = reader.readNext()) != null) {
						// If key dos not exists, ignore the line.
						if (lineCount >= startLineNumber && rowData.length >= mapping.getKeyIndex() + 1) {
							try {
								parser.parseAll(table, rowData);
							} catch (Exception ex) {
								logger.warn("Couldn't parse row from OpenCSV: "+ lineCount);
							}
							
							globalCounter++;
						}
						
						lineCount++;
					}

					try {
						reader.close();
					} catch (Exception e) { }
				} else { //Use the "old" method for splitting the lines.
					String line;
					String[] parts = null;
					
					while ((line = bufRd.readLine()) != null) {
						/*
						 * Ignore Empty & Commnet lines.
						 */
						if ((commentChar != null) && line.startsWith(commentChar)) {
							// Do nothing
						} else if ((lineCount >= startLineNumber) && (line.trim().length() > 0)) {
							parts = line.split(delimiter);
							
							// If key does not exists, ignore the line.
							if (parts.length >= mapping.getKeyIndex() + 1) {
								try {
									parser.parseAll(table, parts);
								} catch (Exception ex) {
									logger.warn("Couldn't parse row: "+ lineCount);
								}
								
								globalCounter++;
							}
						}

						lineCount++;
					}
				}
			} catch (MalformedInputException mie) {
				throw new IOException("Unable to import table: illegal character encoding in input");
			} finally {
				if (bufRd != null)
					bufRd.close();
			}
		} finally {
			if (is != null)
				is.close();
		}
	}

	@Override
	public String getReport() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> invalid = parser.getInvalidMap();
		sb.append(globalCounter + " entries are loaded and mapped into table.");
		
		if (!invalid.isEmpty()) {
			sb.append("\n\nThe following enties are invalid and were not imported:\n");
			int limit = 10;
			
			for (String key : invalid.keySet()) {
				sb.append(key + " = " + invalid.get(key) + "\n");
				
				if (limit-- <= 0) {
					sb.append("Approximately " + (invalid.size() - 10) + " additional entries were not imported...");
					break;
				}
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public MappingParameter getMappingParameter() {
		return mapping;
	}
}
