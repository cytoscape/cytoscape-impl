package org.cytoscape.webservice.psicquic.mapper;

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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.webservice.psicquic.miriam.Miriam;
import org.cytoscape.webservice.psicquic.miriam.Miriam.Datatype;
import org.cytoscape.webservice.psicquic.miriam.Synonyms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class InteractionClusterMapper {

	private static final Logger logger = LoggerFactory.getLogger(InteractionClusterMapper.class);

	private static final String SCHEMA_NAMESPACE = "org.cytoscape.webservice.psicquic.miriam";

	static final String PREDICTED_GENE_NAME = "Human Readable Gene Name";
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
	static final String TAXNOMY_DB = "taxonomy.db";

	public InteractionClusterMapper() {
		namespaceSet = new HashSet<String>();
		this.name2ns = new HashMap<String, String>();
		this.synonym2ns = new HashMap<String, String>();

		// Read resource file
		try {
			parseXml();
		} catch (Exception ex) {
			throw new RuntimeException("Could not read resource file", ex);
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
			final String db = ref.getDatabase();

			row.set(TAXNOMY, speciesID);
			row.set(TAXNOMY_NAME, name);
			row.set(TAXNOMY_DB, db);
		}
	}

	private Miriam parseXml() throws IOException {

		final URL xml = MergedNetworkBuilder.class.getClassLoader().getResource("MiriamResources_all.xml");
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
			if(candidateString.contains("_")) {
				final String firstPart = candidateString.split("_")[0];
				for(String candidate: attrList) {
					if(candidate.equalsIgnoreCase(firstPart)) {
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
}
