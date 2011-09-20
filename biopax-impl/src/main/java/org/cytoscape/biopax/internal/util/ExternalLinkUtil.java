// $Id: ExternalLinkUtil.java,v 1.10 2006/06/15 22:06:02 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.util;

import java.util.*;

import org.cytoscape.biopax.internal.ExternalLink;



/**
 * Utility Class for Creating Links to External Databases.
 *
 * @author Ethan Cerami
 * 
 * TODO replace with Miriam (MiriamLink) or identifiers.org API
 */
public class ExternalLinkUtil {
	private static Map dbMap;
	private static Map ihopMap;
	private static final String SPACE = "%20";
	private static final String PIPE_CHAR = "%7C";
	private static final String AMPERSAND = "&";
	private static final String COMMA = ",";
	private static final String UNIPROT_AC = "UNIPROT__AC";
	private static String pipeChar = PIPE_CHAR;

	/**
	 * Gets a URL to the specified dbName/id Pair.
	 *
	 * @param dbName External Database.
	 * @param id     External ID.
	 * @return a URL String, or null, if dbName is not found.
	 */
	public static String getUrl(String dbName, String id) {
		dbName = dbName.toUpperCase();

		String url = (String) dbMap.get(dbName);

		if (url != null) {
			return url + id;
		} else {
			return null;
		}
	}

	/**
	 * Enables URL Encoding.
	 * URL Encoding is on by default.
	 * The only reason to turn this off is to easily view URLs within a unit
	 * test.  Note that URL Encoding must be done in order to properly
	 * launch an External Web Browser.
	 *
	 * @param flag boolean flag.
	 */
	public static void useUrlEncoding(boolean flag) {
		if (flag) {
			pipeChar = "%7C";
		} else {
			pipeChar = "|";
		}
	}

	/**
	 * Creates an HTML Link to the specified Database.
	 *
	 * @param dbName External Database.
	 * @param id     External ID.
	 * @return HTML String.
	 */
	public static String createLink(String dbName, String id) {
        dbName = dbName.toUpperCase();
		String url = getUrl(dbName, id);
		StringBuffer buf = new StringBuffer();

        if (url != null) {
			buf.append("<A class=\"link\" HREF=\"" + url + "\">" + dbName + ":  " + id + "</A>");
		} else {
			buf.append(dbName + ":  " + id);
		}

		return buf.toString();
	}

	/**
	 * Gets a Link for Searching IHOP.
	 * <p/>
	 * The following rules apply for creating links to IHOP:
	 * <UL>
	 * <LI>Only create links for elements of type:  protein, DNA and RNA.
	 * IHOP does not capture information about other BioPAX types.
	 * <LI>If synonyms exist, use them.
	 * <LI>If XRefs for UniProt, Entrez Gene or RefSeq exist, use them.
	 * <LI>If we have at least one synonym or xref, append a taxonomy ID.
	 * <LI>In the special case where we have just one UNIPROT ID, and
	 * no synonyms, we don't have enough information in IHOP to create a
	 * meaningful search result page.  In this case, no link is created.
	 * </UL>
	 * For details on how to construct IHOP links, see:
	 * http://www.pdg.cnb.uam.es/UniPub/iHOP/info/dev/in.html
	 *
	 * @param type       BioPAX Type, e.g. protein
	 * @param synList    ArrayList of Synonym Strings.
	 * @param dbList     ArrayList of ExternalLink Objects.
	 * @param taxonomyId NCBI TaxonomyID or -1 if unknown.
	 * @return URL String, or null if a URL cannot be constructed.
	 */
	public static String getIHOPUrl(String type, List synList, List dbList, int taxonomyId) 
	{
		if (type.equalsIgnoreCase("protein") 
			|| type.equalsIgnoreCase("dna")
		    	|| type.equalsIgnoreCase("rna")) 
		{
			StringBuffer url = new StringBuffer();

			// Use the URL Below for local testing within cbio
			// StringBuffer url = new StringBuffer
			//        ("http://cbio.mskcc.org/UniPub/iHOP/in?");
			String synonymParameter = createSynonymParameter(synList);
			String dbParameter = createDbParameter(dbList, synList);

			url.append(synonymParameter);
            if (dbParameter != null && dbParameter.length() > 0) {
                appendAmpersand(synonymParameter, url);
			    url.append(dbParameter);
            }

            //  Taxonomy ID appears like this:
            //  ncbi_tax_id_1=9609
            if (url.length() > 0) {
            	//  removed NCBI Taxonomy ID;  results in nearly always getting a hit w/i iHOP.
            	//	url.append("ncbi_tax_id_1=" + taxonomyId);
            	url.insert(0, "http://www.ihop-net.org/UniPub/iHOP/in?");

            	// return string - but encode spaces first
            	return url.toString().replaceAll("\\s", SPACE);
            }
		} 
		
		return null;
	}

	private static void appendAmpersand(String param, StringBuffer url) {
		if (param.length() > 0) {
			url.append(AMPERSAND);
		}
	}

