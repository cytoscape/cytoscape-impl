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
package org.cytoscape.biopax.internal.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.cytoscape.biopax.internal.BioPaxMapper;
import org.cytoscape.model.CyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BioPax Utility Class - is a BioPAX Model Adapter 
 * that also defines additional constants. 
 *
 * @author Ethan Cerami, Rex, Arman and Igor Rodchenkov
 * 
 * @CyAPI.Final.Class
 */
public final class BioPaxUtil {
	private static final Map<String,String> cellLocationMap;
	private static final Map<String,String> chemModificationsMap;
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxUtil.class);
    public static final String BIOPAX_DATA = "biopax_data";
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
	public static final String PROTEIN_PHOSPHORYLATED = "Protein-phosphorylated";
	
	// private Constructor
	private BioPaxUtil() {}
	
	static  {
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
		Model model = null;
		try {
			SimpleIOHandler handler = new SimpleIOHandler();
			handler.mergeDuplicates(true); // a workaround (illegal) BioPAX data having duplicated rdf:ID...
			model =  handler.convertFromOWL(in);	
			// immediately convert to BioPAX Level3 model
			if(model != null && BioPAXLevel.L2.equals(model.getLevel())) {
				model = new OneTwoThree().filter(model);
			}
		} catch (Throwable e) {
			log.warn("Import failed: " + e);
		}
		return model;
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

		return bpe.getRDFId();
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
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * 
	 * @return the value or null
	 */
	public static Object getValue(BioPAXElement bpe, String... properties) {
		for (String property : properties) {
			try {
				Method method = bpe.getModelInterface().getMethod(
						"get" + property.substring(0, 1).toUpperCase()
								+ property.substring(1).replace('-', '_'));
				Object invoke = method.invoke(bpe);
				if (invoke != null) {
					return invoke;
				}
//				PropertyEditor editor = SimpleEditorMap.L3
//					.getEditorForProperty(property, bpe.getModelInterface());
//				return editor.getValueFromBean(bpe); // is always a Set!
			} catch (Exception e) {
				if(log.isDebugEnabled()) {
					// this is often OK, as we guess L2 or L3 properties...
					log.debug("Ignore property " + property + " for " 
						+ bpe.getRDFId() + ": " + e);
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Attempts to get the values of specified BioPAX properties.
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * 
	 * @return the set of property values or null
	 */
	public static Collection<?> getValues(BioPAXElement bpe, String... properties) {
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
						+ bpe.getRDFId() + ": " + e);
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
		if(bpe instanceof Named) {
			return ((Named)bpe).getStandardName();
		} else 
			return null;
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
			
		BioPAXElement bs = (BioPAXElement) getValue(bpe, "organism");
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
		
		if(bpe instanceof Entity) {
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
		
		try {
			Object bs = getValue(bpe, "organism");
			if (bs instanceof BioSource) {
				Set<Xref> xrefs = ((BioSource)bs).getXref();
				if(!xrefs.isEmpty()) {
					Xref tx = xrefs.iterator().next();
					taxonomyId = Integer.parseInt(tx.getId());
				}
			}
		} catch (Exception e) {
			taxonomyId = -1;
		}

		return taxonomyId;
	}

	/**
	 * Gets the Comment field.
	 *
	 * @param bpe a BioPAX element
	 * @return comment field
	 */
	public static String getComment(BioPAXElement bpe) {
		return ((Level3Element)bpe).getComment().toString();
	}

	/**
	 * Gets the Availability Field.
	 *
	 * @param bpe BioPAX element
	 * @return availability field or null, if not available.
	 */
	public static String getAvailability(BioPAXElement bpe) {
		if(bpe instanceof Entity) {
			return ((Entity)bpe).getAvailability().toString();
		} else
			return null;
	}
	
	
	public static <T extends Xref> List<T> getXRefs(BioPAXElement bpe, Class<T> xrefClass) {
		if(bpe instanceof XReferrable) {
			List<T> erefs = new ArrayList<T>();
			erefs.addAll(new ClassFilterSet<Xref,T>( ((XReferrable)bpe).getXref(), xrefClass) );
			if(bpe instanceof SimplePhysicalEntity && 
				((SimplePhysicalEntity)bpe).getEntityReference() != null)
			{
				erefs.addAll(new ClassFilterSet<Xref,T>(
					((SimplePhysicalEntity)bpe).getEntityReference().getXref(), xrefClass) );
			}
			return erefs;
		}
		return new ArrayList<T>();
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
			subclasses.addAll(SimpleEditorMap.L3.getKnownSubClassesOf(c));
		}
		
		return subclasses;
	}


	/**
	 * Creates a name for to the BioPAX model
	 * using its top-level process name(s). 
	 * 
	 * @param model
	 * @return
	 */
	public static String getName(Model model) {		
		StringBuffer modelName = new StringBuffer();
		
		ModelUtils mu = new ModelUtils(model);
		
		Collection<Pathway> pws = mu.getRootElements(Pathway.class);
		for(Pathway pw: pws) {
				modelName.append(" ").append(getNodeName(pw)); 
		}
		
		if(modelName.length()==0) {
			Collection<Interaction> itrs = mu.getRootElements(Interaction.class);
			for(Interaction it: itrs) {
				modelName.append(" ").append(getNodeName(it));
			}	
		}
		
		if(modelName.length()==0) {
			modelName.append(model.getXmlBase());
		}
		
		String name = modelName.toString().trim();

		return name;
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
		return Collections.unmodifiableMap(chemModificationsMap);
	}
	
	public static Map<String, String> getCellLocationMap() {
		return Collections.unmodifiableMap(cellLocationMap);
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

	
	public static boolean isBioPAXNetwork(CyNetwork cyNetwork) {
		return Boolean.TRUE == cyNetwork.getRow(cyNetwork)
			.get(BioPaxMapper.BIOPAX_NETWORK, Boolean.class);
	}
	
	
	public static String toOwl(BioPAXElement bpe) {
		StringWriter writer = new StringWriter();
		try {
			SimpleIOHandler simpleExporter = new SimpleIOHandler(BioPAXLevel.L3);
			simpleExporter.writeObject(writer, bpe);
		} catch (Exception e) {
			log.error("Failed printing '" + bpe.getRDFId() + "' to OWL", e);
		}
		return writer.toString();
	}
	
	
	public static void fixDisplayName(Model model) {
		if (log.isInfoEnabled())
			log.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}
}
