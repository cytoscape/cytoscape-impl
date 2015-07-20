package org.cytoscape.tableimport.internal.util;

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

import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.ImportType.NETWORK_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.ONTOLOGY_IMPORT;
import static org.cytoscape.tableimport.internal.util.ImportType.TABLE_IMPORT;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ALIAS;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.EDGE_ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.INTERACTION;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.KEY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.NONE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.ONTOLOGY;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.SOURCE;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.SOURCE_ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TARGET;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TARGET_ATTR;
import static org.cytoscape.tableimport.internal.util.SourceColumnSemantic.TAXON;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

/**
 *
 */
public class TypeUtil {

	private static final List<SourceColumnSemantic> TABLE_IMPORT_TYPES = Arrays.asList(
			NONE, KEY, ATTR
	);
	private static final List<SourceColumnSemantic> NETWORK_IMPORT_TYPES = Arrays.asList(
			NONE, SOURCE, INTERACTION, TARGET, EDGE_ATTR, SOURCE_ATTR, TARGET_ATTR
	);
	private static final List<SourceColumnSemantic> ONTOLOGY_IMPORT_TYPES = Arrays.asList(
			NONE, KEY, ALIAS, ONTOLOGY, TAXON, ATTR
	);
	
	private static final String[] PREF_KEY_NAMES = new String[] {
		"shared name", "name", "identifier", "id", "node", "node id", "edge", "edge id",
		"names", "identifiers", "ids", "nodes", "node ids", "edges", "edge ids",
		"gene", "gene id", "gene name", "protein",
	};
	private static final String[] PREF_SOURCE_NAMES = new String[] {
		"source", "source node", "source name", "source id", "source identifier",
		"node 1", "node a", "identifier 1", "identifier a", "id 1", "id a",
		"key 1", "key a",
		"source shared name", "name 1", "name a", "shared name 1", "shared name a",
		"source gene", "gene 1", "gene id 1", "gene name 1", "id interactor a",
		"name", "shared name", "node", "gene", "gene id", "gene name", "id", "identifier"
	};
	private static final String[] PREF_TARGET_NAMES = new String[] {
		"target", "target node", "target name", "target id", "target identifier",
		"node 2", "node b", "identifier 2", "identifier b", "id 2", "id b",
		"key 2", "key b",
		"target shared name", "name 2", "name b", "shared name 2", "shared name b",
		"target gene", "gene 2", "gene id 2", "gene name 2", "id interactor b"
	};
	private static final String[] PREF_INTERACTION_NAMES = new String[] {
		"interaction", "interaction type", "interaction types", "edge type", "edge types",
		"interaction id", "interaction identifier",
		"type"
	};
	private static final String[] PREF_ONTOLOGY_NAMES = new String[] {
		"gene ontology", "ontology", "go"
	};
	private static final String[] PREF_TAXON_NAMES = new String[] {
		"taxon", "tax id", "taxonomy", "organism"
	};
	
	private static Pattern truePattern = Pattern.compile("^true$", Pattern.CASE_INSENSITIVE);
	private static Pattern falsePattern = Pattern.compile("^false$", Pattern.CASE_INSENSITIVE);
	
	private TypeUtil() {}
	
	public static List<SourceColumnSemantic> getAvailableTypes(final ImportType importType) {
		if (importType == NETWORK_IMPORT) return NETWORK_IMPORT_TYPES;
		if (importType == ONTOLOGY_IMPORT) return ONTOLOGY_IMPORT_TYPES;
		
		return TABLE_IMPORT_TYPES;
	}
	
	public static SourceColumnSemantic getDefaultType(final ImportType importType) {
		return importType == NETWORK_IMPORT ? EDGE_ATTR : ATTR;
	}
	
