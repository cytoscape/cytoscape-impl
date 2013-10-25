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

	public static final String PREDICTED_GENE_NAME = "Human Readable Gene Name";
	public static final String CROSS_SPECIES_EDGE = "Cross Species Interaction";

	static final String PUB_ID = "publication id";
	static final String PUB_DB = "publication db";

	static final String EXPERIMENT = "experiment";

	private final static Pattern exact1Pattern = Pattern.compile("^[A-Z][A-Z][A-Z]\\d");
	private final static Pattern ncbiPattern = Pattern.compile("^[A-Za-z].+");
	private final static Pattern uniprotPattern = Pattern.compile("^[a-zA-Z]\\d.+");

	private static final String ENTREZ_GENE_ATTR_NAME = "entrez gene/locuslink";
	private static final String UNIPROT_ATTR_NAME = "uniprot";
	private static final String STRING_ATTR_NAME = "string";

	private final Set<String> namespaceSet;
	private final Map<String, String> name2ns;
	private final Map<String, String> synonym2ns;

	static final String TAXNOMY = "taxonomy";
	static final String TAXNOMY_NAME = "taxonomy.name";

	boolean isInitialized = false;

	private String currentGeneName = null;

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
	private final String[] parseValues(final String entry) {
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

	private Map<String, String> createNames(String nameText) {
		final Map<String, String> map = new HashMap<String, String>();
		final String[] names = SPLITTER.split(nameText);

		for (String name : names) {
			final String[] parts = SPLITTER_NAME_SPACE.split(name);
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

		for (String[] entry : entries) {
			for (String name : entry) {
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

		// for (int i = 0; i < entries.length; i++) {
		// String[] oneEntry = SPLITTER.split(entries[i]);
		// for (String entry : oneEntry) {
		// final String[] vals = parseValues(entry);
		// System.out.println(i + ")   " + vals[0] + " --- " + vals[1] + " --- "
		// + vals[2]);
		// }
		// }
		//
		// System.out.print("\n\n");

		final Map<String, String> accsSource = createNames(entries[0]);
		final Map<String, String> accsTarget = createNames(entries[1]);
		processNames(sourceRow, accsSource);
		processNames(targetRow, accsTarget);

		final Map<String, List<String>> otherSource = createOtherNames(entries[2], entries[4]);
		processOtherNames(sourceRow, otherSource);
		if (currentGeneName != null)
			sourceRow.set(PREDICTED_GENE_NAME, currentGeneName);
		else {
			guessHumanReadableName(sourceRow);
		}

		final Map<String, List<String>> otherTarget = createOtherNames(entries[3], entries[5]);
		processOtherNames(targetRow, otherTarget);
		if (currentGeneName != null)
			targetRow.set(PREDICTED_GENE_NAME, currentGeneName);
		else {
			guessHumanReadableName(targetRow);
		}

		setSpecies(entries[9], sourceRow);
		setSpecies(entries[10], targetRow);

		// For 2.6 data

		// For 2.7 data

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
		final Map<String, String> accsTarget = interaction.getInteractorAccsB();
		processNames(sourceRow, accsSource);
		processNames(targetRow, accsTarget);

		final Map<String, List<String>> otherSource = interaction.getOtherInteractorAccsA();
		final Map<String, List<String>> otherTarget = interaction.getOtherInteractorAccsB();
		processOtherNames(sourceRow, otherSource);
		processOtherNames(targetRow, otherTarget);

		final Collection<CrossReference> speciesSource = interaction.getOrganismsA();
		final Collection<CrossReference> speciesTarget = interaction.getOrganismsB();

		// Add Species names
		if (speciesSource.size() != 0) {
			CrossReference speciesSourceFirst = speciesSource.iterator().next();
			processSpecies(sourceRow, speciesSourceFirst);
		}
		if (speciesTarget.size() != 0) {
			CrossReference speciesTargetFirst = speciesTarget.iterator().next();
			processSpecies(targetRow, speciesTargetFirst);
		}

		// Try to find hjuman-readable gene name
		guessHumanReadableName(sourceRow);
		guessHumanReadableName(targetRow);
	}

	public void mapEdgeColumn(final EncoreInteraction interaction, final CyRow row) {

		final Set<String> exp = interaction.getExperimentToPubmed().keySet();
		row.set(EXPERIMENT, new ArrayList<String>(exp));

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

		// Interaction (use DB names)
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

	public void mapEdgeColumn(final String[] entries, final CyRow row) {
		final String[] detectionMethods = SPLITTER.split(entries[6]);
		final List<String> methods = new ArrayList<String>();
		for (final String entry : detectionMethods) {
			final String method = parseValues(entry)[1];
			if (method != null)
				methods.add(method);
		}
		if (!methods.isEmpty())
			row.set(EXPERIMENT, methods);

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

		// Interaction (use DB names)
		final String[] dbNames = SPLITTER.split(entries[12]);
		row.set(CyEdge.INTERACTION, parseValues(dbNames[0])[0]);

		final String[] scores = SPLITTER.split(entries[14]);
		for (String score : scores) {
			final String[] scoreArray = parseValues(score);
			String scoreType = "Confidence-Score-" + scoreArray[0];
			String value = scoreArray[1];
			if(value == null) {
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
	}
}
