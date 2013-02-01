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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;
import org.cytoscape.psi_mi.internal.schema.mi25.BibrefType;
import org.cytoscape.psi_mi.internal.schema.mi25.CvType;
import org.cytoscape.psi_mi.internal.schema.mi25.DbReferenceType;
import org.cytoscape.psi_mi.internal.schema.mi25.EntrySet;
import org.cytoscape.psi_mi.internal.schema.mi25.ExperimentType;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractionElementType;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractorElementType;
import org.cytoscape.psi_mi.internal.schema.mi25.NamesType;
import org.cytoscape.psi_mi.internal.schema.mi25.ParticipantType;
import org.cytoscape.psi_mi.internal.schema.mi25.XrefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps PSI-MI Level 2.5 to Interaction Objects.
 *
 * @author Ethan Cerami
 * @author Nisha Vinod
 */
public class MapPsiTwoFiveToInteractions implements Mapper {
	
	private static final Logger logger = LoggerFactory.getLogger(MapPsiTwoFiveToInteractions.class);
		
	private final Map<String, InteractorElementType> interactorMap;
	private final Map<String, ExperimentType> experimentMap;
	
	private final List<Interaction> interactions;
	private final String xml;
	

	/**
	 * Constructor.
	 *
	 * @param xml XML Document.
	 * @param interactions ArrayList of Interaction objects.
	 */
	public MapPsiTwoFiveToInteractions(final String xml, final List<Interaction> interactions) {
		this.xml = xml;
		this.interactions = interactions;
		
		interactorMap = new HashMap<String, InteractorElementType>();
		experimentMap = new HashMap<String, ExperimentType>();
	}

	/**
	 * Perform Mapping.
	 *
	 * @throws MapperException Problem Performing mapping.
	 */
	@Override
	public void doMapping() throws MapperException {
		parseXml(xml);
	}

	/**
	 * Parses the PSI XML Document.
	 * @throws MapperException 
	 */
	private void parseXml(final String xml) throws MapperException {

		final StringReader reader = new StringReader(xml);

		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance(MapInteractionsToPsiTwoFive.SCHEMA_NAMESPACE, getClass()
					.getClassLoader());
		} catch (JAXBException e) {
			logger.error("Could not create JAXBContext", e);
		}
		
		Unmarshaller u = null;
		try {
			u = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.error("Could not create Unmarshaller", e);
		}

		EntrySet entrySet = null;
		try {
			entrySet = (EntrySet) u.unmarshal(reader);
		} catch (JAXBException e) {
			logger.error("unmarshal operation failed", e);
		}
		