	public static SourceColumnSemantic[] guessTypes(final ImportType importType, final TableModel model,
			final AttributeDataType[] dataTypes) {
		final int size = model.getColumnCount();
		
		final SourceColumnSemantic[] types = new SourceColumnSemantic[size];
		
		if (importType == NETWORK_IMPORT)
			Arrays.fill(types, EDGE_ATTR);
		else
			Arrays.fill(types, ATTR);
		
		if (dataTypes == null || dataTypes.length == 0 || dataTypes.length != model.getColumnCount())
			return types;
		
		boolean srcFound = false;
		boolean tgtFound = false;
		boolean interactFound = false;
		boolean keyFound = false;
		boolean goFound = false;
		boolean taxFound = false;

		// First pass: Look for exact column name
		// Second pass: Select column whose name contains one of the tokens
		MAIN_LOOP:
		for (int count = 0; count < 2; count++) {
			boolean exact = count == 0;
			
			for (int i = 0; i < size; i++) {
				final String name = model.getColumnName(i);
				final AttributeDataType dataType = dataTypes[i];
				
				if (importType == NETWORK_IMPORT) {
					if (!srcFound && matches(name, PREF_SOURCE_NAMES, exact) && isValid(SOURCE, dataType)) {
						srcFound = true;
						types[i] = SOURCE;
					} else if (!tgtFound && matches(name, PREF_TARGET_NAMES, exact) && isValid(TARGET, dataType)) {
						tgtFound = true;
						types[i] = TARGET;
					} else if (!interactFound && matches(name, PREF_INTERACTION_NAMES, exact) &&
							isValid(INTERACTION, dataType)) {
						interactFound = true;
						types[i] = INTERACTION;
					}
					
					if (srcFound && tgtFound && interactFound)
						break MAIN_LOOP;
				} else if (importType == ONTOLOGY_IMPORT) {
					if (!keyFound && matches(name, PREF_KEY_NAMES, exact) && canBeKey(model, i, dataType)) {
						keyFound = true;
						types[i] = KEY;
					} else if (!goFound && matches(name, PREF_ONTOLOGY_NAMES, exact) && isValid(ONTOLOGY, dataType)) {
						goFound = true;
						types[i] = ONTOLOGY;
					} else if (!taxFound && matches(name, PREF_TAXON_NAMES, exact) && isValid(TAXON, dataType)) {
						taxFound = true;
						types[i] = TAXON;
					}
					
					if (keyFound && goFound && taxFound)
						break MAIN_LOOP;
				} else if (!keyFound) {
					if (canBeKey(model, i, dataType)) {
						keyFound = true;
						types[i] = KEY;
						break MAIN_LOOP;
					}
				}
			}
		}
		
		if (importType == TABLE_IMPORT && !keyFound) {
			// Just use the first String or Integer column as key then...
			for (int i = 0; i < types.length; i++) {
				if (dataTypes[i] == TYPE_STRING || dataTypes[i] == TYPE_INTEGER || dataTypes[i] == TYPE_LONG) {
					types[i] = KEY;
					break;
				}
			}
		} else if (importType == NETWORK_IMPORT) {
			// Try to find good candidates for source/target node attributes
			for (int i = 0; i < types.length; i++) {
				if (types[i] == ATTR) { // Hasn't been chosen as KEY, SOURCE, TARGET or INTERACTION yet...
					// TODO
				}
			}
		}

		return types;
	}
	
