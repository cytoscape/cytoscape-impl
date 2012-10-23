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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * BioPax Utility Class - is a BioPAX Model Adapter 
 * that also defines additional constants. 
 */
public final class BioPaxUtil {
	private static final Map<String,String> cellLocationMap;
	private static final Map<String,String> chemModificationsMap;
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxUtil.class);
    public static final String BIOPAX_DATA = "BIOPAX_DATA";
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

	public static class StaxHack {
		public static final void runWithHack(Runnable runnable) {
			Thread thread = Thread.currentThread();
			ClassLoader loader = thread.getContextClassLoader();
			try {
				thread.setContextClassLoader(WstxInputFactory.class.getClassLoader());
				runnable.run();
			} finally {
				thread.setContextClassLoader(loader);
			}
		}
	}
	
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
		Model model = convertFromOwl(in);
		// immediately convert to BioPAX Level3 model
		if(model != null && BioPAXLevel.L2.equals(model.getLevel())) {
			model = new OneTwoThree().filter(model);
		}
		return model;
	}
	
	private static Model convertFromOwl(final InputStream stream) {
		final Model[] model = new Model[1];
		final SimpleIOHandler handler = new SimpleIOHandler();
		handler.mergeDuplicates(true); // a workaround (illegal) BioPAX data having duplicated rdf:ID...
		StaxHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				try {
					model[0] =  handler.convertFromOWL(stream);	
				} catch (Throwable e) {
					log.warn("Import failed: " + e);
				}
			}
		});
		return model[0];
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
		if (nodeName == null || nodeName.length()== 0) {
			nodeName = getStandardName(bpe);
			if (nodeName == null || nodeName.length() == 0) {
				Collection<String> names = getSynonyms(bpe);
				if (!names.isEmpty())
					nodeName = getTheShortestString(names);
			}
		}

		return (nodeName == null || nodeName.length() == 0)
				? bpe.getRDFId() : StringEscapeUtils.unescapeHtml(nodeName);
	}
	
	
	// get the shortest string
	private static String getTheShortestString(Collection<String> nameList) {
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
	private static String getShortName(BioPAXElement bpe) {
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
	private static String getStandardName(BioPAXElement bpe) {
		if(bpe instanceof Named) {
			return ((Named)bpe).getStandardName();
		} else 
			return null;
	}

	/**
	 * Gets all names, if any.
	 *
	 * @param bpe BioPAX element
	 * @return Collection of names.
	 */
	public static Collection<String> getSynonyms(BioPAXElement bpe) {
		Collection<String> names = new HashSet<String>();
		if(bpe instanceof Named) {
			names = ((Named)bpe).getName();
		}
		return names;
	}
	
	
	/**
	 * Gets the NCBI Taxonomy ID.
	 * @param bpe BioPAX element
	 *
	 * @return taxonomyId, or -1, if not available.
	 */
	public static int getOrganismTaxonomyId(BioPAXElement bpe) {
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
		
		Collection<Pathway> pws = ModelUtils.getRootElements(model, Pathway.class);
		for(Pathway pw: pws) {
				modelName.append(" ").append(getNodeName(pw)); 
		}
		
		if(modelName.length()==0) {
			Collection<Interaction> itrs = ModelUtils.getRootElements(model, Interaction.class);
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
	
	
	public static String truncateLongStr(String str) {
		if(str != null) {
			str = str.replaceAll("[\n\r \t]+", " ");
			if (str.length() > MAX_DISPLAY_STRING_LEN) {
				str = str.substring(0, MAX_DISPLAY_STRING_LEN/2-1) + "..." + str.substring(str.length()-MAX_DISPLAY_STRING_LEN/2);
			}
		}
		return str;
	}

	
	public static String toOwl(final BioPAXElement bpe) {
		final StringWriter writer = new StringWriter();
		final SimpleIOHandler simpleExporter = new SimpleIOHandler(BioPAXLevel.L3);
		StaxHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				try {
					simpleExporter.writeObject(writer, bpe);
				} catch (Exception e) {
					log.error("Failed printing '" + bpe.getRDFId() + "' to OWL", e);
				}
			}
		});
		return writer.toString();
	}
	
}
