package org.cytoscape.tableimport.internal.util;

import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.tableimport.internal.ui.PreviewTablePanel.PreviewTableModel;

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

public final class TypeUtil {

	/**
	 * Default value for Interaction edge attribute.
	 */
	public static final String DEFAULT_INTERACTION = "interacts with";
	
	private static final List<SourceColumnSemantic> TABLE_IMPORT_TYPES = Arrays.asList(
			NONE, KEY, ATTR
	);
	private static final List<SourceColumnSemantic> NETWORK_IMPORT_TYPES = Arrays.asList(
			NONE, SOURCE, INTERACTION, TARGET, EDGE_ATTR, SOURCE_ATTR, TARGET_ATTR
	);
	private static final List<SourceColumnSemantic> ONTOLOGY_IMPORT_TYPES = Arrays.asList(
			NONE, KEY, ALIAS, ONTOLOGY, TAXON, ATTR
	);
	
	private static final List<String> NAMESPACES = Arrays.asList(
			CyNetwork.LOCAL_ATTRS, CyNetwork.DEFAULT_ATTRS
	);
	
	private static final String[] PREF_KEY_NAMES = new String[] {
		"shared name", "name", "identifier", "id", "key", "names", "identifiers", "ids", "keys",
		"node", "node id", "node key", "edge", "edge id", "edge key",
		"nodes", "node ids", "node keys", "edges", "edge ids", "edge keys",
		"gene", "gene id", "gene name", "protein",
		"genes", "gene ids", "gene names", "proteins"
	};
	private static final String[] PREF_SOURCE_NAMES = new String[] {
		"source", "source node", "source name", "source id", "source identifier",
		"node 1", "node a", "identifier 1", "identifier a", "id 1", "id a",
		"key 1", "key a",
		"source shared name", "name 1", "name a", "shared name 1", "shared name a",
		"source gene", "gene 1", "gene1", "gene a", "genea", "gene id 1", "gene name 1", "id interactor a",
		"name", "shared name", "node", "gene", "gene id", "gene name", "id", "identifier"
	};
	private static final String[] PREF_TARGET_NAMES = new String[] {
		"target", "target node", "target name", "target id", "target identifier",
		"node 2", "node b", "identifier 2", "identifier b", "id 2", "id b",
		"key 2", "key b",
		"target shared name", "name 2", "name b", "shared name 2", "shared name b",
		"target gene", "gene 2", "gene2", "gene b", "geneb", "gene id 2", "gene name 2", "id interactor b"
	};
	private static final String[] PREF_INTERACTION_NAMES = new String[] {
		"interaction", "interaction type", "interaction types", "edge type", "edge types",
		"interaction id", "interaction identifier",
		"type", "evidence"
	};
	private static final String[] PREF_ONTOLOGY_NAMES = new String[] {
		"gene ontology", "ontology", "go"
	};
	private static final String[] PREF_TAXON_NAMES = new String[] {
		"taxon", "tax id", "taxonomy", "organism"
	};
	
	private static Pattern truePattern = Pattern.compile("^true$", Pattern.CASE_INSENSITIVE);
	private static Pattern falsePattern = Pattern.compile("^false$", Pattern.CASE_INSENSITIVE);
	
	/** Keeps the preferred namespace of regular nodes/edges attributes */
	private static String preferredNamespace = CyNetwork.DEFAULT_ATTRS;
	
	private TypeUtil() {}
	
	public static List<SourceColumnSemantic> getAvailableTypes(ImportType importType) {
		if (importType == NETWORK_IMPORT) return NETWORK_IMPORT_TYPES;
		if (importType == ONTOLOGY_IMPORT) return ONTOLOGY_IMPORT_TYPES;
		
		return TABLE_IMPORT_TYPES;
	}
	
	public static List<String> getAvailableNamespaces(ImportType importType) {
		return importType == NETWORK_IMPORT ? NAMESPACES : Collections.emptyList();
	}
	
	public static SourceColumnSemantic getDefaultType(ImportType importType) {
		return importType == NETWORK_IMPORT ? EDGE_ATTR : ATTR;
	}
	