	public static AttributeDataType[] guessDataTypes(final TableModel model) {
		// 0 = Boolean,  1 = Integer,  2 = Double,  3 = String
		final int[][] typeChecker = new int[4][model.getColumnCount()];
		String cell = null;

		final int rowCount = Math.min(1000, model.getRowCount());
		
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				cell = (String) model.getValueAt(i, j);
				boolean found = false;

				if (cell != null) { 
					// boolean
					if (truePattern.matcher(cell).matches() || falsePattern.matcher(cell).matches()) {
						typeChecker[0][j]++;
						found = true;
					} else {
						// integers
						try {
							Integer.valueOf(cell);
							typeChecker[1][j]++;
							found = true;
						} catch (NumberFormatException e) {
						}
			
						// floats
						try {
							Double.valueOf(cell);
							typeChecker[2][j]++;
							found = true;
						} catch (NumberFormatException e) {
						}
					}
				}
				
				// default to string
				if (found == false) {
					// TODO: try to detect List types by 
					typeChecker[3][j]++;
				}
			}
		}

		final AttributeDataType[] dataTypes = new AttributeDataType[model.getColumnCount()];

		for (int i = 0; i < dataTypes.length; i++) {
			int maxVal = 0;
			int maxIndex = 0;

			for (int j = 0; j < 4; j++) {
				if (maxVal < typeChecker[j][i]) {
					maxVal = typeChecker[j][i];
					maxIndex = j;
				}
			}
	
			if (maxIndex == 0)
				dataTypes[i] = TYPE_BOOLEAN;
			else if (maxIndex == 1)
				dataTypes[i] = TYPE_INTEGER;
			else if (maxIndex == 2)
				dataTypes[i] = TYPE_FLOATING;
			else
				dataTypes[i] = TYPE_STRING;
		}

		return dataTypes;
	}
	
	/**
	 * Returns true if columns of the passed column type can have duplicate names in the source file or table.
	 * @param types 
	 */
	public static boolean allowsDuplicateName(final ImportType importType, final SourceColumnSemantic type1,
			SourceColumnSemantic type2) {
		boolean b = type1 == NONE || type2 == NONE;
		
		if (importType == NETWORK_IMPORT) {
			b = b || (type1 == SOURCE_ATTR && type2 != SOURCE_ATTR && type2 != SOURCE && type2 != TARGET);
			b = b || (type2 == SOURCE_ATTR && type1 != SOURCE_ATTR && type1 != SOURCE && type1 != TARGET);
			b = b || (type1 == TARGET_ATTR && type2 != TARGET_ATTR && type2 != SOURCE && type2 != TARGET);
			b = b || (type2 == TARGET_ATTR && type1 != TARGET_ATTR && type1 != SOURCE && type1 != TARGET);
			b = b || (type1 == EDGE_ATTR && type2 != EDGE_ATTR && type2 != INTERACTION);
			b = b || (type2 == EDGE_ATTR && type1 != EDGE_ATTR && type1 != INTERACTION);
		}
		
		return b;
	}
	
	public static boolean isValid(final SourceColumnSemantic type, final AttributeDataType dataType) {
		if (type == KEY || type == SOURCE || type == TARGET)
			return dataType == TYPE_INTEGER || dataType == TYPE_LONG || dataType == TYPE_STRING;
		
		if (type == INTERACTION || type == ONTOLOGY || type == TAXON)
			return dataType == TYPE_STRING;
		
		return true;
	}
	
	private static boolean matches(String name, final String[] preferredNames, final boolean exact) {
		// Remove all special chars and spaces from column name
		name = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
		
		PREFERRED_NAMES:
		for (String s : preferredNames) {
			if (exact) {
				s = s.replaceAll(" ", "");
				
				if (name.equalsIgnoreCase(s))
					return true;
			} if (!exact) {
				final String[] tokens = s.split(" ");
				boolean b = false;
				
				for (final String t : tokens) {
					b = b && name.contains(t.toLowerCase());
					
					if (!b)
						continue PREFERRED_NAMES;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean canBeKey(final TableModel model, final int col, final AttributeDataType dataType) {
		if (dataType != TYPE_STRING && dataType != TYPE_INTEGER && dataType != TYPE_LONG)
			return false;
		
		final int rowCount = Math.min(1000, model.getRowCount());
		final Set<Object> values = new HashSet<>();
		
		for (int row = 0; row < rowCount; row++) {
			final Object val = model.getValueAt(row, col);
			
			if (val == null)
				return false;
			
			if (dataType == TYPE_STRING) {
				final String s = val.toString();
				
				if (values.contains(s))
					return false;
				
				values.add(s);
			} else {
				try {
					final Long n = Long.parseLong(val.toString());
					
					if (values.contains(n))
						return false;
					
					values.add(n);
				} catch (NumberFormatException e) {
					return false;
				}
			}
		}
		
		return true;
	}
}
