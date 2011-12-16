package org.cytoscape.psi_mi.internal.data_mapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;

import psidev.psi.mi.xml.model.Alias;
import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.ExperimentDescription;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Interactor;
import psidev.psi.mi.xml.model.InteractorType;
import psidev.psi.mi.xml.model.Names;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Participant;
import psidev.psi.mi.xml.model.Source;

public class PSIMI25EntryMapper {
	
	private static final String NAME_FULL = "Full Name";
	private static final String NAME_SHORT = CyTableEntry.NAME;
	
	private static final String INTERACTOR_TYPE = "Interactor Type";
	private static final String TAX_ID = "Species (NCBI TAX ID)";
	private static final String SPECIES = "Species";
	
	private final EntrySet es;
	private final CyNetwork network;
	
	private final Map<Integer, CyNode>id2NodeMap;
	
	public PSIMI25EntryMapper(final CyNetwork network, final EntrySet es) {
		this.es = es;
		this.network = network;
		
		id2NodeMap = new HashMap<Integer, CyNode>();
	}
	
	public void map() {
		final Collection<Entry> entries = es.getEntries();
		
		for(Entry entry: entries) {
			mapNetworkMetaData(entry);
			mapNodes(entry.getInteractors());
			mapEdges(entry.getInteractions());
		}
	}

	
	/**
	 * Convert network metadata to CyNetwork table data.
	 * 
	 * @param entry
	 */
	private void mapNetworkMetaData(final Entry entry) {
		final CyTable networkTable = network.getDefaultNetworkTable();
		final Collection<Attribute> attrs = entry.getAttributes();
		final Collection<ExperimentDescription> exp = entry.getExperiments();
		
		// TODO: this takes very long time for some data sets.
		//mapExperiments(exp, networkTable);
		// Source is always unique
		final Source source = entry.getSource();
		mapSource(source, networkTable);
		entry.getAvailabilities();
		
	}
	
	private void mapSource(final Source source, final CyTable networkTable) {
		final Names names = source.getNames();
		mapNames(networkTable, network.getSUID(), names, "Network Source ");
		
		source.getBibref();
	}
	
	private void mapNames(final CyTable table, final Long suid, final Names names, final String prefix) {
		if(names == null)
			return;
		
		final String shortName = names.getShortLabel();
		final String shortNameLael;
		final String fullNameLabel;
		final Collection<Alias> aliases = names.getAliases();
		
		if(prefix != null) {
			shortNameLael = prefix + NAME_SHORT;
			fullNameLabel = prefix + NAME_FULL;
		} else {
			shortNameLael = NAME_SHORT;
			fullNameLabel = NAME_FULL;
		}
		
		if(shortName != null) {
			if(table.getColumn(shortNameLael) == null)
				table.createColumn(shortNameLael, String.class, false);
			
			table.getRow(suid).set(shortNameLael, shortName);
		}
		final String fullName = names.getFullName();
		if(fullName != null) {
			if(table.getColumn(fullNameLabel) == null)
				table.createColumn(fullNameLabel, String.class, false);
			
			table.getRow(suid).set(fullNameLabel, fullName);
		}
		
		for(Alias alias: aliases) {
			final String type = alias.getType();
			final String val = alias.getValue();
			if(table.getColumn(type) == null)
				table.createColumn(type, String.class, false);
			
			table.getRow(suid).set(type, val);
		}
	}
	
	private void mapExperiments(Collection<ExperimentDescription> exp, final CyTable networkTable) {
		int expCount = 1;
		
		for(ExperimentDescription desc: exp) {
			String prefix = "Experiment " + expCount + ": ";
			final Names names = desc.getNames();
			mapNames(networkTable, network.getSUID(), names, prefix);
			expCount++;
		}
	}

	private void mapNodes(final Collection<Interactor> interactors) {
		final CyTable nodeTable = network.getDefaultNodeTable();
		
		// Create default columns
		nodeTable.createColumn(INTERACTOR_TYPE, String.class, false);
		nodeTable.createColumn(TAX_ID, String.class, false);
		nodeTable.createColumn(SPECIES, String.class, false);
		
		for(final Interactor interactor: interactors) {
			final int id = interactor.getId();
			final CyNode node = network.addNode();
			final InteractorType itrType = interactor.getInteractorType();
			final Names typeNames = itrType.getNames();
			if(typeNames != null) {
				final String shortName = typeNames.getShortLabel();
				nodeTable.getRow(node.getSUID()).set(INTERACTOR_TYPE, shortName);
			}
			final Organism org = interactor.getOrganism();
			if(org != null) {
				final Integer taxID = org.getNcbiTaxId();
				mapNames(nodeTable, node.getSUID(), org.getNames(), "Species ");
				
				if(taxID != null)
					nodeTable.getRow(node.getSUID()).set(TAX_ID, Integer.toString(taxID));
				
			}
			mapNames(nodeTable, node.getSUID(), interactor.getNames(), null);
			id2NodeMap.put(id, node);
		}
	}
	
	private void mapEdges(final Collection<Interaction> interactions) {
		
		final CyTable edgeTable = network.getDefaultEdgeTable();
		final CyTable nodeTable = network.getDefaultNodeTable();
		
		for(Interaction interaction: interactions) {
			final int id = interaction.getId();
			final Collection<Participant> nodes = interaction.getParticipants();
			
			// Regular edge
			if(nodes.size() == 2) {
				final Iterator<Participant> itr = nodes.iterator();
				final Participant source = itr.next();
				final Participant target = itr.next();
				
				final CyNode sourceCyNode = id2NodeMap.get(source.getInteractor().getId());
				final CyNode targetCyNode = id2NodeMap.get(target.getInteractor().getId());
				// PPI does not have directinarity
				final CyEdge edge = network.addEdge(sourceCyNode, targetCyNode, false);
				edgeTable.getRow(edge.getSUID()).set(CyEdge.INTERACTION, "pp");
				final String sourceName = nodeTable.getRow(sourceCyNode.getSUID()).get(CyTableEntry.NAME, String.class);
				final String targetName = nodeTable.getRow(targetCyNode.getSUID()).get(CyTableEntry.NAME, String.class);
				edgeTable.getRow(edge.getSUID()).set(CyTableEntry.NAME,  sourceName + " (pp) " + targetName);
				
				mapNames(edgeTable, edge.getSUID(), interaction.getNames(), null);
			} else {
				createClique(nodes);
			}
		}
		
	}
	
	
	private void createClique(final Collection<Participant> nodes) {
//		for(Participant p: nodes) {
//			ref = p.getInteractorRef().getRef();
//		}
	}
	
	private void mapAttributes(final Collection<Attribute> attrs, final CyTable table) {
		for(Attribute attr: attrs) {
			final String name = attr.getName();
			
			if(table.getColumn(name) == null)
				table.createColumn(name, String.class, false);
			
			final String nameAc = attr.getNameAc();
			final String value = attr.getValue();
		}
	}
	
	private void mapXref() {
		
	}

}