		final int entryCount = entrySet.getEntry().size();
		for (int i = 0; i < entryCount; i++)
			extractEntry(entrySet.getEntry().get(i));
	}

	/**
	 * Extracts PSI Entry Root Element.
	 */
	private void extractEntry(EntrySet.Entry entry) throws MapperException {
		final EntrySet.Entry.ExperimentList expList = entry.getExperimentList();
		extractExperimentList(expList);

		final EntrySet.Entry.InteractorList interactorList = entry.getInteractorList();
		extractInteractorList(interactorList);

		final EntrySet.Entry.InteractionList interactionList = entry.getInteractionList();
		extractInteractionList(interactionList);
	}

	/**
	 * Extracts Experiment List, and places into HashMap.
	 */
	private void extractExperimentList(EntrySet.Entry.ExperimentList expList) {
		if(expList != null) {
			final int count = expList.getExperimentDescription().size();
			for (int i = 0; i < count; i++) {
				final ExperimentType expType = expList.getExperimentDescription().get(i);
				experimentMap.put(Integer.toString(expType.getId()), expType);
			}
		}
	}

	/**
	 * Extracts PSI InteractorList, and places into HashMap.
	 */
	private void extractInteractorList(EntrySet.Entry.InteractorList interactorList) {
		logger.debug("Extracting Interactor List: Start");

		if (interactorList != null) {
			for (InteractorElementType cProtein : interactorList.getInteractor())
				interactorMap.put(Integer.toString(cProtein.getId()), cProtein);
		}

		logger.debug("Extracting Interactor List: End");
	}

	/**
	 * Extracts PSI Interaction List
	 */
	private void extractInteractionList(EntrySet.Entry.InteractionList interactionList) throws MapperException {
		logger.debug("Extracting Interaction List: Start");

		for (EntrySet.Entry.InteractionList.Interaction cInteraction : interactionList.getInteraction()) {
			Interaction interaction = new Interaction();
			final List<CvType> interactionTypes = cInteraction.getInteractionType();

			final InteractionElementType.ParticipantList pList = cInteraction.getParticipantList();
			final int pCount = pList.getParticipant().size();
			final List<Interactor> interactorList = new ArrayList<Interactor>();
			final Map<String, String> interactorRoles = new HashMap<String, String>();

			for (int j = 0; j < pCount; j++) {
				Interactor interactor = extractInteractorRefOrElement(pList, j);
				logger.debug("Getting interactor:  " + interactor);
				interactorList.add(interactor);

				ParticipantType participant = pList.getParticipant().get(j);
				ParticipantType.ExperimentalRoleList role = participant.getExperimentalRoleList();

				if (role != null) {
					for (ParticipantType.ExperimentalRoleList.ExperimentalRole expRole : role.getExperimentalRole()) {
						NamesType namesType = expRole.getNames();
						String roleName = namesType.getShortLabel();
						logger.debug("Storing role for:  " + interactor.getName() + " --> " + roleName);
						interactorRoles.put(interactor.getName(), roleName);
					}
				}
			}

			interaction.setInteractors(interactorList);
			interaction.setInteractionId(cInteraction.getId());

			List<Interaction> expDatalist = extractExperimentalData(cInteraction, interaction);

			// Add BAIT MAP / Names To all Interactions.
			for (int j = 0; j < expDatalist.size(); j++) {
				interaction = expDatalist.get(j);
				interaction.addAttribute(InteractionVocab.BAIT_MAP, interactorRoles);
				extractInteractionNamesXrefs(cInteraction, interaction);
				addInteractorType(interactionTypes, interaction);
			}

			logger.debug("Adding num interactions:  " + expDatalist.size());
			interactions.addAll(expDatalist);
		}

		logger.debug("Extracting Interaction List: End");
	}

	private void addInteractorType(List<CvType> interactionTypes,
			org.cytoscape.psi_mi.internal.model.Interaction interaction) {
		if (interactionTypes != null) {
			if (interactionTypes.size() == 1) {
				CvType interactionType = interactionTypes.get(0);
				NamesType namesType = interactionType.getNames();
				if (namesType != null) {
					String shortName = namesType.getShortLabel();
					if (shortName != null) {
						interaction.addAttribute(InteractionVocab.INTERACTION_TYPE_NAME, shortName);
					}
				}
			}
		}
	}

    /**
	 * Extracts Interaction Names.
	 */
	private void extractInteractionNamesXrefs(InteractionElementType cInteraction,
	                                          org.cytoscape.psi_mi.internal.model.Interaction interaction) {
		NamesType names = cInteraction.getNames();

		if (names != null) {
			String shortLabel = names.getShortLabel();
			String fullName = names.getFullName();

			if (shortLabel != null)
				interaction.addAttribute(InteractionVocab.INTERACTION_SHORT_NAME, shortLabel);

			if (fullName != null)
				interaction.addAttribute(InteractionVocab.INTERACTION_FULL_NAME, fullName);
		}

		XrefType xref = cInteraction.getXref();
		ExternalReference[] refs = extractExternalRefs(xref);

		if ((refs != null) && (refs.length > 0)) {
			logger.debug("Got refs:  " + refs.length);
			interaction.setExternalRefs(refs);
		}
	}

	/**
	 * Extracts Interactor From Reference or Element.
	 */
	private Interactor extractInteractorRefOrElement(InteractionElementType.ParticipantList pList, int j)
			throws MapperException {
		Interactor interactor;
		InteractorElementType cInteractor = null;
		ParticipantType participant = pList.getParticipant().get(j);
		Integer ref = participant.getInteractorRef();

		if (ref != null)
			cInteractor = interactorMap.get(ref.toString());
		else
			cInteractor = participant.getInteractor();

		interactor = createInteractor(cInteractor);

		return interactor;
	}

	/**
	 * Extracts Interactor Name
	 */
	private void extractInteractorName(InteractorElementType cProtein, Interactor interactor)
	    throws MapperException {
		NamesType names = cProtein.getNames();

		if (names != null) {
			String name = MapperUtil.normalizeText(MapperUtil.extractName(cProtein, interactor.getExternalRefs()));

			interactor.setName(name);

			String fullName = names.getFullName();
			interactor.addAttribute(InteractorVocab.FULL_NAME, fullName);
		}
	}

	/**
	 * Extracts All Interactor External References.
	 */
	private ExternalReference[] extractExternalRefs(XrefType xref) {
		ArrayList<ExternalReference> refList = new ArrayList<ExternalReference>();

		if (xref != null) {
			DbReferenceType primaryRef = xref.getPrimaryRef();
			createExternalReference(primaryRef.getDb(), primaryRef.getId(), refList);

			for (DbReferenceType secondaryRef : xref.getSecondaryRef()) {
				createExternalReference(secondaryRef.getDb(), secondaryRef.getId(), refList);
			}

			ExternalReference[] refs = new ExternalReference[refList.size()];
			refs = refList.toArray(refs);

			return refs;
		} else {
			return null;
		}
	}

	/**
	 * Creates ExternalReference.
	 */
	private void createExternalReference(String db, String id, List<ExternalReference> refList) {
		ExternalReference ref = new ExternalReference(db, id);
		refList.add(ref);
	}

	/**
	 * Extracts Experimental Data.
	 * <p/>
	 * Notes:  In PSI-MI, each interaction element can have 1 or more
	 * experimentDescriptions.  For each experimentDescription, we
	 * create a new DataServices Interaction object.
	 * <p/>
	 * In other words, a Data Services Interaction object contains
	 * data for one interaction, determined under exactly one experimental
	 * condition.
	 */
	private List<Interaction> extractExperimentalData(InteractionElementType cInteraction, Interaction interactionTemplate)
	    throws MapperException {
		InteractionElementType.ExperimentList expList = cInteraction.getExperimentList();
		List<Interaction> list = new ArrayList<Interaction>();

		if (expList != null) {
			int expCount = expList.getExperimentRefOrExperimentDescription().size();

			for (int i = 0; i < expCount; i++) {
				org.cytoscape.psi_mi.internal.model.Interaction interaction = cloneInteractionTemplate(interactionTemplate);
				Object expItem = expList.getExperimentRefOrExperimentDescription().get(i);
				ExperimentType expType = extractExperimentReferenceOrElement(expItem);
				String id = getPubMedId(expType);

				if (id != null) {
					interaction.addAttribute(InteractionVocab.PUB_MED_ID, id);
				}

				extractInteractionDetection(expType, interaction);
				list.add(interaction);
			}
		} else {
			throw new MapperException("Could not determine experimental "
			                          + "data for one of the PSI-MI interactions");
		}

		return list;
	}

	/**
	 * Clones the InteractionTemplate.  Only clones the Interactors
	 * contained within the interaction, and none of the Interaction
	 * attributes.
	 */
	private Interaction cloneInteractionTemplate(Interaction interactionTemplate) {
		Interaction interaction = new Interaction();
		List<Interactor> interactors = interactionTemplate.getInteractors();
		interaction.setInteractors(interactors);

		return interaction;
	}

	/**
	 * Extracts an Experiment Reference or Sub-Element.
	 */
	private ExperimentType extractExperimentReferenceOrElement(Object expItem) {
		if (expItem instanceof Integer) {
			String ref = "" + expItem;

			return experimentMap.get(ref);
		} else {
			return (ExperimentType) expItem;
		}
	}

	/**
	 * Gets Interaction Detection.
	 */
	private void extractInteractionDetection(final ExperimentType expDesc, final Interaction interaction) {
		String expSystem;

		if (expDesc != null) {
			final CvType detection = expDesc.getInteractionDetectionMethod();
			if(detection == null)
				return;
			
			final NamesType names = detection.getNames();
			expSystem = names.getShortLabel();

			if (expSystem != null) {
				interaction.addAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME, expSystem);
			}

			XrefType xref = detection.getXref();

			if (xref != null) {
				DbReferenceType primaryRef = xref.getPrimaryRef();

				if (primaryRef != null) {
					interaction.addAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_DB,
					                         primaryRef.getDb());
					interaction.addAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_ID,
					                         primaryRef.getId());
				}
			}
		}
	}

	/**
	 * Extracts Pub Med ID.
	 */
	private String getPubMedId(ExperimentType expDesc) {
		String id = null;

		if (expDesc != null) {
			BibrefType bibRef = expDesc.getBibref();

			if (bibRef != null) {
				XrefType xRef = bibRef.getXref();

				if (xRef != null) {
					DbReferenceType primaryRef = xRef.getPrimaryRef();

					if (primaryRef != null) {
						id = primaryRef.getId();
					}
				}
			}
		}

		return id;
	}

	/**
	 * Extracts Single PSI Interactor.
	 */
	private Interactor createInteractor(InteractorElementType cProtein) throws MapperException {
		Interactor interactor = new Interactor();
		extractOrganismInfo(cProtein, interactor);
		extractSequenceData(cProtein, interactor);

		ExternalReference[] refs = extractExternalRefs(cProtein.getXref());

		if ((refs != null) && (refs.length > 0)) {
			interactor.setExternalRefs(refs);
		}

		//  Set Local Id.
		String localId = "" + cProtein.getId();
		interactor.addAttribute(InteractorVocab.LOCAL_ID, localId);

		//  Set Interactor Name Last, as it may be dependent on
		//  external references.
		extractInteractorName(cProtein, interactor);
		extractCvType(cProtein, interactor);

		return interactor;
	}

	private void extractCvType(InteractorElementType cProtein, Interactor interactor) {
		CvType cvType = cProtein.getInteractorType();

		if (cvType != null) {
			interactor.setCvType(cvType);
		}
	}

	/**
	 * Extracts Sequence Data.
	 */
	private void extractSequenceData(InteractorElementType cProtein, Interactor interactor) {
		String sequence = cProtein.getSequence();

		if (sequence != null) {
			interactor.addAttribute(InteractorVocab.SEQUENCE_DATA, sequence);
		}
	}

	/**
	 * Extracts Organism Information.
	 */
	private void extractOrganismInfo(InteractorElementType cProtein, Interactor interactor) {
		InteractorElementType.Organism organism = cProtein.getOrganism();

		if (organism != null) {
			NamesType names = organism.getNames();
			String commonName = names.getShortLabel();
			String fullName = names.getFullName();
			int ncbiTaxID = organism.getNcbiTaxId();
            if (commonName != null && commonName.length() > 0) {
                interactor.addAttribute(InteractorVocab.ORGANISM_COMMON_NAME, commonName);
            }
            if (fullName != null && fullName.length() > 0) {
                interactor.addAttribute(InteractorVocab.ORGANISM_SPECIES_NAME, fullName);
            }
            if (ncbiTaxID > 0) {
                interactor.addAttribute(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID,
			                        Integer.toString(ncbiTaxID));
            }
        }
	}
}
