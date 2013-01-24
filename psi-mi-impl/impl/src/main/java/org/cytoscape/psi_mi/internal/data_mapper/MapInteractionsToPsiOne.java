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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.psi_mi.internal.model.AttributeBag;
import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;
import org.cytoscape.psi_mi.internal.schema.mi1.BibrefType;
import org.cytoscape.psi_mi.internal.schema.mi1.CvType;
import org.cytoscape.psi_mi.internal.schema.mi1.DbReferenceType;
import org.cytoscape.psi_mi.internal.schema.mi1.EntrySet;
import org.cytoscape.psi_mi.internal.schema.mi1.ExperimentType;
import org.cytoscape.psi_mi.internal.schema.mi1.InteractionElementType;
import org.cytoscape.psi_mi.internal.schema.mi1.NamesType;
import org.cytoscape.psi_mi.internal.schema.mi1.ProteinInteractorType;
import org.cytoscape.psi_mi.internal.schema.mi1.ProteinParticipantType;
import org.cytoscape.psi_mi.internal.schema.mi1.RefType;
import org.cytoscape.psi_mi.internal.schema.mi1.XrefType;


/**
 * Converts Data Services Object Model to the PSI-MI, Level 1 Format.
 *
 * @author Ethan Cerami
 */
public class MapInteractionsToPsiOne implements SchemaMapper<EntrySet> {
	public static final String SCHEMA_NAMESPACE = "org.cytoscape.psi_mi.internal.schema.mi1";
	
	private static final String EXP_AFFINITY_PRECIPITATION = "Affinity Precipitation";
	private static final String EXP_AFFINITY_CHROMOTOGRAPHY = "Affinity Chromatography";
	private static final String EXP_TWO_HYBRID = "Two Hybrid";
	private static final String EXP_PURIFIED_COMPLEX = "Purified Complex";

	@Override
	public String getSchemaNamespace() {
		return SCHEMA_NAMESPACE;
	}
	
	/**
	 * Pub Med Database.
	 */
	private static final String PUB_MED_DB = "pubmed";
	private EntrySet entrySet;

	/**
	 * ArrayList of Protein-Protein Interactions.
	 */
	private List<Interaction> interactions;

	/**
	 * Constructor.
	 *
	 * @param interactions ArrayList of Interactions.
	 */
	public MapInteractionsToPsiOne(List<Interaction> interactions) {
		this.interactions = interactions;
	}

	/**
	 * Perform Mapping.
	 */
	public void doMapping() {
		// Create Entry Set and Entry
		entrySet = new EntrySet();
		entrySet.setLevel(new BigInteger("1"));
		entrySet.setVersion(new BigInteger("1"));

		EntrySet.Entry entry = new EntrySet.Entry();

		//  Get Interactor List
		EntrySet.Entry.InteractorList interactorList = getInteractorList();

		//  Get Interaction List
		EntrySet.Entry.InteractionList interactionList = getInteractionList();

		//  Add to Entry node
		entry.setInteractorList(interactorList);
		entry.setInteractionList(interactionList);
		entrySet.getEntry().add(entry);
	}

	/**
	 * Gets PSI XML.
	 *
	 * @return Root PSI Element.
	 */
	@Override
	public EntrySet getModel() {
		return entrySet;
	}

	/**
	 * Gets Interactor List.
	 *
	 * @return Castor InteractorList.
	 */
	private EntrySet.Entry.InteractorList getInteractorList() {
		Map<String, Interactor> proteinSet = getNonRedundantInteractors();
		EntrySet.Entry.InteractorList interactorList = new EntrySet.Entry.InteractorList();

		for (Interactor interactor : proteinSet.values()) {
			//  Create new Interactor
			ProteinInteractorType jaxbInteractor = new ProteinInteractorType();
			setNameId(interactor, jaxbInteractor);
			setOrganism(interactor, jaxbInteractor);
			setSequence(interactor, jaxbInteractor);

			XrefType xref = createExternalRefs(interactor);

			if (xref != null) {
				jaxbInteractor.setXref(xref);
			}

			//  Add to Interactor List
			interactorList.getProteinInteractor().add(jaxbInteractor);
		}

		return interactorList;
	}

