/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.psi_mi.internal.data_mapper;

import java.io.StringReader;
import java.math.BigInteger;
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
 * Maps PSI-MI Level 1 Document to Interaction Objects.
 *
 * @author Ethan Cerami
 * @author Nisha Vinod
 */
public class MapPsiOneToInteractions implements Mapper {
	private Map<String, ProteinInteractorType> interactorMap;
	private Map<String, ExperimentType> experimentMap;
	private List<Interaction> interactions;
	private String xml;

	/**
	 * Constructor.
	 *
	 * @param xml          XML Document.
	 * @param interactions ArrayList of Interaction objects.
	 */
	public MapPsiOneToInteractions(String xml, List<Interaction> interactions) {
		this.xml = xml;
		this.interactions = interactions;
	}

	/**
	 * Perform Mapping.
	 *
	 * @throws MapperException Problem Performing mapping.
	 */
	public void doMapping() throws MapperException {
		parseXml(xml);
	}

	/**
	 * Parses the PSI XML Document.
	 */
	private void parseXml(String content) throws MapperException {
		try {
			interactorMap = new HashMap<String, ProteinInteractorType>();
			experimentMap = new HashMap<String, ExperimentType>();

			StringReader reader = new StringReader(content);
			JAXBContext jc = JAXBContext.newInstance(MapInteractionsToPsiOne.SCHEMA_NAMESPACE, getClass().getClassLoader());
			Unmarshaller u = jc.createUnmarshaller();
			EntrySet entrySet = (EntrySet) u
			                                                                                                                 .unmarshal(reader);
			int entryCount = entrySet.getEntry().size();

			for (int i = 0; i < entryCount; i++) {
				EntrySet.Entry entry = entrySet.getEntry().get(i);
				extractEntry(entry);
			}
		} catch (JAXBException e) {
			throw new MapperException(e, "PSI-MI XML File is invalid:  " + e.getMessage());
		}
	}

	/**
	 * Extracts PSI Entry Root Element.
	 */
	private void extractEntry(EntrySet.Entry entry) throws MapperException {
		EntrySet.Entry.ExperimentList expList = entry.getExperimentList();
		extractExperimentList(expList);

		EntrySet.Entry.InteractorList interactorList = entry.getInteractorList();
		extractInteractorList(interactorList);

		EntrySet.Entry.InteractionList interactionList = entry.getInteractionList();
		extractInteractionList(interactionList);
	}

	/**
	 * Extracts Experiment List, and places into HashMap.
	 */
	private void extractExperimentList(EntrySet.Entry.ExperimentList expList) {
		if (expList != null) {
			for (ExperimentType expType : expList.getExperimentDescription()) {
				String id = expType.getId();
				experimentMap.put(id, expType);
			}
		}
	}

	/**
	 * Extracts PSI InteractorList, and places into HashMap.
	 */
	private void extractInteractorList(EntrySet.Entry.InteractorList interactorList) {
		if (interactorList != null) {
			for (ProteinInteractorType cProtein : interactorList.getProteinInteractor()) {
				String id = cProtein.getId();
				interactorMap.put(id, cProtein);
			}
		}
	}

	/**
	 * Extracts PSI Interaction List
	 */
	private void extractInteractionList(EntrySet.Entry.InteractionList interactionList)
	    throws MapperException {
		for (InteractionElementType cInteraction : interactionList.getInteraction()) {
			Interaction interaction = new Interaction();
			interaction.setInteractionId(cInteraction.getInteractionType().size());
            List<CvType> interactionTypes = cInteraction.getInteractionType();

			InteractionElementType.ParticipantList pList = cInteraction.getParticipantList();
			ArrayList<Interactor> interactorList = new ArrayList<Interactor>();
			Map<String, String> interactorRoles = new HashMap<String, String>();

			for (ProteinParticipantType participant : pList.getProteinParticipant()) {
				Interactor interactor = extractInteractorRefOrElement(participant);
				interactorList.add(interactor);

				String role = participant.getRole();

				if (role != null) {
					interactorRoles.put(interactor.getName(), role);
				}
			}

			interaction.setInteractors(interactorList);

			List<Interaction> list = extractExperimentalData(cInteraction, interaction);

			//  Add BAIT MAP / Names To all Interactions.
			for (Interaction interaction2 : list) {
				interaction2.addAttribute(InteractionVocab.BAIT_MAP, interactorRoles);
				extractInteractionNamesXrefs(cInteraction, interaction2);
                addInteractorType(interactionTypes, interaction2);
            }

            interactions.addAll(list);
		}
	}

    private void addInteractorType(List<CvType> interactionTypes, Interaction interaction) {
        if (interactionTypes != null) {
            if (interactionTypes.size() ==1) {
                CvType interactionType = interactionTypes.get(0);
                NamesType namesType = interactionType.getNames();
                if (namesType != null) {
                    String shortName = namesType.getShortLabel();
                    if (shortName != null) {
                        interaction.addAttribute(InteractionVocab.INTERACTION_TYPE_NAME,
                            shortName);
                    }
                }
            }
        }
    }


