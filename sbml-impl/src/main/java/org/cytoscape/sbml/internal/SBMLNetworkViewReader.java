package org.cytoscape.sbml.internal;

/*
 * #%L
 * Cytoscape SBML Impl (sbml-impl)
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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public class SBMLNetworkViewReader extends AbstractTask implements CyNetworkReader {
	private static final int BUFFER_SIZE = 16384;
	
	static final String NODE_NAME_ATTR_LABEL = "name"; //$NON-NLS-1$

	static final String INTERACTION_TYPE_ATTR = "interaction type"; //$NON-NLS-1$
	
	static final String SBML_TYPE_ATTR = "sbml type"; //$NON-NLS-1$

	static final String SBML_ID_ATTR = "sbml id"; //$NON-NLS-1$

	static final String SBML_INITIAL_CONCENTRATION_ATTR = "sbml initial concentration"; //$NON-NLS-1$

	static final String SBML_INITIAL_AMOUNT_ATTR = "sbml initial amount"; //$NON-NLS-1$

	static final String SBML_CHARGE_ATTR = "sbml charge"; //$NON-NLS-1$

	static final String SBML_COMPARTMENT_ATTR = "sbml compartment"; //$NON-NLS-1$

	static final String SBML_TYPE_SPECIES = "species"; //$NON-NLS-1$

	static final String SBML_TYPE_REACTION = "reaction"; //$NON-NLS-1$

	static final String INTERACTION_TYPE_REACTION_PRODUCT = "reaction-product"; //$NON-NLS-1$

	static final String INTERACTION_TYPE_REACTION_REACTANT = "reaction-reactant"; //$NON-NLS-1$

	static final String INTERACTION_TYPE_REACTION_MODIFIER = "reaction-modifier"; //$NON-NLS-1$

	static final String KINETIC_LAW_ATTR_TEMPLATE = "kineticLaw-%1$s"; //$NON-NLS-1$

	static final String KINETIC_LAW_UNITS_ATTR_TEMPLATE = "kineticLaw-%1$s-units"; //$NON-NLS-1$
	
	private final InputStream stream;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;

	//private CyNetworkView view;
	private CyNetwork network;

	public SBMLNetworkViewReader(InputStream stream, CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory) {
		this.stream = stream;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
	}

	@SuppressWarnings("deprecation")
	public void run(TaskMonitor taskMonitor) throws Exception {
		String xml = readString(stream);
		SBMLDocument document = JSBML.readSBMLFromString(xml);
		
		network = networkFactory.createNetwork();
		//view = viewFactory.getNetworkView(network);
		Model model = document.getModel();
		
		// Create a node for each Species
		Map<String, CyNode> speciesById = new HashMap<String, CyNode>();
		for (Species species : model.getListOfSpecies()) {
			CyNode node = network.addNode();
			speciesById.put(species.getId(), node);
			CyRow attributes = network.getRow(node);
			checkNodeSchema(attributes);
			attributes.set(NODE_NAME_ATTR_LABEL, species.getName());
			attributes.set(SBML_TYPE_ATTR, SBML_TYPE_SPECIES);
			attributes.set(SBML_ID_ATTR, species.getId());

			attributes.set(SBML_INITIAL_CONCENTRATION_ATTR, species.getInitialConcentration());
			attributes.set(SBML_INITIAL_AMOUNT_ATTR, species.getInitialAmount());
			attributes.set(SBML_CHARGE_ATTR, species.getCharge());
			
			String compartment = species.getCompartment();
			if (compartment != null) {
				attributes.set(SBML_COMPARTMENT_ATTR, compartment);
			}
		}
		
		// Create a node for each Reaction
		Map<String, CyNode> reactionsById = new HashMap<String, CyNode>();
		for (Reaction reaction : model.getListOfReactions()) {
			CyNode node = network.addNode();
			reactionsById.put(reaction.getId(), node);
			CyRow attributes = network.getRow(node);
			checkNodeSchema(attributes);
			String name = reaction.getName();
			if (name == null) {
				attributes.set(NODE_NAME_ATTR_LABEL, reaction.getId());
			} else {
				attributes.set(NODE_NAME_ATTR_LABEL, name);
			}
			attributes.set(SBML_TYPE_ATTR, SBML_TYPE_REACTION);
			attributes.set(SBML_ID_ATTR, reaction.getId());
			
			for (SpeciesReference product : reaction.getListOfProducts()) {
				CyNode sourceNode = speciesById.get(product.getSpecies());
				CyEdge edge = network.addEdge(sourceNode, node, true);
				CyRow edgeAttributes = network.getRow(edge);
				checkEdgeSchema(edgeAttributes);
				edgeAttributes.set(INTERACTION_TYPE_ATTR, INTERACTION_TYPE_REACTION_PRODUCT);
			}
			
			for (SpeciesReference reactant : reaction.getListOfReactants()) {
				CyNode sourceNode = speciesById.get(reactant.getSpecies());
				CyEdge edge = network.addEdge(sourceNode, node, true);
				CyRow edgeAttributes = network.getRow(edge);
				checkEdgeSchema(edgeAttributes);
				edgeAttributes.set(INTERACTION_TYPE_ATTR, INTERACTION_TYPE_REACTION_REACTANT);
			}
			
			for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
				CyNode sourceNode = speciesById.get(modifier.getSpecies());
				CyEdge edge = network.addEdge(sourceNode, node, true);
				CyRow edgeAttributes = network.getRow(edge);
				checkEdgeSchema(edgeAttributes);
				edgeAttributes.set(INTERACTION_TYPE_ATTR, INTERACTION_TYPE_REACTION_MODIFIER);
			}
			
			KineticLaw law = reaction.getKineticLaw();
			if (law != null) {
				for (LocalParameter parameter : law.getListOfParameters()) {
					String parameterName = parameter.getName();
					String key = String.format(KINETIC_LAW_ATTR_TEMPLATE, parameterName);
					checkSchema(attributes, key, Double.class);
					attributes.set(key, parameter.getValue());
					
					String units = parameter.getUnits();
					if (units != null) {
						String unitsKey = String.format(KINETIC_LAW_UNITS_ATTR_TEMPLATE, parameterName);
						checkSchema(attributes, unitsKey, String.class);
						attributes.set(unitsKey, units);
					}
				}
			}
		}
	}
	
	private void checkEdgeSchema(CyRow attributes) {
		checkSchema(attributes, INTERACTION_TYPE_ATTR, String.class);
	}

	private void checkNodeSchema(CyRow attributes) {
		checkSchema(attributes, SBML_TYPE_ATTR, String.class);
		checkSchema(attributes, SBML_ID_ATTR, String.class);
		checkSchema(attributes, SBML_INITIAL_CONCENTRATION_ATTR, Double.class);
		checkSchema(attributes, SBML_INITIAL_AMOUNT_ATTR, Double.class);
		checkSchema(attributes, SBML_CHARGE_ATTR, Integer.class);
		checkSchema(attributes, SBML_COMPARTMENT_ATTR, String.class);
	}

	private <T> void checkSchema(CyRow attributes, String attributeName, Class<T> type) {
		if (attributes.getTable().getColumn(attributeName) == null)
			attributes.getTable().createColumn(attributeName, type, false);
	}

	private static String readString(InputStream source) throws IOException {
		StringWriter writer = new StringWriter();
		BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		try {
			char[] buffer = new char[BUFFER_SIZE];
			int charactersRead = reader.read(buffer, 0, buffer.length);
			while (charactersRead != -1) {
				writer.write(buffer, 0, charactersRead);
				charactersRead = reader.read(buffer, 0, buffer.length);
			}
		} finally {
			reader.close();
		}
		return writer.toString();
	}

	public void cancel() {
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		return viewFactory.createNetworkView(network);
	}
}