	/**
	 * Sets Sequence Data.
	 */
	private void setSequence(Interactor interactor, ProteinInteractorType jaxbInteractor) {
		String seqData = (String) interactor.getAttribute(InteractorVocab.SEQUENCE_DATA);

		if (seqData != null) {
			jaxbInteractor.setSequence(seqData);
		}
	}

	/**
	 * Sets Interactor Name and ID.
	 *
	 * @param interactor     Data Services Interactor object.
	 * @param jaxbInteractor JAXB Protein Interactor Object.
	 */
	private void setNameId(Interactor interactor, ProteinInteractorType jaxbInteractor) {
		NamesType names = new NamesType();
		names.setShortLabel(interactor.getName());

		String fullName = (String) interactor.getAttribute(InteractorVocab.FULL_NAME);

		if (fullName != null) {
			names.setFullName(fullName);
		}

		jaxbInteractor.setNames(names);
		jaxbInteractor.setId(interactor.getName());
	}

	/**
	 * Sets Interactor Organism.
	 *
	 * @param interactor     Data Services Interactor Object.
	 * @param jaxbInteractor JAXB Protein Interactor Object.
	 */
	private void setOrganism(Interactor interactor, ProteinInteractorType jaxbInteractor) {
		ProteinInteractorType.Organism organism = new ProteinInteractorType.Organism();
		String taxonomyID = (String) interactor.getAttribute(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID);

		if (taxonomyID != null) {
			organism.setNcbiTaxId(new BigInteger(taxonomyID));
		}

		NamesType orgNames = new NamesType();
		String commonName = (String) interactor.getAttribute(InteractorVocab.ORGANISM_COMMON_NAME);

		if (commonName != null) {
			orgNames.setShortLabel(commonName);
		}

		String speciesName = (String) interactor.getAttribute(InteractorVocab.ORGANISM_SPECIES_NAME);

		if (speciesName != null) {
			orgNames.setFullName(speciesName);
		}

		organism.setNames(orgNames);
		jaxbInteractor.setOrganism(organism);
	}

	/**
	 * Sets Interactor External References.
	 * Filters out any redundant external references.
	 */
	private XrefType createExternalRefs(AttributeBag bag) {
		Set<String> set = new HashSet<String>();
		ExternalReference[] refs = bag.getExternalRefs();
		XrefType xref = new XrefType();

		if ((refs != null) && (refs.length > 0)) {
			//  Add Primary Reference
			createPrimaryKey(refs[0], xref);

			//  All others become Secondary References
			if (refs.length > 1) {
				for (int i = 1; i < refs.length; i++) {
					String key = this.generateXRefKey(refs[i]);

					if (!set.contains(key)) {
						createSecondaryKey(refs[i], xref);
						set.add(key);
					}
				}
			}
		}

		if (xref.getPrimaryRef() != null) {
			return xref;
		} else {
			return null;
		}
	}

	/**
	 * Generates XRef Key.
	 *
	 * @param ref External Reference
	 * @return Hash Key.
	 */
	private String generateXRefKey(ExternalReference ref) {
		return ref.getDatabase() + "." + ref.getId();
	}

	/**
	 * Creates Primary Key.
	 *
	 * @param ref  External Reference.
	 * @param xref Castor XRef.
	 */
	private void createPrimaryKey(ExternalReference ref, XrefType xref) {
		DbReferenceType primaryRef = new DbReferenceType();
		primaryRef.setDb(ref.getDatabase());
		primaryRef.setId(ref.getId());
		xref.setPrimaryRef(primaryRef);
	}