    /**
	 * Extracts Interaction Names.
	 */
	private void extractInteractionNamesXrefs(InteractionElementType cInteraction,
	                                          Interaction interaction) {
		NamesType names = cInteraction.getNames();

		if (names != null) {
			String shortLabel = names.getShortLabel();
			String fullName = names.getFullName();

			if (shortLabel != null) {
				interaction.addAttribute(InteractionVocab.INTERACTION_SHORT_NAME, shortLabel);
			}

			if (fullName != null) {
				interaction.addAttribute(InteractionVocab.INTERACTION_FULL_NAME, fullName);
			}
		}

		XrefType xref = cInteraction.getXref();
		ExternalReference[] refs = this.extractExternalRefs(xref);

		if ((refs != null) && (refs.length > 0)) {
			interaction.setExternalRefs(refs);
		}
	}

	/**
	 * Extracts Interactor From Reference or Element.
	 */
	private Interactor extractInteractorRefOrElement(ProteinParticipantType participant) throws MapperException {
		Interactor interactor = null;
		ProteinInteractorType cInteractor;
		RefType ref = participant.getProteinInteractorRef();

		if (ref != null) {
			String key = ref.getRef();
			cInteractor = interactorMap.get(key);

			if (cInteractor == null) {
				throw new MapperException("No Interactor Found for " + "proteinInteractorRef:  "
				                          + key);
			}
		} else {
			cInteractor = participant.getProteinInteractor();
		}

		if (cInteractor != null) {
			interactor = createInteractor(cInteractor);
		}

		return interactor;
	}

	/**
	 * Extracts Interactor Name
	 */
	private void extractInteractorName(ProteinInteractorType cProtein, Interactor interactor)
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
            if (primaryRef != null) {
                createExternalReference(primaryRef.getDb(), primaryRef.getId(), refList);
            }

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
	private List<Interaction> extractExperimentalData(InteractionElementType cInteraction,
	                                          Interaction interactionTemplate)
	    throws MapperException {
		InteractionElementType.ExperimentList expList = cInteraction.getExperimentList();
		List<Interaction> list = new ArrayList<Interaction>();

		if (expList != null) {
			for (Object expItem : expList.getExperimentRefOrExperimentDescription()) {
				Interaction interaction = cloneInteractionTemplate(interactionTemplate);
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
		if (expItem instanceof RefType) {
			RefType refType = (RefType) expItem;

			return experimentMap.get(refType.getRef());
		} else {
			return (ExperimentType) expItem;
		}
	}

	/**
	 * Gets Interaction Detection.
	 */
	private void extractInteractionDetection(ExperimentType expDesc, Interaction interaction) {
		String expSystem = null;

		if (expDesc != null) {
			CvType detection = expDesc.getInteractionDetection();
            if (detection != null) {
                NamesType names = detection.getNames();
                if (names != null) {
                    expSystem = names.getShortLabel();

                    if (expSystem != null) {
                        interaction.addAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME, expSystem);
                    }
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
	private Interactor createInteractor(ProteinInteractorType cProtein) throws MapperException {
		Interactor interactor = new Interactor();
		extractOrganismInfo(cProtein, interactor);
		extractSequenceData(cProtein, interactor);

		ExternalReference[] refs = extractExternalRefs(cProtein.getXref());

		if ((refs != null) && (refs.length > 0)) {
			interactor.setExternalRefs(refs);
		}

		//  Set Local Id.
		String localId = cProtein.getId();
		interactor.addAttribute(InteractorVocab.LOCAL_ID, localId);

		//  Set Interactor Name Last, as it may be dependent on
		//  external references.
		extractInteractorName(cProtein, interactor);

		return interactor;
	}

	/**
	 * Extracts Sequence Data.
	 */
	private void extractSequenceData(ProteinInteractorType cProtein, Interactor interactor) {
		String sequence = cProtein.getSequence();

		if (sequence != null) {
			interactor.addAttribute(InteractorVocab.SEQUENCE_DATA, sequence);
		}
	}

	/**
	 * Extracts Organism Information.
	 */
	private void extractOrganismInfo(ProteinInteractorType cProtein, Interactor interactor) {
		ProteinInteractorType.Organism organism = cProtein.getOrganism();

		if (organism != null) {
			NamesType names = organism.getNames();
			String commonName = names.getShortLabel();
			String fullName = names.getFullName();
			BigInteger ncbiTaxID = organism.getNcbiTaxId();
            if (commonName != null && commonName.length() > 0) {
                interactor.addAttribute(InteractorVocab.ORGANISM_COMMON_NAME, commonName);
            }
            if (fullName != null && fullName.length() > 0) {
                interactor.addAttribute(InteractorVocab.ORGANISM_SPECIES_NAME, fullName);
            }
            if (ncbiTaxID != null) {
                interactor.addAttribute(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID,
                    ncbiTaxID.toString());
            }
        }
    }
}
