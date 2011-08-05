/** Copyright (c) 2010 University of Toronto (UofT)
 ** and Memorial Sloan-Kettering Cancer Center (MSKCC).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/
package org.cytoscape.biopax.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.dataSource;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.publicationXref;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.cytoscape.biopax.internal.ExternalLink;
import org.cytoscape.biopax.internal.MapBioPaxToCytoscapeImpl;
import org.cytoscape.biopax.internal.util.ParentFinder;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BioPax Utility Class - is a BioPAX Model Adapter 
 * that also defines additional constants. 
 *
 * @author Ethan Cerami, Rex, Arman and Igor Rodchenkov
 */
public class BioPaxUtil {
	private static final Map<String,String> plainEnglishMap;
	private static final Map<String,String> cellLocationMap;
	private static final Map<String,String> chemModificationsMap;
	private static final EditorMap editorMapLevel3;
	private static final EditorMap editorMapLevel2;
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxUtil.class);
	
	protected BioPaxUtil() {}
	
	
    private static Map<Long, Model> networkModelMap = new HashMap<Long, Model>();

    public static final String BIOPAX_MODEL_STRING = "biopax.model.xml";
    
    public static final String DEFAULT_CHARSET = "UTF-8";
	
    public static final int MAX_DISPLAY_STRING_LEN = 25;

	public static final String NULL_ELEMENT_TYPE = "BioPAX Element";
	
	/**
	 * BioPAX Class:  phosphorylation site
	 */
	public static final String PHOSPHORYLATION_SITE = "phosphorylation site";

	/**
	 * BioPAX Class:  protein phosphorylated
	 */
	public static final String PROTEIN_PHOSPHORYLATED = "protein-phosphorylated";
	

	static  {
		editorMapLevel2 = new SimpleEditorMap();
		editorMapLevel3 = new SimpleEditorMap(BioPAXLevel.L3);
		
		plainEnglishMap = new HashMap<String,String>();
		// all keys are lower case!
		plainEnglishMap.put("protein", "Protein");
		plainEnglishMap.put("smallmolecule", "Small Molecule");
		plainEnglishMap.put("physicalentity", "Physical Entity");
		plainEnglishMap.put("complex", "Complex");
		plainEnglishMap.put("dna", "DNA");
		plainEnglishMap.put("rna", "RNA");
		plainEnglishMap.put("interaction", "Interaction");
		plainEnglishMap.put("physicalinteraction", "Physical Interaction");
		plainEnglishMap.put("control", "Control");
		plainEnglishMap.put("catalysis", "Catalysis");
		plainEnglishMap.put("modulation", "Modulation");
		plainEnglishMap.put("conversion", "Conversion");
		plainEnglishMap.put("biochemicalreaction", "Biochemical Reaction");
		plainEnglishMap.put("molecularinteraction", "Molecular Interaction");
		plainEnglishMap.put("complexassembly", "Complex Assembly");
		plainEnglishMap.put("transportwithbiochemicalreaction", "Transport with Biochemical Reaction");
		plainEnglishMap.put("transport", "Transport");
		plainEnglishMap.put("transportwithbiochemicalreaction", "Transport with Biochemical Reaction");
		plainEnglishMap.put("geneticinteraction", "Genetic Interaction");
		plainEnglishMap.put("templatereaction", "Template Reaction");
		plainEnglishMap.put("degradation", "Degradation");
		// chemical modifications
		plainEnglishMap.put("acetylation site", "Acetylation Site");
		plainEnglishMap.put("glycosylation site", "Glycosylation Site");
		plainEnglishMap.put("phosphorylation site", "Phosphorylation Site");
		plainEnglishMap.put("sumoylation site", "Sumoylation Site");
		plainEnglishMap.put("ubiquitination site", "Ubiquitination Site");
		// cellular locations
		plainEnglishMap.put("cellular component unknown", "Cellular Component Unknown");
		plainEnglishMap.put("centrosome", "Centrosome");
		plainEnglishMap.put("cytoplasm", "Cytoplasm");
		plainEnglishMap.put("endoplasmic reticulum", "Endoplasmic Reticulum");
		plainEnglishMap.put("endosome", "Endosome");
		plainEnglishMap.put("extracellular", "Extracellular");
		plainEnglishMap.put("golgi apparatus", "Golgi Apparatus");
		plainEnglishMap.put("mitochondrion", "Mitochondrion");
		plainEnglishMap.put("nucleoplasm", "Nucleoplasm");
		plainEnglishMap.put("nucleus", "Nucleus");
		plainEnglishMap.put("plasma membrane", "Plasma Membrane");
		plainEnglishMap.put("ribosome", "Ribosome");
		plainEnglishMap.put("transmembrane", "Transmembrane");
		
		// the following is for node labels
		cellLocationMap = new HashMap<String, String>();
		cellLocationMap.put("cellular component unknown", "");
		cellLocationMap.put("centrosome", "CE");
		cellLocationMap.put("cytoplasm", "CY");
		cellLocationMap.put("endoplasmic reticulum", "ER");
		cellLocationMap.put("endosome", "EN");
		cellLocationMap.put("extracellular", "EM");
		cellLocationMap.put("golgi apparatus", "GA");
		cellLocationMap.put("mitochondrion", "MI");
		cellLocationMap.put("nucleoplasm", "NP");
		cellLocationMap.put("nucleus", "NU");
		cellLocationMap.put("plasma membrane", "PM");
		cellLocationMap.put("ribosome", "RI");
		cellLocationMap.put("transmembrane", "TM");
		
		chemModificationsMap = new HashMap<String, String>();
		chemModificationsMap.put("acetylation site", "A");
		chemModificationsMap.put("glycosylation site", "G");
		chemModificationsMap.put("phosphorylation site", "P");
		chemModificationsMap.put("proteolytic cleavage site", "PCS");
		chemModificationsMap.put("sumoylation site", "S");
		chemModificationsMap.put("ubiquitination site", "U");	
	}
	

	/**
	 * Constructor.
	 *
	 * @param in BioPAX data file name.
	 * @return BioPaxUtil new instance (containing the imported BioPAX data)
	 * @throws FileNotFoundException 
	 */
	public static Model read(final InputStream in) throws FileNotFoundException {
		try {
			return new SimpleIOHandler().convertFromOWL(in);
		} catch (Exception e) {
			log.warn("Import failed: " + e);
		}
		return null;
	}

	/**
	 * Converts the specified type into "Plain English".
	 * For example, the type "biochemicalReaction" is converted to
	 * "Biochemical Reaction".
	 * <p/>
	 * If the type is not know, the origianl argument type is simply returned.
	 *
	 * @param type BioPAX Type String.
	 * @return BioPAX Type String, in "Plain English".
	 */
	public static String getTypeInPlainEnglish(String type) {

		String plainEnglish = plainEnglishMap.get(type.toLowerCase());

		if (plainEnglish == null) {
			return type;
		} else {
			return plainEnglish;
		}
	}

	
	public static String getType(BioPAXElement bpe) {
		if(bpe instanceof physicalEntityParticipant) {
			return getType(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		}

		return (bpe != null) 
			? getTypeInPlainEnglish(bpe.getModelInterface().getSimpleName())
			: NULL_ELEMENT_TYPE;	
	}
	
	
	public Map<String,String> BioPaxCellularLocations() {
		return cellLocationMap;
	}
	
	public Map<String,String> BioPaxChemicalModifications() {
		return chemModificationsMap;
	}
	
	/**
	 * Gets or infers the name of the node. 
	 * 
	 * @param bpe BioPAX Element
	 * @return
	 */
	public static String getNodeName(BioPAXElement bpe) {

		if(bpe == null) {
			return "";
		}
				
		String nodeName = getShortName(bpe);

		if ((nodeName != null) && (nodeName.length() > 0)) {
			return nodeName;
		}

		nodeName = getStandardName(bpe);
		if ((nodeName != null) && (nodeName.length() > 0)) {
			return nodeName;
		}

		Collection<String> names = getSynonymList(bpe);
		if (names != null && !names.isEmpty()) {
			return getTheShortestString(names);
		}

		return getLocalPartRdfId(bpe);
	}
	
	
	public static String generateId(CyNetwork network, BioPAXElement bpe) {
		String id = (bpe instanceof physicalEntityParticipant 
				&& ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY()!=null)
					? ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY().getRDFId() 
					: bpe.getRDFId();
		return network.getSUID() + 
				"" + id.hashCode() + "-" + getLocalPartRdfId(bpe);
	}

	
	public static String getLocalPartRdfId(BioPAXElement bpe) {
		if(bpe == null) return "";
		
		// also fix pEPs
		String id = (bpe instanceof physicalEntityParticipant 
				&& ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY()!=null)
					? ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY().getRDFId() 
					: bpe.getRDFId();
					
		return id.replaceFirst("^.+#", "");
	}
	
	// get the shortest string
	public static String getTheShortestString(Collection<String> nameList) {
		String shortest = null;
		if (nameList != null && !nameList.isEmpty()) {
			int minLength = -1;
			for (String name: nameList) {
				if ( name.length() < minLength || minLength == -1) {
					minLength = name.length();
					shortest = name;
				}
			}
		}
		return shortest;
	}
	
	
	/**
	 * Attempts to get the value of any of the BioPAX properties
	 * in the list.
	 * 
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * @return the value or null
	 */
	public static Object getValue(CyNetwork network, BioPAXElement bpe, String... properties) {
		for (String property : properties) {
			try {
				Method method = bpe.getModelInterface().getMethod(
						"get" + property.substring(0, 1).toUpperCase()
								+ property.substring(1).replace('-', '_'));
				Object invoke = method.invoke(bpe);
				if (invoke != null) {
					return invoke;
				}
			} catch (Exception e) {
				if(log.isDebugEnabled()) {
					// this is often OK, as we guess L2 or L3 properties...
					log.debug("Ignore property " + property + " for " 
						+ BioPaxUtil.generateId(network, bpe) + ": " + e);
				}
			}
		}

		return null;
	}
	
	
	/**
	 * Attempts to get the values of specified BioPAX properties.
	 * 
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * @return the set of property values or null
	 */
	public static Collection<?> getValues(CyNetwork network, BioPAXElement bpe, String... properties) {
		Collection<Object> col = new HashSet<Object>();
		
		for (String property : properties) {
			try {
				Method method = bpe.getModelInterface().getMethod(
						"get" + property.substring(0, 1).toUpperCase()
								+ property.substring(1).replace('-', '_'));
				
				Object invoke = method.invoke(bpe);
				if (invoke != null) {
					// return value can be collection or Object
					if (invoke instanceof Collection) {
						col.addAll((Collection) invoke);
					} else {
						col.add(invoke);
					}
				}
			} catch (Exception e) {
				if(log.isDebugEnabled()) {
					log.debug("Cannot get value of '" + property + "' for "
						+ BioPaxUtil.generateId(network, bpe) + ": " + e);
				}
			}
		}
		
		return col;
	}
	
	
	/**
	 * Gets the Short Name (or Display Name).
	 *
	 * @param bpe BioPAX element
	 * @return short name field, or null if not available.
	 */
	public static String getShortName(BioPAXElement bpe) {
		String shortName = null;
		
		if(bpe instanceof Named) {
			shortName = ((Named)bpe).getDisplayName();
		} else if(bpe instanceof sequenceFeature) {
			shortName = ((sequenceFeature)bpe).getSHORT_NAME();
		} else if(bpe instanceof physicalEntityParticipant){ // fix PEPs
			shortName = getShortName(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof entity) {
			shortName = ((entity)bpe).getSHORT_NAME();
		}
		
		return shortName;
	}

	/**
	 * Gets the Name Field.
	 *
	 * @param bpe BioPAX element
	 * @return name field, or null if not available.
	 */
	public static String getStandardName(BioPAXElement bpe) {
		String stdName = null;
		if(bpe instanceof Named) {
			stdName = ((Named)bpe).getStandardName();
		} else if(bpe instanceof sequenceFeature) {
			stdName = ((sequenceFeature)bpe).getNAME();
		} else if(bpe instanceof physicalEntityParticipant){
			stdName = getStandardName(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof entity) {
			stdName = ((entity)bpe).getNAME();
		} else if(bpe instanceof bioSource) {
			stdName = ((bioSource)bpe).getNAME();
		} else if(bpe instanceof dataSource) {
			stdName = ((dataSource)bpe).getNAME().toString();
		} 
		return stdName;
	}

	/**
	 * Gets synonym names.
	 *
	 * @param bpe BioPAX element
	 * @return Collection of Synonym String Objects.
	 */
	public static Collection<String> getSynonymList(BioPAXElement bpe) {
		Collection<String> names = new HashSet<String>();
		
		if(bpe instanceof Named) {
			names = ((Named)bpe).getName();
		} else if(bpe instanceof sequenceFeature) {
			names = ((sequenceFeature)bpe).getSYNONYMS();
		} else if(bpe instanceof physicalEntityParticipant){
			names = getSynonymList(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof entity) {
			names = ((entity)bpe).getSYNONYMS();
		}
		
		return names;
	}

	/**
	 * Gets the Organism Name.
	 *
	 * @param bpe BioPAX element
	 * @return organism field, or null if not available.
	 */
	public static String getOrganismName(CyNetwork network, BioPAXElement bpe) {
		String organism = null;
		
		if(bpe instanceof physicalEntityParticipant) {
			return getOrganismName(network, ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		}
		
		BioPAXElement bs = (BioPAXElement) getValue(network, bpe, "ORGANISM", "organism");
		if (bs != null) {
			organism = getNodeName(bs);
		} 

		return organism;
	}

	/**
	 * If exist, gets all data sources 
	 * (according to the BioPAX spec. there should be only one...)
	 * names as "comment : name"...
	 * 
	 * @param bpe BioPAX element
	 * @return data source names
	 */
	public static String getDataSource(BioPAXElement bpe) {
		StringBuffer sb = new StringBuffer();
		
		if(bpe instanceof physicalEntityParticipant) {
			return getDataSource(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if (bpe instanceof entity) {
			Collection<dataSource> datasources = ((entity)bpe).getDATA_SOURCE();
			for(dataSource ds : datasources) {
				if(ds.getCOMMENT() != null) 
					sb.append(ds.getCOMMENT().toString());
				sb.append(" : ").append(getNodeName(ds)).append(' '); 
			}
		} else if(bpe instanceof Entity) {
			Collection<Provenance> datasources = ((Entity)bpe).getDataSource();
			for(Provenance pr : datasources) {
				if(pr.getComment() != null) 
					sb.append(pr.getComment().toString());
				sb.append(" : ").append(getNodeName(pr)).append(' '); 
			}
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Gets the NCBI Taxonomy ID.
	 *
	 * @param bpe BioPAX element
	 * @return taxonomyId, or -1, if not available.
	 */
	public static int getOrganismTaxonomyId(CyNetwork network, BioPAXElement bpe) {
		int taxonomyId = -1;

		if(bpe instanceof physicalEntityParticipant) {
			return getOrganismTaxonomyId(network, ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		}
		
		try {
			Object bs = getValue(network, bpe, "ORGANISM", "organism");
			if (bs instanceof BioSource) {
				Set<Xref> xrefs = ((BioSource)bs).getXref();
				if(!xrefs.isEmpty()) {
					Xref tx = xrefs.iterator().next();
					taxonomyId = Integer.parseInt(tx.getId());
				}
			} else if(bs instanceof bioSource){
				taxonomyId = Integer.parseInt(((bioSource)bs).getTAXON_XREF().getID());
			}
		} catch (Exception e) {
			taxonomyId = -1;
		}

		return taxonomyId;
	}

	/**
	 * Gets the Comment field.
	 *
	 * @param bpe BioPAX element
	 * @return comment field or null, if not available.
	 */
	public static String getComment(BioPAXElement bpe) {
		if(bpe instanceof Level3Element) {
			return ((Level3Element)bpe).getComment().toString();
		} else if(bpe instanceof Level2Element){
			return ((Level2Element)bpe).getCOMMENT().toString();
		}
		return null;
	}

	/**
	 * Gets the Availability Field.
	 *
	 * @param bpe BioPAX element
	 * @return availability field or null, if not available.
	 */
	public static String getAvailability(BioPAXElement bpe) {
		
		if(bpe instanceof physicalEntityParticipant) {
			return getAvailability(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		}
		
		String availability = null;

		if (bpe instanceof entity) {
			availability = ((entity)bpe).getAVAILABILITY().toString();
		} else if(bpe instanceof Entity) {
			availability = ((Entity)bpe).getAvailability().toString();
		}

		return availability;
	}

	/**
	 * Gets an ArrayList of all Unification XRefs.
	 *
	 * @param bpe BioPAX element
	 * @return ArrayList of ExternalLink Objects.
	 */
	public static List<ExternalLink> getUnificationXRefs(BioPAXElement bpe) {
		
		if(bpe instanceof physicalEntityParticipant) {
			return getUnificationXRefs(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof org.biopax.paxtools.model.level2.XReferrable) {
			return extractXrefs(new ClassFilterSet<unificationXref>(
					((org.biopax.paxtools.model.level2.XReferrable)bpe).getXREF(),
						unificationXref.class) );
		} else if(bpe instanceof org.biopax.paxtools.model.level3.XReferrable) {
			List<ExternalLink> erefs = new ArrayList<ExternalLink>();
			erefs.addAll(extractXrefs(new ClassFilterSet<UnificationXref>(
					((org.biopax.paxtools.model.level3.XReferrable)bpe).getXref(),
					UnificationXref.class) ));
			if(bpe instanceof SimplePhysicalEntity && ((SimplePhysicalEntity)bpe).getEntityReference() != null) {
				erefs.addAll(extractXrefs(new ClassFilterSet<UnificationXref>(
						((SimplePhysicalEntity)bpe).getEntityReference().getXref(),
						UnificationXref.class) ));
			}
			return erefs;
		}
		return new ArrayList<ExternalLink>();
	}

	/**
	 * Gets an ArrayList of all Relationship XRefs.
	 *
	 * @param bpe BioPAX element
	 * @return ArrayList of ExternalLink Objects.
	 */
	public static List<ExternalLink> getRelationshipXRefs(BioPAXElement bpe) {
		
		if(bpe instanceof physicalEntityParticipant) {
			return getRelationshipXRefs(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof org.biopax.paxtools.model.level2.XReferrable) {
			return extractXrefs(new ClassFilterSet<relationshipXref>(
					((org.biopax.paxtools.model.level2.XReferrable)bpe).getXREF(),
						relationshipXref.class) );
		} else if(bpe instanceof org.biopax.paxtools.model.level3.XReferrable) {
			List<ExternalLink> erefs = new ArrayList<ExternalLink>();
			erefs.addAll(extractXrefs(new ClassFilterSet<RelationshipXref>(
					((org.biopax.paxtools.model.level3.XReferrable)bpe).getXref(),
					RelationshipXref.class) ));
			if(bpe instanceof SimplePhysicalEntity && ((SimplePhysicalEntity)bpe).getEntityReference() != null) {
				erefs.addAll(extractXrefs(new ClassFilterSet<RelationshipXref>(
						((SimplePhysicalEntity)bpe).getEntityReference().getXref(),
						RelationshipXref.class) ));
			}
			return erefs;
		}
		return new ArrayList<ExternalLink>();
	}

	
	/**
	 * Gets an ArrayList of all Publication XRefs.
	 *
	 * @param bpe BioPAX element
	 * @return ArrayList of ExternalLink Objects.
	 */
	public static List<ExternalLink> getPublicationXRefs(BioPAXElement bpe) {
		
		if(bpe instanceof physicalEntityParticipant) {
			return getPublicationXRefs(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof org.biopax.paxtools.model.level2.XReferrable) {
			return extractXrefs(new ClassFilterSet<publicationXref>(
					((org.biopax.paxtools.model.level2.XReferrable)bpe).getXREF(),
					publicationXref.class) );
		} else if(bpe instanceof org.biopax.paxtools.model.level3.XReferrable) {
			List<ExternalLink> erefs = new ArrayList<ExternalLink>();
			erefs.addAll(extractXrefs(new ClassFilterSet<PublicationXref>(
					((org.biopax.paxtools.model.level3.XReferrable)bpe).getXref(),
					PublicationXref.class) ));
			if(bpe instanceof SimplePhysicalEntity && ((SimplePhysicalEntity)bpe).getEntityReference() != null) {
				erefs.addAll(extractXrefs(new ClassFilterSet<PublicationXref>(
						((SimplePhysicalEntity)bpe).getEntityReference().getXref(),
						PublicationXref.class) ));
			}
			return erefs;
		}
		
		return new ArrayList<ExternalLink>();
	}
	
	
	/**
	 * Gets an ArrayList of all XRefs.
	 *
	 * @return ArrayList of ExternalLink Objects.
	 */
	public static List<ExternalLink> getAllXRefs(BioPAXElement bpe) {
		if(bpe instanceof physicalEntityParticipant) {
			return getAllXRefs(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY());
		} else if(bpe instanceof org.biopax.paxtools.model.level2.XReferrable) {
			return extractXrefs(((org.biopax.paxtools.model.level2.XReferrable)bpe).getXREF());
		} else if(bpe instanceof org.biopax.paxtools.model.level3.XReferrable) {
			List<ExternalLink> erefs = new ArrayList<ExternalLink>();
			erefs.addAll(extractXrefs(((org.biopax.paxtools.model.level3.XReferrable)bpe).getXref()));
			if(bpe instanceof SimplePhysicalEntity && ((SimplePhysicalEntity)bpe).getEntityReference() != null) {
				erefs.addAll(extractXrefs(((SimplePhysicalEntity)bpe).getEntityReference().getXref()));
			}
			return erefs;
		}
		return new ArrayList<ExternalLink>();
	}

	private static List<ExternalLink> extractXrefs(Collection<? extends BioPAXElement> xrefs) {
		List<ExternalLink> dbList = new ArrayList<ExternalLink>();

		for (BioPAXElement ref: xrefs) {		
			String db = null;
			String id = null;
			String relType = null;
			String title = null;
			String year = null;
			String author = null;
			String url = null;
			String source = null;
			
			if(ref instanceof xref) {
				xref x = (xref)ref;
				db = x.getDB();
				String ver = x.getID_VERSION();
				id = x.getID(); // + ((ver!=null) ? "_" + ver : "");
				if(x instanceof relationshipXref) {
					relType = ((relationshipXref)x).getRELATIONSHIP_TYPE();
				}
				if(x instanceof publicationXref) {
					publicationXref px = (publicationXref)x;
					author = px.getAUTHORS().toString();
					title = px.getTITLE();
					source = px.getSOURCE().toString();
					url =px.getURL().toString();
					year = px.getYEAR() + "";
				}
			} else if(ref instanceof Xref) {
				Xref x = (Xref)ref;
				db = x.getDb();
				String ver = x.getIdVersion();
				id = x.getId(); // + ((ver!=null) ? "_" + ver : "");
				if(x instanceof RelationshipXref) {
					RelationshipTypeVocabulary v = ((RelationshipXref)x).getRelationshipType();
					if(v != null) relType = v.getTerm().toString();
				}
				if(x instanceof PublicationXref) {
					PublicationXref px = (PublicationXref)x;
					author = px.getAuthor().toString();
					title = px.getTitle();
					source = px.getSource().toString();
					url =px.getUrl().toString();
					year = px.getYear() + "";
				}
			}

			if ((db != null) && (id != null)) {
				ExternalLink link = new ExternalLink(db, id);
				link.setAuthor(author);
				link.setRelType(relType);
				link.setTitle(title);
				link.setYear(year);
				link.setSource(source);
				link.setUrl(url);
				dbList.add(link);
			}
		}

		return dbList;
	}
	
	/**
	 * Gets the BioPAX Classes to Property Editors Map (PaxTools)
	 * 
	 * @param bioPAXLevel
	 * @return
	 */
	public static EditorMap getEditorMap(BioPAXLevel bioPAXLevel) {
		return (bioPAXLevel == BioPAXLevel.L2) ? editorMapLevel2 : editorMapLevel3;
	}
	
	/**
	 * Gets the joint set of all known subclasses of the specified BioPAX types.
	 * 
	 * @param classes BioPAX (PaxTools Model Interfaces) Classes
	 * @return
	 */
	public static Collection<Class> getSubclassNames(Class<? extends BioPAXElement>... classes) {
		Collection<Class> subclasses = new HashSet<Class>();
		
		for (Class<? extends BioPAXElement> c : classes) {
			if (Level3Element.class.isAssignableFrom(c)) {
				subclasses.addAll(editorMapLevel3.getKnownSubClassesOf(c));
			} else {
				subclasses.addAll(editorMapLevel2.getKnownSubClassesOf(c));
			}
		}
		
		return subclasses;
	}


	/**
	 * Creates a name for to the BioPAX model
	 * using its top-level pathway name(s). 
	 * 
	 * @param model
	 * @return
	 */
	public static String getName(Model model) {		
		StringBuffer modelName = new StringBuffer();
		
		if(model.getLevel() == BioPAXLevel.L3) {
			Collection<Pathway> pws = model.getObjects(Pathway.class);
			for(Pathway pw: pws) {
				if(pw.getPathwayComponentOf().isEmpty()
						&& pw.getParticipantOf().isEmpty()
						&& pw.getControlledOf().isEmpty()) {
					modelName.append(" ").append(getNodeName(pw)); 
				}
			}
			if(modelName.length()==0) {
				Collection<Interaction> itrs = model.getObjects(Interaction.class);
				for(Interaction it: itrs) {
					if(it.getPathwayComponentOf().isEmpty()
							&& it.getControlledOf().isEmpty()
							&& it.getParticipantOf().isEmpty()) {
						modelName.append(" ").append(getNodeName(it));
					}
				}	
			}
		} else { // Level 1 and 2
			Collection<pathway> pws = model.getObjects(pathway.class);
			for(pathway pw: pws) {
				if(pw.isPATHWAY_COMPONENTSof().isEmpty()
						&& pw.isPARTICIPANTSof().isEmpty()
						&& pw.isCONTROLLEDOf().isEmpty()) {
					modelName.append(" ").append(getNodeName(pw));
				}
			}
			if(modelName.length()==0) {
				Collection<interaction> itrs = model.getObjects(interaction.class);
				for(interaction it: itrs) {
					if(it.isPATHWAY_COMPONENTSof().isEmpty()
							&& it.isPARTICIPANTSof().isEmpty()
							&& it.isCONTROLLEDOf().isEmpty()) {
						modelName.append(" ").append(getNodeName(it));
					}
				}	
			}
		}
		
		return modelName.toString().trim();
	}
	
	/**
	 * Gets all the objects of provided BioPAX types.
	 * 
	 * @param model BioPAX (PaxTools) model
	 * @param classes query BioPAX types - e.g. Protein.class, Complex.class
	 * @return
	 */
	public static Set<? extends BioPAXElement> getObjects(Model model, Class<? extends BioPAXElement>... classes) {
		Set<BioPAXElement> coll = new HashSet<BioPAXElement>();
		if (model != null) {
			for (Class<? extends BioPAXElement> c : classes) {
				coll.addAll(model.getObjects(c));
			}
		}
		return coll;
	}


	/**
	 * For a BioPAX element, 
	 * find parent pathways names.
	 * 
	 * @param bpe
	 * @param model 
	 * @return
	 */
	/* TODO re-factoring (pre-calculate pathway membership for all entities, save in the Map, get from the Map...)
	public static Set<String> getParentPathwayName(BioPAXElement bpe, Model model) {
		Set<String> pathwayNames = new HashSet<String>();
		
		Set<? extends BioPAXElement> procs = BioPaxUtil.getObjects(model, pathway.class, Pathway.class);
		
		// remove sub-pathways (finding all parents is sacrificed to performance...)
		Set<BioPAXElement> topPathways = new HashSet<BioPAXElement>();
		for(BioPAXElement p : procs ) {
			if( (p instanceof pathway 
				&& ((pathway)p).isPATHWAY_COMPONENTSof().isEmpty()
				&& ((pathway)p).isPARTICIPANTSof().isEmpty()
				&& ((pathway)p).isCONTROLLEDOf().isEmpty()) 
				||
				(p instanceof Pathway 
				&& ((Pathway)p).getPathwayComponentOf().isEmpty()
				&& ((Pathway)p).getParticipantOf().isEmpty()
				&& ((Pathway)p).getControlledOf().isEmpty()))
			{
				topPathways.add(p);
			}
		}
		
		Set<BioPAXElement> pathways = fetchParentNodeNames(bpe, topPathways);
				
		if(!pathways.isEmpty()) {
			for(BioPAXElement pw : pathways) {
				pathwayNames.add(getNodeName(pw));
			}
		} else if(bpe instanceof pathway || bpe instanceof Pathway) {
			pathwayNames.add(getNodeName(bpe));
		}
			
		
		if(!pathwayNames.isEmpty()) {
			return pathwayNames;
		}
		
		// otherwise, find top-level interactions
		procs = BioPaxUtil.getObjects(model, interaction.class, Interaction.class);
		for(BioPAXElement itr : procs ) {
			if( (itr instanceof interaction 
				&& ((interaction)itr).isPATHWAY_COMPONENTSof().isEmpty()
				&& ((interaction)itr).isPARTICIPANTSof().isEmpty()
				&& ((interaction)itr).isCONTROLLEDOf().isEmpty()) 
				||
				(itr instanceof Interaction 
				&& ((Interaction)itr).getPathwayComponentOf().isEmpty()
				&& ((Interaction)itr).getParticipantOf().isEmpty()
				&& ((Interaction)itr).getControlledOf().isEmpty()))
			{
				topPathways.add(itr);
			}
		}
		pathways = fetchParentNodeNames(bpe, topPathways);
		if(!pathways.isEmpty()) {
			for(BioPAXElement pw : pathways) {
				pathwayNames.add(getNodeName(pw));
			}
		} else if(bpe instanceof interaction || bpe instanceof Interaction) {
			pathwayNames.add(getNodeName(bpe));
		}
		
		return pathwayNames;
	}
	*/
	
	/**
	 * Finds element's parents (CyNode names) in the collection.
	 * 
	 * Use with caution: this method can get computationally expensive or loop!
	 * 
	 * @param bpe
	 * @param procs - candidates
	 * @return
	 */
	public static Set<BioPAXElement> fetchParentNodeNames(BioPAXElement bpe, Set<? extends BioPAXElement> procs) {
		Set<BioPAXElement> parents = new HashSet<BioPAXElement>();
		EditorMap editorMap = (bpe instanceof Level2Element) ? editorMapLevel2 : editorMapLevel3;
		ParentFinder parentFinder = new ParentFinder(editorMap);
		for (BioPAXElement proc : procs) {
			if(!parents.contains(proc) && parentFinder.isParentChild(proc, bpe))
				parents.add(proc);
		}
		return parents;
	}
	
	// convenience proc.
	private static String joinNames(Collection<? extends BioPAXElement> elements) {
		if(elements != null && !elements.isEmpty()) {
			StringBuffer name = new StringBuffer();
			for(BioPAXElement e : elements) {
				name.append(getNodeName(e)).append(' ');
			}
			return name.toString();
		}
		return null;
	}


	/**
	 * Checks whether the element is of 
	 * any of the listed BioPAX types.
	 * 
	 * @param e
	 * @param classes
	 * @return
	 */
	public static boolean isOneOfBiopaxClasses(BioPAXElement e, Class<? extends BioPAXElement>... classes) {
		for(Class<? extends BioPAXElement> c : classes) {
			if(c.isInstance(e)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Gets abbreviated cellular location term.
	 * 
	 * @param value
	 * @return
	 */
	public static String getAbbrCellLocation(String value) {
		for(String abr: cellLocationMap.keySet()) {
			if(value.toLowerCase().contains(abr)) {
				return cellLocationMap.get(abr);
			}
		}
		return value;
	}
	
	/**
	 * Gets abbreviated chemical modification term.
	 * 
	 * @param value
	 * @return
	 */
	public static String getAbbrChemModification(String value) {
		for(String abr: chemModificationsMap.keySet()) {
			if(value.toLowerCase().contains(abr)) {
				return chemModificationsMap.get(abr);
			}
		}
		return value;
	}
	
	
	public static Map<String, String> getChemModificationsMap() {
		return chemModificationsMap;
	}
	
	public static Map<String, String> getCellLocationMap() {
		return cellLocationMap;
	}
	
	
	public static String truncateLongStr(String str) {
		if(str != null) {
			str = str.replaceAll("[\n\r \t]+", " ");
			if (str.length() > MAX_DISPLAY_STRING_LEN) {
				str = str.substring(0, MAX_DISPLAY_STRING_LEN) + "...";
			}
		}
		return str;
	}

	public static Map<Long, Model> getNetworkModelMap() {
		return networkModelMap;
	}

	public static void removeNetworkModel(long cyNetworkId) {
	    networkModelMap.remove(cyNetworkId);
	    if(log.isDebugEnabled())
	    	log.debug("in-memory biopax networks: " 
	    			+ networkModelMap.toString());
	}

	public static boolean addNetworkModel(long cyNetworkId, Model bpModel) {
		networkModelMap.put(cyNetworkId, bpModel);
		return true;
	}

	public static Model getNetworkModel(long cyNetworkId) {
		Model bpModel = networkModelMap.get(cyNetworkId);
		return bpModel;
	}
	
	public static boolean isBioPAXNetwork(CyNetwork cyNetwork) {
		// BioPAX network (having interaction nodes)
		CyRow row = cyNetwork.getCyRow();
		Boolean b1 = row.get(MapBioPaxToCytoscapeImpl.BIOPAX_NETWORK, Boolean.class);
		// BioPAX network that was converted to SIF (TODO mapping to the simplified SIF network currently is not done)
        // Bgg fix: disable exporting SIF networks (read from PC web service) for now
		Boolean b2 = false; //networkAttributes.getBooleanAttribute(networkID, MapBioPaxToCytoscape.BINARY_NETWORK);

        return (b1 != null && b1) || (b2 != null && b2);
	}
	
}