	/**
	 * Creates Secondary Key.
	 *
	 * @param ref  External Reference
	 * @param xref Castro XRef.
	 */
	private void createSecondaryKey(ExternalReference ref, XrefType xref) {
		DbReferenceType secondaryRef = new DbReferenceType();
		secondaryRef.setDb(ref.getDatabase());
		secondaryRef.setId(ref.getId());
		xref.getSecondaryRef().add(secondaryRef);
	}

	/**
	 * Gets a complete list of NonRedundant Proteins.
	 *
	 * @return HashMap of NonRedundant Proteins.
	 */
	private Map<String, Interactor> getNonRedundantInteractors() {
		Map<String, Interactor> interactorMap = new HashMap<String, Interactor>();

		for (Interaction interaction : interactions) {
			List<Interactor> interactors = interaction.getInteractors();

			for (Interactor interactor : interactors) {
				addToHashMap(interactor, interactorMap);
			}
		}

		return interactorMap;
	}

	/**
	 * Conditionally adds Protein to HashMap.
	 *
	 * @param interactor    Interactor Object.
	 * @param interactorMap HashMap of NonRedundant Interactors.
	 */
	private void addToHashMap(Interactor interactor, Map<String, Interactor> interactorMap) {
		String orfName = interactor.getName();

		if (!interactorMap.containsKey(orfName)) {
			interactorMap.put(orfName, interactor);
		}
	}

	/**
	 * Gets Interaction List.
	 *
	 * @return Castor InteractionList.
	 */
	private EntrySet.Entry.InteractionList getInteractionList() {
		EntrySet.Entry.InteractionList interactionList = new EntrySet.Entry.InteractionList();

		//  Iterate through all interactions
		for (int i = 0; i < interactions.size(); i++) {
			//  Create New Interaction
			InteractionElementType jaxbInteraction = new InteractionElementType();
			Interaction interaction = interactions.get(i);

			//  Add Experiment List
			InteractionElementType.ExperimentList expList = getExperimentDescription(interaction, i);
			jaxbInteraction.setExperimentList(expList);

			//  Add Participants
			InteractionElementType.ParticipantList participantList = getParticipantList(interaction);
			jaxbInteraction.setParticipantList(participantList);

			//  Add to Interaction List
			interactionList.getInteraction().add(jaxbInteraction);

			//  Add Xrefs
			XrefType xref = createExternalRefs(interaction);

			if (xref != null) {
				jaxbInteraction.setXref(xref);
			}
		}

		return interactionList;
	}

	/**
	 * Gets Experiment Description.
	 *
	 * @param interaction Interaction object.
	 * @return Castor InteractionElementTypeChoice object.
	 */
	private InteractionElementType.ExperimentList getExperimentDescription(Interaction interaction,
	                                                                       int index) {
		//  Create New Experiment List
		InteractionElementType.ExperimentList expList = new InteractionElementType.ExperimentList();

		//  Create New Experiment Description
		ExperimentType expType = new ExperimentType();

		//  Set Experimental ID
		expType.setId("exp" + index);

		//  Set Bibliographic Reference
		BibrefType bibRef = null;

		Object pmid = interaction.getAttribute(InteractionVocab.PUB_MED_ID);

		if ((pmid != null) && pmid instanceof String) {
			bibRef = createBibRef(PUB_MED_DB, (String) pmid);
			expType.setBibref(bibRef);
		}

		//  Set Interaction Detection
		CvType interactionDetection = getInteractionDetection(interaction);
		expType.setInteractionDetection(interactionDetection);

		//  Set Choice Element
		expList.getExperimentRefOrExperimentDescription().add(expType);

		return expList;
	}

	/**
	 * Creates a Bibliography Reference.
	 *
	 * @param database Database.
	 * @param id       ID String.
	 * @return Castor Bibref Object.
	 */
	private BibrefType createBibRef(String database, String id) {
		XrefType xref = createXRef(database, id);
		BibrefType bibRef = new BibrefType();
		bibRef.setXref(xref);

		return bibRef;
	}