	/**
	 * DBRefs appear like this:
	 * dbrefs_1=UNIPROT__AC|P0214,NCBI_GENE__ID=327..
	 */
	private static String createDbParameter(List dbList, List synList) {
		int dbHits = 0;
		int uniProtHits = 0;
		StringBuffer temp = new StringBuffer();

		if ((dbList != null) && (dbList.size() > 0)) {
			for (int i = 0; i < dbList.size(); i++) {
				ExternalLink link = (ExternalLink) dbList.get(i);
				if(link != null && link.getDbName() != null) {
					String code = (String) ihopMap.get(link.getDbName().toUpperCase());
					if (code != null) {
						if (code.equals(UNIPROT_AC)) {
							uniProtHits++;
						}

						dbHits++;
						temp.append(code + pipeChar + link.getId() + COMMA);
					}
				}
			}

			if (temp.length() > 0) {
				//  This is a special case.
				if ((uniProtHits == dbHits) && (synList.size() == 0)) {
					return new String();
				} else {
					//  Insert parameter name; remove last comma
					temp.insert(0, "dbrefs_1=");

					return temp.substring(0, temp.length() - 1);
				}
			}
		}

		return new String();
	}

	/**
	 * Synonyms appear like this:
	 * syns_1=SYN1|SYN2|SYN3...
	 */
	private static String createSynonymParameter(List synList) {
		StringBuffer temp = new StringBuffer();

		if ((synList != null) && (synList.size() > 0)) {
			temp.append("syns_1=");

			for (int i = 0; i < synList.size(); i++) {
				temp.append((String) synList.get(i));

				if (i < (synList.size() - 1)) {
					temp.append(pipeChar);
				}
			}
		}

		return temp.toString();
	}

	/**
	 * Creates HTML for a Link to IHOP.
	 *
	 * @param type       BioPAX Type, e.g. protein
	 * @param synList    ArrayList of Synonym Strings.
	 * @param linkList   ArrayList of ExternalLink Objects.
	 * @param taxonomyId NCBI TaxonomyID or -1 if unknown.
	 * @return HTML Link.
	 */
	public static String createIHOPLink(String type, 
			List synList, List linkList, int taxonomyId) 
	{
		String url = getIHOPUrl(type, synList, linkList, taxonomyId);

        if (url != null) {
			StringBuffer buf = new StringBuffer();
			buf.append("<A class=\"link\" HREF=\"" + url + "\">" + "Search iHOP</A>");
			return buf.toString();
		} else {
			return null;
		}
	}

	static {
		dbMap = new HashMap();
		ihopMap = new HashMap();

		//  Pub Med
		String url = "http://www.ncbi.nlm.nih.gov/entrez/"
		             + "query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract" + "&list_uids=";
		dbMap.put("PUBMED", url);
		dbMap.put("PMID", url);

        //  HPRD
        url = "http://hprd.org/protein/";
        dbMap.put ("HPRD", url);

        //  UniProt
		url = "http://www.pir.uniprot.org/cgi-bin/upEntry?id=";

		HashMap temp = new HashMap();
		temp.put("UNIPROT", url);
		temp.put("SWISSPROT", url);
		temp.put("SWP", url);
		temp.put("SWISS-PROT", url);
		dbMap.putAll(temp);
		addIHOPEntries(temp, UNIPROT_AC);

		// Gene Ontology
		url = "http://www.godatabase.org/cgi-bin/amigo/go.cgi?open_1=";
		dbMap.put("GO", url);

		//  Reactome
		url = "http://reactome.org/cgi-bin/eventbrowser?DB=gk_current&ID=";
		dbMap.put("REACTOME", url);

		//  PDB
		url = "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=";
		dbMap.put("PDB", url);

		//  Ref Seq
		url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&" + "cmd=search&term=";
		temp = new HashMap();
		temp.put("REFSEQ", url);
		temp.put("REF-SEQ", url);
		temp.put("REF_SEQ", url);
		dbMap.putAll(temp);
		addIHOPEntries(temp, "NCBI_REFSEQ__NP");

		// OMIM
		url = "http://www.ncbi.nlm.nih.gov/entrez/dispomim.cgi?id=";
		dbMap.put("OMIM", url);

		//  Entrez Gene
		url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&" + "cmd=search&term=";
		temp = new HashMap();
        temp.put("ENTREZGENE", url);
        temp.put("ENTREZ_GENE", url);
		temp.put("LOCUS_LINK", url);
		temp.put("LOCUSLINK", url);
		temp.put("LOCUS-LINK", url);
		dbMap.putAll(temp);
		addIHOPEntries(temp, "NCBI_GENE__ID");

		//  Unigene
		url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?" + "db=unigene&cmd=search&term=";
		dbMap.put("UNIGENE", url);

		//  NCBI GenBank
		url = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein" + "&val=";
		dbMap.put("GENBANK", url);
		dbMap.put("ENTREZ_GI", url);
		dbMap.put("GI", url);
	}

	private static void addIHOPEntries(HashMap map, String iHopCode) {
		Iterator iterator = map.keySet().iterator();

		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			ihopMap.put(key, iHopCode);
		}
	}
}