	public static SourceColumnSemantic[] guessTypes(
			ImportType importType,
			TableModel model,
			AttributeDataType[] dataTypes,
			Set<SourceColumnSemantic> ignoredTypes
	) {
		int size = model.getColumnCount();
		
		var types = new SourceColumnSemantic[size];
		
		if (importType == NETWORK_IMPORT)
			Arrays.fill(types, EDGE_ATTR);
		else
			Arrays.fill(types, ATTR);
		
		if (dataTypes == null || dataTypes.length == 0 || dataTypes.length != model.getColumnCount())
			return types;
		
		boolean srcFound = ignoredTypes != null && ignoredTypes.contains(SOURCE);
		boolean tgtFound = ignoredTypes != null && ignoredTypes.contains(TARGET);
		boolean interactFound = ignoredTypes != null && ignoredTypes.contains(INTERACTION);
		boolean keyFound = ignoredTypes != null && ignoredTypes.contains(KEY);
		boolean goFound = ignoredTypes != null && ignoredTypes.contains(ONTOLOGY);
		boolean taxFound = ignoredTypes != null && ignoredTypes.contains(TAXON);

		// First pass: Look for exact column name
		// Second pass: Select column whose name contains one of the tokens
		MAIN_LOOP:
		for (int attempt = 0; attempt < 2; attempt++) {
			boolean exact = attempt == 0;
			
			for (int i = 0; i < size; i++) {
				var name = model.getColumnName(i);
				var dataType = dataTypes[i];
				
				if (attempt == 0) {
					if (name.isBlank()
							|| CyIdentifiable.SUID.equalsIgnoreCase(name)
							|| name.endsWith(".SUID")
							|| CyNetwork.SELECTED.equalsIgnoreCase(name)) {
						// Empty/blank, SUID and 'selected' columns are ignored by default
						types[i] = NONE;
					} else if (importType == NETWORK_IMPORT) {
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
					}
				} else if (!keyFound) { // Second attempt and we haven't found a key column yet...
					if (types[i] != NONE && canBeKey(model, i, dataType)) {
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
				var name = model.getColumnName(i);
				
				if (CyIdentifiable.SUID.equalsIgnoreCase(name)) // Columns called SUID are ignored by default
					continue;
				
				if (dataTypes[i] == TYPE_STRING || dataTypes[i] == TYPE_INTEGER || dataTypes[i] == TYPE_LONG) {
					types[i] = KEY;
					break;
				}
			}
		} else if (importType == NETWORK_IMPORT) {
			// Try to find good candidates for source/target node attributes
			for (int i = 0; i < types.length; i++) {
				if (types[i] == EDGE_ATTR) {
					// Hasn't been chosen as KEY, SOURCE, TARGET, INTERACTION, SOURCE_ATTR or TARGET_ATTR  yet...
					for (int j = 0; j < types.length; j++) {
						// For now, simply look for columns with same name
						if (i != j && types[j] == EDGE_ATTR && model.getColumnName(i).equals(model.getColumnName(j))) {
							types[i] = SOURCE_ATTR;
							types[j] = TARGET_ATTR;
							break;
						}
					}
				}
			}
		}

		return types;
	}
	
	public static AttributeDataType[] guessSheetDataTypes(PreviewTableModel model, Character decimalSeparator) {
		if (!model.hasPredefinedTypes())
			return guessDataTypes(model, decimalSeparator);
		
		// TODO LIST?

		var dataTypes = new AttributeDataType[model.getColumnCount()];
		
		for (int col = 0; col < model.getColumnCount(); col++) {
			var predefinedClass = model.getPredefinedColumnClass(col);
			
			if (predefinedClass == Double.class) {
				dataTypes[col] = TYPE_FLOATING;
			} else if (predefinedClass == Long.class) {
				dataTypes[col] = TYPE_LONG;
			} else if (predefinedClass == Integer.class) {
				dataTypes[col] = TYPE_INTEGER;
			} else if (predefinedClass == Boolean.class) {
				dataTypes[col] = TYPE_BOOLEAN;
			} else {
				dataTypes[col] = TYPE_STRING;
			}
		}
		
		return dataTypes;
	}
	
	public static AttributeDataType[] guessDataTypes(TableModel model, Character decimalSeparator) {
		var dataTypes = new AttributeDataType[model.getColumnCount()];
		int rowCount = Math.min(1000, model.getRowCount());
		
		COLUMN_LOOP:
		for (int col = 0; col < model.getColumnCount(); col++) {
			var dt = dataTypes[col];
			
			for (int row = 0; row < rowCount; row++) {
				var val = (String) model.getValueAt(row, col);
				
				if (val == null || val.isEmpty() || val.equals("null"))
					continue;

				if (dt == TYPE_STRING || dt == TYPE_STRING_LIST) {
					// If type detected as String, it can't be a number or boolean anymore...
					// TODO: Try to detect List types
					continue COLUMN_LOOP;
				}
				
				if (dt == null) {
					// Data Type not detected yet, so try everything (the order is important)...
					if (isBoolean(val))
						dt = TYPE_BOOLEAN;
					else if (isInteger(val))
						dt = TYPE_INTEGER;
					else if (isLong(val))
						dt = TYPE_LONG;
					else if (isDouble(val, decimalSeparator))
						dt = TYPE_FLOATING;
					else
						dt = TYPE_STRING; // Defaults to String!
				} else {
					// Previously detected as boolean?
					if (dt == TYPE_BOOLEAN) {
						// Just make sure the other rows are also compatible with boolean values...
						if (!isBoolean(val)) {
							// This row does not contain a boolean, so the column has to be a String
							dt = TYPE_STRING;
							break;
						}
					} else if (dt == TYPE_INTEGER) {
						// Make sure the other rows are also integers...
						if (!isInteger(val)) {
							if (isLong(val))
								dt = TYPE_LONG;
							else if (isDouble(val, decimalSeparator))
								dt = TYPE_FLOATING;
							else
								dt = TYPE_STRING;
						}
					} else if (dt == TYPE_LONG) {
						// Make sure the other rows are also longs (no need to check for integers anymore)...
						if (!isLong(val)) {
							if (isDouble(val, decimalSeparator))
								dt = TYPE_FLOATING;
							else
								dt = TYPE_STRING;
						}
					} else if (dt == TYPE_FLOATING) {
						// Make sure the other rows are also doubles (no need to check for other numeric types)...
						if (!isDouble(val, decimalSeparator))
							dt = TYPE_STRING;
					}
				}
			}
			
			dataTypes[col] = dt;
		}
		
		// Non detected types default to String
		for (int i = 0; i < dataTypes.length; i++) {
			if (dataTypes[i] == null)
				dataTypes[i] = TYPE_STRING;
		}

		return dataTypes;
	}
	
	public static SourceColumnSemantic[] parseColumnTypeList(String strList) {
		var typeList = new ArrayList<SourceColumnSemantic>();
		
		if (strList != null) {
			var tokens = getCSV(strList);
			
			for (var t : tokens) {
				var s = t.trim().toLowerCase().replaceAll("[^a-zA-Z]", "");
				final SourceColumnSemantic dataType;

				switch(s) {
					case "s":
					case "source":
					case "source node":
						dataType = SourceColumnSemantic.SOURCE;
						break;
					case "t":
					case "target":
					case "target node":
						dataType = SourceColumnSemantic.TARGET;
						break;
					case "i":
					case "interaction":
						dataType = SourceColumnSemantic.INTERACTION;
						break;
					case "sa":
					case "source attribute":
						dataType = SourceColumnSemantic.SOURCE_ATTR;
						break;
					case "ta":
					case "target attribute":
						dataType = SourceColumnSemantic.TARGET_ATTR;
						break;
					case "ea":
					case "edge attribute":
						dataType = SourceColumnSemantic.EDGE_ATTR;
						break;
					case "x":
					case "skip":
					case "none":
						dataType = SourceColumnSemantic.NONE;
						break;
					default:
						throw new IllegalArgumentException("Invalid Column Type: \"" + t + "\"");
				}
				
				typeList.add(dataType);
			}
		}
		
		return typeList.toArray(new SourceColumnSemantic[typeList.size()]);
	}

	public static AttributeDataType[] parseDataTypeList(String strList) {
		var dataTypeList = new ArrayList<AttributeDataType>();
		
		if (strList != null) {
			var tokens = getCSV(strList);
			
			for (var t : tokens) {
				var s = t.trim().toLowerCase().replaceAll("[^a-zA-Z]", "");
				final AttributeDataType dataType;
				
				switch(s) {
					case "i":
					case "int":
					case "integer":
						dataType = AttributeDataType.TYPE_INTEGER;
						break;
					case "l":
					case "long":
					case "longinteger":
						dataType = AttributeDataType.TYPE_LONG;
						break;
					case "f":
					case "d":
					case "float":
					case "floating":
					case "floatingpoint":
					case "decimal":
					case "double":
						dataType = AttributeDataType.TYPE_FLOATING;
						break;
					case "b":
					case "bool":
					case "boolean":
						dataType = AttributeDataType.TYPE_BOOLEAN;
						break;
					case "s":
					case "t":
					case "text":
					case "string":
						dataType = AttributeDataType.TYPE_STRING;
						break;
					case "ii":
					case "li":
					case "il":
					case "listint":
					case "listinteger":
					case "intlist":
					case "integerlist":
						dataType = AttributeDataType.TYPE_INTEGER_LIST;
						break;
					case "ll":
					case "listlong":
					case "listlonginteger":
					case "longlist":
					case "longintegerlist":
						dataType = AttributeDataType.TYPE_LONG_LIST;
						break;
					case "ff":
					case "dd":
					case "lf":
					case "fl":
					case "ld":
					case "dl":
					case "listfloat":
					case "listfloating":
					case "listfloatingpoint":
					case "listdecimal":
					case "listdouble":
					case "floatlist":
					case "floatinglist":
					case "floatingpointlist":
					case "decimallist":
					case "doublelist":
						dataType = AttributeDataType.TYPE_FLOATING_LIST;
						break;
					case "bb":
					case "lb":
					case "bl":
					case "listbool":
					case "listboolean":
					case "boollist":
					case "booleanlist":
						dataType = AttributeDataType.TYPE_BOOLEAN_LIST;
						break;
					case "ss":
					case "ls":
					case "sl":
					case "lt":
					case "tl":
					case "liststring":
					case "listtext":
					case "stringlist":
					case "textlist":
						dataType = AttributeDataType.TYPE_STRING_LIST;
						break;
					default:
						throw new IllegalArgumentException("Invalid Data Type: \"" + t + "\"");
				}
				
				dataTypeList.add(dataType);
			}
		}
		
		return dataTypeList.toArray(new AttributeDataType[dataTypeList.size()]);
	}
	
	public static String[] getPreferredNamespaces(SourceColumnSemantic[] types) {
		var namespaces = types != null ? new String[types.length] : null;
		
		if (namespaces != null) {
			for (int i = 0; i < types.length; i++) {
				var t = types[i];
				namespaces[i] = getPreferredNamespace(t);
			}
		}
		
		return namespaces;
	}
	
	public static String getPreferredNamespace(SourceColumnSemantic type) {
		if (type == null)
			return null;
		
		// PKs (source, target, etc) must always be local columns!
		return type.isUnique() ? CyNetwork.LOCAL_ATTRS : preferredNamespace;
	}
	
	public static void setPreferredNamespace(String preferredNamespace) {
		TypeUtil.preferredNamespace = preferredNamespace;
	}
	
	private static boolean isBoolean(String val) {
		return val != null && (truePattern.matcher(val).matches() || falsePattern.matcher(val).matches());
	}
	
	private static boolean isNaN(String val) {
		if (val != null)
			return val.equals("NA") || val.equals("#NUM!") || val.equals("NaN");
		
		return false;
	}

	private static boolean isInteger(String val) {
		if (val != null) {
			if (isNaN(val))
				return true;
			
			try {
				long n = Long.parseLong(val.trim());
				return n <= Integer.MAX_VALUE && n >= Integer.MIN_VALUE;
			} catch (NumberFormatException e) {
			}
		}
		
		return false;
	}
	
	private static boolean isLong(String val) {
		if (val != null) {
			if (isNaN(val))
				return true;
			
			try {
				Long.parseLong(val.trim());
				return true;
			} catch (NumberFormatException e) {
			}
		}
		
		return false;
	}
	
	private static boolean isDouble(String val, Character decimalSeparator) {
		if (val != null) {
			val = val.trim();
			
			if (isNaN(val))
				return true;
//			try {
//				Double.parseDouble(val);
//			} catch (NumberFormatException e) {
//				return false;
//			}
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator(decimalSeparator.charValue());
			dfs.setExponentSeparator("E");

			DecimalFormat df = new DecimalFormat();
			df.setDecimalFormatSymbols(dfs);
			df.setGroupingUsed(false); // We don't use the grouping

			ParsePosition parsePosition = new ParsePosition(0);

			// DecimalFormat parse doesn't accept 'e'
			String val1 = val.replace('e', 'E');
			df.parse(val1, parsePosition);
			if(parsePosition.getIndex() != val1.length()) {
				return false;
			}
			
			// Also check if it ends with 'f' or 'd' (if so, it should be a String!)
			val1 = val1.toLowerCase();
			return !val1.endsWith("f") && !val1.endsWith("d");
		}
		
		return false;
	}

	/**
	 * Returns true if columns of the passed column type can have duplicate names in the source file or table.
	 * @param types 
	 */
	public static boolean allowsDuplicateName(ImportType importType, SourceColumnSemantic type1,
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
	
	public static boolean isValid(SourceColumnSemantic type, AttributeDataType dataType) {
		if (type == KEY || type == SOURCE || type == TARGET)
			return dataType == TYPE_INTEGER || dataType == TYPE_LONG || dataType == TYPE_STRING;
		
		if (type == INTERACTION || type == ONTOLOGY || type == TAXON)
			return dataType == TYPE_STRING;
		
		return true;
	}
	
	public static boolean isValid(SourceColumnSemantic type, String namespace) {
		if (type == NONE)
			return false;
		
		if (type == KEY || type == SOURCE || type == TARGET)
			return namespace == CyNetwork.LOCAL_ATTRS;
		
		if (type == INTERACTION || type == ONTOLOGY || type == TAXON)
			return namespace == CyNetwork.LOCAL_ATTRS;
		
		return true;
	}
	
	private static boolean matches(String name, String[] preferredNames, boolean exact) {
		// Remove all special chars and spaces from column name
		name = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
		
		PREFERRED_NAMES:
		for (var s : preferredNames) {
			if (exact) {
				s = s.replaceAll(" ", "");
				
				if (name.equalsIgnoreCase(s))
					return true;
			} if (!exact) {
				var tokens = s.split(" ");
				boolean b = false;
				
				for (var t : tokens) {
					b = b && name.contains(t.toLowerCase());
					
					if (!b)
						continue PREFERRED_NAMES;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean canBeKey(TableModel model, int col, AttributeDataType dataType) {
		if (dataType != TYPE_STRING && dataType != TYPE_INTEGER && dataType != TYPE_LONG)
			return false;
		
		int rowCount = Math.min(1000, model.getRowCount());
		var values = new HashSet<Object>();
		
		for (int row = 0; row < rowCount; row++) {
			var val = model.getValueAt(row, col);
			
			if (val == null)
				return false;
			
			if (dataType == TYPE_STRING) {
				var s = val.toString();
				
				if (values.contains(s))
					return false;
				
				values.add(s);
			} else {
				try {
					Long n = Long.parseLong(val.toString());
					
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
	
	public static String[] getCSV(String str) {
		// Split the string, but allow for protected commas
		String [] s1 = str.split("(?<!\\\\),");
		
		// Now replace any backslashes with nothing.
		for (int index = 0; index < s1.length; index++) {
			String s = s1[index];
			s1[index] = s.replaceAll("\\\\", "");
		}
		
		return s1;
	}
}