	/**
	 * Creates a Primary Reference.
	 *
	 * @param database Database.
	 * @param id       ID String.
	 * @return Castor XRef object
	 */
	private XrefType createXRef(String database, String id) {
		XrefType xref = new XrefType();
		DbReferenceType primaryRef = new DbReferenceType();
		primaryRef.setDb(database);
		primaryRef.setId(id);
		xref.setPrimaryRef(primaryRef);

		return xref;
	}

	/**
	 * Gets Interaction Detection element.
	 * It is possible that an interaction is missing important attributes,
	 * such as Experimental System Name, XRef DB, and XRef ID.  All of these
	 * attributes are required by PSI.  Rather than throwing an exception
	 * here, the data_mapper manually specifies "Not Specified" for all missing
	 * attributes.
	 *
	 * @param interaction Interaction.
	 * @return InteractionDetection Object.
	 */
	private CvType getInteractionDetection(Interaction interaction) {
		CvType interactionDetection = new CvType();
		String idStr = null;

		try {
			idStr = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME);
		} catch (ClassCastException e) {
			idStr = null;
		}

		if (idStr == null) {
			idStr = "Not Specified";
		}

		String idRef = null;

		try {
			idRef = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_ID);
		} catch (ClassCastException e) {
			idRef = null;
		}

		//  If there is no ID Ref, find a best match.
		if (idRef == null) {
			if (idStr.equals(EXP_AFFINITY_PRECIPITATION)
			    || idStr.equals(EXP_AFFINITY_CHROMOTOGRAPHY)) {
				idStr = "affinity chromatography technologies";
				idRef = "MI:0004";
			} else if (idStr.equals(EXP_TWO_HYBRID)) {
				idStr = "classical two hybrid";
				idRef = "MI:0018";
			} else if (idStr.equals(EXP_PURIFIED_COMPLEX)) {
				idStr = "copurification";
				idRef = "MI:0025";
			} else {
				idRef = "Not Specified";
			}
		}

		NamesType names = createName(idStr, null);
		interactionDetection.setNames(names);

		String dbRef = null;

		try {
			dbRef = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_DB);
		} catch (ClassCastException e) {
			dbRef = null;
		}

		if (dbRef == null) {
			dbRef = "PSI-MI";
		}

		XrefType xref = createXRef(dbRef, idRef);
		interactionDetection.setXref(xref);

		return interactionDetection;
	}

	/**
	 * Creates a new Names Object.
	 *
	 * @param shortLabel Short Name Label.
	 * @param fullName   Full Name/Description.
	 * @return Castor Names Object.
	 */
	private NamesType createName(String shortLabel, String fullName) {
		NamesType names = new NamesType();
		names.setShortLabel(shortLabel);

		if (fullName != null) {
			names.setFullName(fullName);
		}

		return names;
	}

	/**
	 * Gets the Interaction Participant List.
	 *
	 * @param interaction Interaction object.
	 * @return Castor Participant List.
	 */
	private InteractionElementType.ParticipantList getParticipantList(Interaction interaction) {
		InteractionElementType.ParticipantList participantList = new InteractionElementType.ParticipantList();

		List<Interactor> interactors = interaction.getInteractors();

		for (Interactor interactor : interactors) {
			String name = interactor.getName();
			ProteinParticipantType participant1 = createParticipant(name);
			participantList.getProteinParticipant().add(participant1);
		}

		return participantList;
	}

	/**
	 * Create New Protein Participant.
	 *
	 * @param id Protein ID.
	 * @return Castor Protein Participant Object.
	 */
	private ProteinParticipantType createParticipant(String id) {
		ProteinParticipantType participant = new ProteinParticipantType();
		RefType ref = new RefType();
		ref.setRef(id);
		participant.setProteinInteractorRef(ref);

		return participant;
	}
}
