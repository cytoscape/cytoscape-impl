package org.cytoscape.webservice.psicquic.mapper;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.webservice.psicquic.miriam.Miriam;
import org.cytoscape.webservice.psicquic.miriam.Miriam.Datatype;
import org.cytoscape.webservice.psicquic.miriam.Synonyms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class InteractionClusterMapper {

	private static final Logger logger = LoggerFactory.getLogger(InteractionClusterMapper.class);

	private static final Pattern SPLITTER = Pattern.compile("\\|");
	private static final Pattern SPLITTER_NAME_SPACE = Pattern.compile("\\:");
	private static final Pattern SPLITTER_TYPE = Pattern.compile("\\(");

	private static final String SCHEMA_NAMESPACE = "org.cytoscape.webservice.psicquic.miriam";

	public static final String PREDICTED_GENE_NAME = "Human Readable Label";
	public static final String INTERACTOR_TYPE = "Interactor Type";
	
	static final String PUB_ID = "Publication ID";
	static final String PUB_DB = "Publication DB";

	static final String AUTHOR = "Author";
	public static final String INTERACTION_TYPE = "Interaction Type";
	public static final String PRIMARY_INTERACTION_TYPE = "Primary Interaction Type";

	static final String SOURCE_DB = "Source Database";

	static final String DETECTION_METHOD_ID = "Detection Method ID";
	public static final String DETECTION_METHOD_NAME = "Detection Method";

	private final static Pattern exact1Pattern = Pattern.compile("^[A-Z][A-Z][A-Z]\\d");
	private final static Pattern ncbiPattern = Pattern.compile("^[A-Za-z].+");
	private final static Pattern uniprotPattern = Pattern.compile("^[a-zA-Z]\\d.+");

	private static final String ENTREZ_GENE_ATTR_NAME = "entrez gene/locuslink";
	private static final String UNIPROT_ATTR_NAME = "uniprot";
	private static final String STRING_ATTR_NAME = "string";

	private final Set<String> namespaceSet;
	private final Map<String, String> name2ns;
	private final Map<String, String> synonym2ns;

	public static final String TAXNOMY = "Taxonomy ID";
	static final String TAXNOMY_NAME = "Taxonomy Name";

	boolean isInitialized = false;

	private String currentGeneName = null;

	// Flag to indicates edge is self-interaction or not.
	private boolean isSelfEdge = false;

	public InteractionClusterMapper() {
		namespaceSet = new HashSet<String>();
		this.name2ns = new HashMap<String, String>();
		this.synonym2ns = new HashMap<String, String>();
	}

	void ensureInitialized() {
		synchronized (this) {
			if (isInitialized) {
				return;
			}
			// Read resource file
			try {
				parseXml();
			} catch (Exception ex) {
				throw new RuntimeException("Could not read resource file", ex);
			}
			isInitialized = true;
		}
	}

	/**
	 * Process one entry in MITAB cell.
	 * 
	 * 
	 * @return String array of result. 1: namespace, 2: value, and 3:
	 *         description.
	 */
	public final String[] parseValues(final String entry) {
		final String[] values = new String[3];

		// Extract name space
		final String[] parts = SPLITTER_NAME_SPACE.split(entry);
		values[0] = parts[0];

		// Parse ID value
		if (parts.length >= 2) {
			final String others = entry.substring(values[0].length() + 1, entry.length());
			final String[] valParts = SPLITTER_TYPE.split(others);
			final String newVal = valParts[0].replaceAll("\"", "");
			values[1] = newVal;
			if (valParts.length >= 2) {
				// Parse description
				values[2] = valParts[1].substring(0, valParts[1].length() - 1).replaceAll("\"", "");
			}
		} else {
			return values;
		}

		return values;
	}

	/**
	 * Extract primary ID sets.
	 * 
	 * @param nameText
	 * @return
	 */
	private final Map<String, String> createNames(String nameText) {
		final Map<String, String> map = new HashMap<String, String>();
		final String[] names = SPLITTER.split(nameText);

		for (final String name : names) {
			// Ignore invalid line
			if (name.equals("-")) {
				this.isSelfEdge = true;
				continue;
			}
			final String[] parts = parseValues(name);
			// final String[] parts = SPLITTER_NAME_SPACE.split(name);
			map.put(parts[0], parts[1]);
		}

		return map;
	}

	private Map<String, List<String>> createOtherNames(final String nameText, final String aliases) {
		currentGeneName = null;

		final Map<String, List<String>> map = new HashMap<String, List<String>>();
		final String[] names = SPLITTER.split(nameText);
		final String[] others = SPLITTER.split(aliases);
		final List<String[]> entries = new ArrayList<String[]>();
		entries.add(names);
		entries.add(others);

		for (final String[] entry : entries) {
			for (final String name : entry) {
				final String[] parsed = parseValues(name);
				List<String> list = map.get(parsed[0]);
				if (list == null) {
					list = new ArrayList<String>();
				}
				if (parsed[1] != null) {
					list.add(parsed[1]);

					if (parsed[2] != null && parsed[2].equals("gene name")) {
						currentGeneName = parsed[1];
					}
				} else {
					continue;
				}
				map.put(parsed[0], list);
			}
		}

		return map;
	}

	public void mapNodeColumn(final String[] entries, final CyRow sourceRow, final CyRow targetRow) {
		this.isSelfEdge = false;

		// Primary ID sets
		final Map<String, String> accsSource = createNames(entries[0]);
		processNames(sourceRow, accsSource);

		// ALT and Aliases
		final Map<String, List<String>> otherSource = createOtherNames(entries[2], entries[4]);
		processOtherNames(sourceRow, otherSource);

		if (currentGeneName != null)
			sourceRow.set(PREDICTED_GENE_NAME, currentGeneName);
		else {
			guessHumanReadableName(sourceRow);
		}
		setSpecies(entries[9], sourceRow);

		if (!isSelfEdge) {
			final Map<String, String> accsTarget = createNames(entries[1]);
			processNames(targetRow, accsTarget);
			final Map<String, List<String>> otherTarget = createOtherNames(entries[3], entries[5]);
			processOtherNames(targetRow, otherTarget);
			if (currentGeneName != null)
				targetRow.set(PREDICTED_GENE_NAME, currentGeneName);
			else {
				guessHumanReadableName(targetRow);
			}
			setSpecies(entries[10], targetRow);
		}

		// For 2.7 data
		if(entries.length>15) {
//			addListColumn(sourceRow, entries[16], "Biological Role", String.class);
//			addListColumn(targetRow, entries[17], "Biological Role", String.class);
//			
//			addListColumn(sourceRow, entries[18], "Experimental Role", String.class);
//			addListColumn(targetRow, entries[19], "Experimental Role", String.class);

			addListColumn(sourceRow, entries[20], INTERACTOR_TYPE, String.class);
			addListColumn(targetRow, entries[21], INTERACTOR_TYPE, String.class);
			
			addListColumn(sourceRow, entries[22], "Xref", String.class);
			addListColumn(targetRow, entries[23], "Xref", String.class);
			
			addSimpleListColumn(sourceRow, entries[25], "Annotations");
			addSimpleListColumn(targetRow, entries[26], "Annotations");
			
			addSimpleListColumn(sourceRow, entries[36], "Features");
			addSimpleListColumn(targetRow, entries[38], "Features");
			
//			addListColumn(sourceRow, entries[40], "Participant Detection Method", String.class);
//			addListColumn(targetRow, entries[41], "Participant Detection Method", String.class);
		}
	}


	private final void setSpecies(final String speciesText, CyRow row) {
		// Pick first entry only.
		final String[] entries = SPLITTER.split(speciesText);
		final String[] values = parseValues(entries[0]);

		if (values[1] != null) {
			row.set(TAXNOMY, values[1]);
		}

		if (values[2] != null) {
			row.set(TAXNOMY_NAME, values[2]);
		}
	}

	public void mapNodeColumn(final EncoreInteraction interaction, final CyRow sourceRow, final CyRow targetRow) {

		final Map<String, String> accsSource = interaction.getInteractorAccsA();
		processNames(sourceRow, accsSource);
		final Map<String, List<String>> otherSource = interaction.getOtherInteractorAccsA();
		processOtherNames(sourceRow, otherSource);
		final Collection<CrossReference> speciesSource = interaction.getOrganismsA();
		// Add Species names
		if (speciesSource.size() != 0) {
			CrossReference speciesSourceFirst = speciesSource.iterator().next();
			processSpecies(sourceRow, speciesSourceFirst);
		}
		// Try to find human-readable gene name
		guessHumanReadableName(sourceRow);

		if (targetRow == null) {
			return;
		}

		// If target exists...
		final Map<String, String> accsTarget = interaction.getInteractorAccsB();
		processNames(targetRow, accsTarget);
		final Map<String, List<String>> otherTarget = interaction.getOtherInteractorAccsB();
		processOtherNames(targetRow, otherTarget);
		final Collection<CrossReference> speciesTarget = interaction.getOrganismsB();
		if (speciesTarget.size() != 0) {
			CrossReference speciesTargetFirst = speciesTarget.iterator().next();
			processSpecies(targetRow, speciesTargetFirst);
		}
		guessHumanReadableName(targetRow);
	}

	public void mapEdgeColumn(final EncoreInteraction interaction, final CyRow row) {

		final Set<String> exp = interaction.getExperimentToPubmed().keySet();
		row.set(DETECTION_METHOD_ID, new ArrayList<String>(exp));

		final List<CrossReference> pubIDs = interaction.getPublicationIds();
		final List<String> pubIdList = new ArrayList<String>();
		final List<String> pubDBList = new ArrayList<String>();
		for (CrossReference pub : pubIDs) {
			pubIdList.add(pub.getIdentifier());
			pubDBList.add(pub.getDatabase());
		}
		if (pubIdList.isEmpty() == false)
			row.set(PUB_ID, pubIdList);
		if (pubDBList.isEmpty() == false)
			row.set(PUB_DB, pubDBList);

		// Interaction (use UniqueID)
		row.set(CyEdge.INTERACTION, interaction.getMappingIdDbNames());

		final List<Confidence> scores = interaction.getConfidenceValues();
		for (Confidence c : scores) {
			String type = c.getType();
			String value = c.getValue();

			if (row.getTable().getColumn(type) == null)
				row.getTable().createColumn(type, Double.class, true);

			try {
				double doubleVal = Double.parseDouble(value);
				row.set(type, doubleVal);
			} catch (NumberFormatException e) {
				// logger.warn("Invalid number string: " + value);
				// Ignore invalid number
			}

		}
	}

	private void processNames(CyRow row, final Map<String, String> accs) {
		for (String originalDBName : accs.keySet()) {

			final String dbName = validateNamespace(originalDBName);

			if (row.getTable().getColumn(dbName) == null)
				row.getTable().createListColumn(dbName, String.class, true);

			List<String> currentList = row.getList(dbName, String.class);
			if (currentList == null)
				currentList = new ArrayList<String>();

			final Set<String> nameSet = new HashSet<String>(currentList);

			final String entry = accs.get(originalDBName);
			nameSet.add(entry);

			row.set(dbName, new ArrayList<String>(nameSet));
		}
	}

	private void processOtherNames(CyRow row, final Map<String, List<String>> accs) {
		for (String originalDBName : accs.keySet()) {

			final String dbName = validateNamespace(originalDBName);

			if (row.getTable().getColumn(dbName) == null)
				row.getTable().createListColumn(dbName, String.class, false);

			List<String> currentList = row.getList(dbName, String.class);
			if (currentList == null)
				currentList = new ArrayList<String>();

			final Set<String> nameSet = new HashSet<String>(currentList);
			final List<String> names = accs.get(originalDBName);
			nameSet.addAll(names);
			row.set(dbName, new ArrayList<String>(nameSet));
		}
	}

	private void processSpecies(CyRow row, CrossReference ref) {
		if (ref != null) {
			final String name = ref.getText();
			final String speciesID = ref.getIdentifier();

			row.set(TAXNOMY, speciesID);
			row.set(TAXNOMY_NAME, name);
		}
	}

	private Miriam parseXml() throws IOException {

		final URL xml = CyNetworkBuilder.class.getClassLoader().getResource("MiriamResources_all.xml");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(xml.openStream()));

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(SCHEMA_NAMESPACE, getClass().getClassLoader());
		} catch (JAXBException e) {
			logger.error("Could not create JAXBContext", e);
		}

		Unmarshaller u = null;
		try {
			u = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.error("Could not create Unmarshaller", e);
		}

		Miriam result = null;
		try {
			result = (Miriam) u.unmarshal(reader);
		} catch (JAXBException e) {
			logger.error("unmarshal operation failed", e);
		}

		List<Datatype> dataTypes = result.getDatatype();
		for (Datatype type : dataTypes) {
			final String ns = type.getNamespace().toLowerCase();
			namespaceSet.add(ns);
			name2ns.put(type.getName().toLowerCase(), ns);
			final Synonyms sym = type.getSynonyms();
			if (sym != null) {
				for (String s : sym.getSynonym()) {
					synonym2ns.put(s.toLowerCase(), ns);
				}
			}
		}
		return result;
	}

	private String validateNamespace(final String columnName) {

		// This is a hack for db's bug
		if (columnName.equals("entrezgene/locuslink"))
			return "entrez gene/locuslink";

		if (namespaceSet.contains(columnName.toLowerCase()))
			return columnName;

		String newName = name2ns.get(columnName.toLowerCase());
		if (newName != null)
			return newName;

		newName = synonym2ns.get(columnName.toLowerCase());
		if (newName != null)
			return newName;

		return columnName;
	}

	private void guessHumanReadableName(final CyRow row) {
		boolean found = false;

		// Special handler for STRING. This is a hack...
		if (row.getTable().getColumn(STRING_ATTR_NAME) != null) {
			final List<String> stringList = row.getList(STRING_ATTR_NAME, String.class);
			if (stringList != null)
				found = findHumanReadableName(row, stringList, ncbiPattern, true);
		}

		if (found)
			return;

		// try NCBI
		if (row.getTable().getColumn(ENTREZ_GENE_ATTR_NAME) != null) {
			final List<String> ncbiList = row.getList(ENTREZ_GENE_ATTR_NAME, String.class);
			if (ncbiList != null)
				found = findHumanReadableName(row, ncbiList, ncbiPattern, true);
		}
		if (found)
			return;

		// Try Uniprot
		List<String> uniprotList = null;
		if (row.getTable().getColumn(UNIPROT_ATTR_NAME) != null) {
			uniprotList = row.getList(UNIPROT_ATTR_NAME, String.class);
			if (uniprotList != null)
				found = findHumanReadableName(row, uniprotList, exact1Pattern, true);
		}
		if (found)
			return;

		if (uniprotList != null)
			found = findHumanReadableName(row, uniprotList, uniprotPattern, false);

		if (found)
			return;

		// Unknown
		if (row.getTable().getColumn("unknown") != null) {
			final List<String> unknownList = row.getList("unknown", String.class);
			if (unknownList != null)
				found = findHumanReadableName(row, unknownList, uniprotPattern, false);
		}
		if (found)
			return;

		if (found == false) {
			// Give up. Use primary key
			row.set(PREDICTED_GENE_NAME, row.get(CyNetwork.NAME, String.class));
		}
	}

	private boolean findHumanReadableName(final CyRow row, final List<String> attrList, Pattern pattern, boolean exist) {
		String candidateString = null;
		for (final String geneID : attrList) {
			if (pattern.matcher(geneID).find() == exist) {
				candidateString = geneID;
				break;
			}
		}
		if (candidateString != null) {
			if (candidateString.contains("_")) {
				final String firstPart = candidateString.split("_")[0];
				for (String candidate : attrList) {
					if (candidate.equalsIgnoreCase(firstPart)) {
						candidateString = firstPart;
						break;
					}
				}

			}
			row.set(PREDICTED_GENE_NAME, candidateString);
			return true;
		}

		return false;
	}

	public void mapEdgeColumn(final String[] entries, final CyRow row, final CyEdge edge, final String sourceName,
			final String targetName) {

		// Column 7: Detection method
		final String[] detectionMethods = SPLITTER.split(entries[6]);
		final List<String> methods = new ArrayList<String>();
		final List<String> methodID = new ArrayList<String>();
		for (final String entry : detectionMethods) {
			final String[] methodContents = parseValues(entry);
			if (methodContents[1] != null)
				methodID.add(methodContents[1]);
			if (methodContents[2] != null)
				methods.add(methodContents[2]);
		}
		if (!methods.isEmpty())
			row.set(DETECTION_METHOD_NAME, methods);
		if (!methodID.isEmpty())
			row.set(DETECTION_METHOD_ID, methodID);

		// Column 8: Authors
		final String[] authorsParts = SPLITTER.split(entries[7]);
		final List<String> authors = new ArrayList<String>();
		for (final String entry : authorsParts) {
			String updatedAuthor = entry.replaceAll("\"", "");
			authors.add(updatedAuthor);
		}
		if (!authors.isEmpty())
			row.set(AUTHOR, authors);

		final List<String> pubIdList = new ArrayList<String>();
		final List<String> pubDBList = new ArrayList<String>();
		final String[] pubID = SPLITTER.split(entries[8]);
		for (final String entry : pubID) {
			String id = parseValues(entry)[1];
			String db = parseValues(entry)[0];
			if (id != null && db != null) {
				pubDBList.add(db);
				pubIdList.add(id);
			}
		}

		if (!pubIdList.isEmpty())
			row.set(PUB_ID, pubIdList);
		if (!pubDBList.isEmpty())
			row.set(PUB_DB, pubDBList);

		final String[] dbNames = SPLITTER.split(entries[12]);
		row.set(SOURCE_DB, parseValues(dbNames[0])[0]);

		// Interaction Types - Use first one as primary type.
		final String[] typeParts = SPLITTER.split(entries[11]);
		final List<String> types = new ArrayList<String>();
		for (final String entry : typeParts) {
			final String type = parseValues(entry)[2];
			if (type != null)
				types.add(type);
		}
		if (!types.isEmpty()) {
			row.set(INTERACTION_TYPE, types);
			row.set(PRIMARY_INTERACTION_TYPE, types.get(0));
		}

		// Set interaction: this is an ID.
		final String[] interactionID = SPLITTER.split(entries[13]);
		final String interaction = parseValues(interactionID[0])[1];
		row.set(CyEdge.INTERACTION, interaction);

		// Create name
		row.set(CyNetwork.NAME, sourceName + " (" + interaction + ") " + targetName);

		final String[] scores = SPLITTER.split(entries[14]);
		for (String score : scores) {
			final String[] scoreArray = parseValues(score);
			String scoreType = "Confidence-Score-" + scoreArray[0];
			String value = scoreArray[1];
			if (value == null) {
				continue;
			}

			if (row.getTable().getColumn(scoreType) == null)
				row.getTable().createColumn(scoreType, Double.class, true);

			try {
				double doubleVal = Double.parseDouble(value);
				row.set(scoreType, doubleVal);
			} catch (NumberFormatException e) {
				// logger.warn("Invalid number string: " + value);
				// Ignore invalid number
			}

		}
		
		// For MITAB 2.7
		if(entries.length > 15) {
			addListColumn(row, entries[16], "Source Biological Role", String.class);
			addListColumn(row, entries[17], "Target Biological Role", String.class);
			addListColumn(row, entries[18], "Source Experimental Role", String.class);
			addListColumn(row, entries[19], "Target Experimental Role", String.class);
			addListColumn(row, entries[40], "Source Participant Detection Method", String.class);
			addListColumn(row, entries[41], "Target Participant Detection Method", String.class);
			
			addListColumn(row, entries[15], "Complex Expansion", String.class);
			addListColumn(row, entries[24], "Xref", String.class);
			
			addSimpleListColumn(row, entries[27], "Annotation");
			
			addListColumn(row, entries[28], "Host Organism Taxonomy", String.class);
			addSimpleListColumn(row, entries[29], "Parameters");

			addSingleColumn(row, entries[30], "Creation Date", String.class);
			addSingleColumn(row, entries[31], "Update Date", String.class);
			
			addSingleColumn(row, entries[35], "Negative", Boolean.class);
		}
	}

	private final void addListColumn(final CyRow row, final String val, final String columnName, final Class<?> listType) {
		if(val == null || val.equals("-")) {
			return;
		}
		
		String newColName = null;

		// Create column if necessary
		if (columnName != null) {
			if (row.getTable().getColumn(columnName) == null) {
				row.getTable().createListColumn(columnName, listType, false);
				row.getTable().createListColumn(columnName + " ID", String.class, false);
			}
			newColName = columnName;
		}

		final String[] entries = SPLITTER.split(val);

		final List<String> ids = new ArrayList<String>();
		final List<String> descriptions = new ArrayList<String>();
		for (final String entry : entries) {
			final String[] contents = parseValues(entry);
			if (newColName == null) {
				if (row.getTable().getColumn(contents[0]) == null) {
					row.getTable().createListColumn(contents[0], listType, false);
					row.getTable().createListColumn(contents[0] + " ID", String.class, false);
				}
				newColName = contents[0];
			}
			if (contents[1] != null)
				ids.add(contents[1]);
			if (contents[2] != null)
				descriptions.add(contents[2]);
		}
		if (!ids.isEmpty())
			row.set(newColName + " ID", ids);
		if (!descriptions.isEmpty())
			row.set(newColName, descriptions);
	}
	
	
	/**
	 * Split the entry by the delimiter and simply creates list column from the values.
	 * 
	 * @param row
	 * @param val
	 * @param columnName
	 */
	private final void addSimpleListColumn(final CyRow row, final String val, final String columnName) {
		// Ignore invalid entry.
		if(val == null || val.equals("-")) {
			return;
		}
		
		// Create column if necessary
		if (row.getTable().getColumn(columnName) == null) {
			row.getTable().createListColumn(columnName, String.class, false);
		}

		final String[] entries = SPLITTER.split(val);
		final List<String> ids = new ArrayList<String>();
		for (final String entry : entries) {
			if(entry != null) {
				final String newEntry = entry.replaceAll("\"", "");
				ids.add(newEntry);
			}
		}
		if (!ids.isEmpty())
			row.set(columnName, ids);
	}
	
	
	/**
	 * Add a single entry
	 * 
	 * @param row
	 * @param val
	 * @param columnName
	 */
	private final void addSingleColumn(final CyRow row, final String val, final String columnName, final Class<?> dataType) {
		// Ignore invalid entry.
		if(val == null || val.equals("-")) {
			return;
		}
		
		// Create column if necessary
		if (row.getTable().getColumn(columnName) == null) {
			row.getTable().createColumn(columnName, dataType, false);
		}
		
		Object newValue = val;
		if(dataType == Boolean.class)
			newValue = Boolean.parseBoolean(val);
		
		row.set(columnName, newValue);
	}
}