package org.cytoscape.psi_mi.internal.data_mapper;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;

import psidev.psi.mi.xml.model.Alias;
import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.ExperimentDescription;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.InteractionType;
import psidev.psi.mi.xml.model.Interactor;
import psidev.psi.mi.xml.model.InteractorType;
import psidev.psi.mi.xml.model.Names;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Participant;
import psidev.psi.mi.xml.model.Source;

public class PSIMI25EntryMapper {
	
	private static final String NAME_FULL = "Full Name";
	private static final String NAME_SHORT = "Short Label";
	
	private static final String INTERACTOR_TYPE = "Interactor Type";
	private static final String TAX_ID = "Species (NCBI TAX ID)";
	
	private static final String INTERACTION_TYPE = "Interaction Type";
	
	private final EntrySet es;
	private final CyNetwork network;
	
	private final Map<Integer, CyNode>id2NodeMap;
	
	private boolean cancelFlag = false;
	
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
		
		for(final Interactor interactor: interactors) {
			if(cancelFlag)
				return;
			
			final int id = interactor.getId();
			
			final CyNode node = network.addNode();
			final String nameColumn = interactor.getNames().getShortLabel();
			nodeTable.getRow(node.getSUID()).set(CyNetwork.NAME, nameColumn);
			
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
			
			mapAttributes(interactor.getAttributes(), nodeTable, node.getSUID());
			
			id2NodeMap.put(id, node);
		}
	}
	
	private void mapEdges(final Collection<Interaction> interactions) {
		
		final CyTable edgeTable = network.getDefaultEdgeTable();
		final CyTable nodeTable = network.getDefaultNodeTable();
		
		edgeTable.createListColumn(INTERACTION_TYPE, String.class, false);
		
		for(Interaction interaction: interactions) {
			if(cancelFlag)
				return;
			
			final Collection<Participant> nodes = interaction.getParticipants();
			
			// Regular edge
			if(nodes.size() == 2) {
				final Iterator<Participant> itr = nodes.iterator();
				final Participant source = itr.next();
				final Participant target = itr.next();
				
				processEdge(source, target, interaction, nodes, nodeTable, edgeTable);
			} else {
				// TODO: do we need Clique, too?
				createSpokeModel(interaction, nodes, nodeTable, edgeTable);
			}
		}
	}
	
	
	private void createSpokeModel(final Interaction interaction, final Collection<Participant> nodes,
			CyTable nodeTable, CyTable edgeTable) {
		final Participant hub = nodes.iterator().next();

		for (Participant target : nodes) {
			if (hub != target)
				processEdge(hub, target, interaction, nodes, nodeTable, edgeTable);

		}
	}
	
	private void processEdge(final Participant source, final Participant target, final Interaction interaction,
			final Collection<Participant> nodes, CyTable nodeTable, CyTable edgeTable) {
		Interactor sourceInteractor = source.getInteractor();
		Interactor targetInteractor = target.getInteractor();
		
		if (sourceInteractor == null || targetInteractor == null) {
			return;
		}
		
		final CyNode sourceCyNode = id2NodeMap.get(sourceInteractor.getId());
		final CyNode targetCyNode = id2NodeMap.get(targetInteractor.getId());
		
		// PPI does not have directinarity
		final CyEdge edge = network.addEdge(sourceCyNode, targetCyNode, false);

		// TODO: what's the best value for interaction?
		edgeTable.getRow(edge.getSUID()).set(CyEdge.INTERACTION, "pp");
		final String sourceName = nodeTable.getRow(sourceCyNode.getSUID()).get(CyNetwork.NAME, String.class);
		final String targetName = nodeTable.getRow(targetCyNode.getSUID()).get(CyNetwork.NAME, String.class);
		edgeTable.getRow(edge.getSUID()).set(CyNetwork.NAME, sourceName + " (pp) " + targetName);

		mapNames(edgeTable, edge.getSUID(), interaction.getNames(), null);
		mapAttributes(interaction.getAttributes(), edgeTable, edge.getSUID());
		final Collection<InteractionType> types = interaction.getInteractionTypes();
		mapInteractionType(types, edgeTable, edge.getSUID());
	}
	
	private void mapInteractionType(final Collection<InteractionType> types, final CyTable table, final Long suid) {
		if(types == null)
			return;
		
		final List<String> typeList = new ArrayList<String>();
		for(final InteractionType type: types) {
			final String label = type.getNames().getShortLabel();
			typeList.add(label);
		}
		table.getRow(suid).set(INTERACTION_TYPE, typeList);
	}
	
	private void mapAttributes(final Collection<Attribute> attrs, final CyTable table, final Long suid) {
		for(Attribute attr: attrs) {
			final String name = attr.getName();
			
			if(table.getColumn(name) == null)
				table.createListColumn(name, String.class, false);
			
			final CyRow row = table.getRow(suid);
			List<String> list = row.getList(name, String.class);
			if(list == null)
				list = new ArrayList<String>();
			
			final String nameAc = attr.getNameAc();
			final String value = attr.getValue();
			list.add(value);
			row.set(name, list);
		}
	}
	
	private void mapXref() {
		
	}
	
	public void cancel() {
		cancelFlag = true;
	}

}
