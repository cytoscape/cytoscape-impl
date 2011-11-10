package org.cytoscape.webservice.ncbi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.webservice.ncbi.rest.EntrezRestClient;
import org.cytoscape.webservice.ncbi.ui.AnnotationCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportTableTask implements Callable<Double> {

	private static final Logger logger = LoggerFactory.getLogger(ImportTableTask.class);

	// Pre-defined keys in the Gene XML file
	private static final String GENE_ID_TAG = "Gene-track_geneid";
	private String ENTRY_KEY = "Entrezgene";
	private static final String TAX_KEY = "Org-ref_taxname";
	private static final String LOCUS_NAME_KEY = "Gene-ref_locus";
	private static final String LOCUS_TAG_KEY = "Gene-ref_locus-tag";
	private static final String ID_IN_PRIMARY_SOURCE_KEY = "Object-id_str";
	
	private static final String SUMMARY_TAG = "Entrezgene_summary";
	
	private static final String PROTEIN_INFO_NAME_TAG = "Prot-ref_desc";

	private enum GeneTags {
		GENE_ID(GENE_ID_TAG, "Entrez Gene ID"), TAX(TAX_KEY, "Taxonomy"), SYMBOL(LOCUS_NAME_KEY, "Official Symbol"), LOCUS_TAG(LOCUS_TAG_KEY, "Locus Name"), SOURCE_ID(
				ID_IN_PRIMARY_SOURCE_KEY, "Source ID"), PROT_NAME(PROTEIN_INFO_NAME_TAG, "Preferred Name");

		private static final Map<String, String> tag2NameMap;
		static {
			tag2NameMap = new HashMap<String, String>();
			for(GeneTags tag: GeneTags.values())
				tag2NameMap.put(tag.getTag(), tag.getAttrName());
		}
		private final String tag;
		private final String attrName;

		private GeneTags(final String tag, final String attrName) {
			this.tag = tag;
			this.attrName = attrName;
		}
		
		public String getTag() {
			return tag;
		}
		
		public String getAttrName() {
			return this.attrName;
		}
		
		public static String getAttrNameFromTag(String tag) {
			return tag2NameMap.get(tag);
		}
	}

	private final String[] ids;
	private final CyTable table;

	private Map<String, String> valueMap;
	private Set<String> pubmedIDs;
	private Node pathwayNode;

	final Set<AnnotationCategory> category;

	private Set<String> pathways;

	private final double portion;

	public ImportTableTask(final String[] ids, final Set<AnnotationCategory> category, final CyTable table, double totalSize) {
		this.ids = ids;
		this.table = table;
		this.category = category;
		
		portion = (double)ids.length/(double)totalSize;
	}

	@Override
	public Double call() throws Exception {

		final URL url = createURL();

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = url.openStream();

		final Document result = builder.parse(is);
		final NodeList entries = result.getElementsByTagName(ENTRY_KEY);
		logger.debug("######## 1 Got Result for = " + url.toString());
		logger.debug("######## 2 Got Entries: " + entries.getLength());

		final int dataSize = entries.getLength();
		for (int i = 0; i < dataSize; i++) {
			Node item = entries.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			logger.debug(i + ": Item = " + item.getNodeName());
			processEntry(item);
		}

		is.close();
		is = null;

		return portion;
	}

	private void processEntry(Node entry) {
		// Create columns
		for(GeneTags geneTag: GeneTags.values()) {
			if(table.getColumn(geneTag.getAttrName()) == null)
				table.createColumn(geneTag.getAttrName(), String.class, false);
		}
		// Summary
		if(table.getColumn("Summary") == null)
			table.createColumn("Summary", String.class, false);
		
		valueMap = new HashMap<String, String>();
		pubmedIDs = new HashSet<String>();
		pathwayNode = null;

		// Get primary key (Entrez gene id)
		walk(entry, GENE_ID_TAG);

		// Check ID. If this does not exists, it's an invalid entry!
		final String geneID = valueMap.get(GENE_ID_TAG);
		logger.debug("Gene ID = " + geneID);
		
		if (geneID == null)
			return;

		// Create row
		final CyRow row = table.getRow(geneID);
		row.set(CyTableEntry.NAME, geneID);

		// First, extract general information.  This will be imported always.
		
		// Taxonomy name
		walk(entry, GeneTags.TAX.getTag());
		// Official Symbol
		walk(entry, GeneTags.SYMBOL.getTag());
		// Locas tag
		walk(entry, GeneTags.LOCUS_TAG.getTag());
		// ID in Source Database
		walk(entry, GeneTags.SOURCE_ID.getTag());

		// Process Summary
		if (category.contains(AnnotationCategory.SUMMARY)) {
			logger.debug("Searching summary");
			for (Node child = entry.getFirstChild(); child != null; child = child.getNextSibling()) {
				
				if (child.getNodeName().equals(SUMMARY_TAG)) {
					logger.debug("Summary Found = " + child.getTextContent());
					row.set("Summary", child.getTextContent());
				}
			}
		}
		
		if (category.contains(AnnotationCategory.GENERAL)) {
			walk(entry, PROTEIN_INFO_NAME_TAG);
		}

		if (category.contains(AnnotationCategory.PATHWAY)) {
			pathways = new HashSet<String>();
			pathwayNode = null;
			processPathways(entry, row, "Pathways");
		}
		
		if (category.contains(AnnotationCategory.PHENOTYPE)) {
			pathways = new HashSet<String>();
			pathwayNode = null;
			processPathways(entry, row, "Phenotypes");
		}
		
		if (category.contains(AnnotationCategory.LINK)) {
			pathways = new HashSet<String>();
			pathwayNode = null;
			processPathways(entry, row, "Additional Links");
		}
		
		if (category.contains(AnnotationCategory.PUBLICATION)) {
			pubmedIDs = new HashSet<String>();
			processPublications(entry, row);
		}
		
		for (String key : valueMap.keySet()) {
			row.set(GeneTags.getAttrNameFromTag(key), valueMap.get(key));
			logger.debug(GeneTags.getAttrNameFromTag(key) + " = " + valueMap.get(key));
		}
	}

	
	private void processPathways(Node entry, CyRow row, String tagName) {
		logger.debug("Searching " + tagName);
		if(table.getColumn(tagName) == null)
			table.createListColumn(tagName, String.class, false);
		
		findPathwaySection(entry, tagName);
		if(this.pathwayNode == null)
			return;
		logger.debug(tagName + "Node found.");
		Node pathwayTarget = null;
		for (Node child = pathwayNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("Gene-commentary_comment")) {
					pathwayTarget = child;
					break;
				}
			}
		}
		walkPathways(pathwayTarget, tagName);
		if(this.pathways.size() != 0) {
			row.set(tagName, new ArrayList<String>(pathways));
			logger.debug(tagName + " Found Size = " + pathways.size());
		}
	}
	
	private void walkPathways(Node node, String tag) {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("Gene-commentary_text")) {
					logger.debug(tag + " Found =>>> " + child.getTextContent());
					pathways.add(child.getTextContent());
					break;
				} else
					walkPathways(child, tag);
			}
		}
	}
	
	private void findPathwaySection(Node entry, String tag) {
		for (Node child = entry.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getTextContent().equals(tag)) {
					pathwayNode = child.getParentNode();
					break;
				} else
					findPathwaySection(child, tag);
			}
		}
	}
	
	private void processPublications(Node entry, CyRow row) {
		logger.debug("Searching publications");
		if(table.getColumn("PubMed ID") == null)
			table.createListColumn("PubMed ID", String.class, false);
		
		for (Node child = entry.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			if (child.getNodeName().equals("Entrezgene_comments")) {
				
				walkPubID(child);
				if(this.pubmedIDs.size() != 0) {
					row.set("PubMed ID", new ArrayList<String>(pubmedIDs));
					logger.debug("Total Found = " + pubmedIDs.size());
				}
			}
		}
	}

	private String walk(Node node, final String targetTag) {
		String result = null;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(targetTag)) {
					result = child.getTextContent();
					logger.debug("Found: " + result);
					valueMap.put(targetTag, result);
					break;
				} else
					walk(child, targetTag);
			}
		}
		return result;
	}
	
	private void walkPubID(Node node) {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("PubMedId")) {
					pubmedIDs.add(child.getTextContent());
					break;
				} else
					walkPubID(child);
			}
		}
	}

	private URL createURL() throws IOException {

		final StringBuilder builder = new StringBuilder();

		for (final String id : ids) {
			if (id != null)
				builder.append(id + ",");
		}

		String urlString = builder.toString();
		urlString = urlString.substring(0, urlString.length() - 1);
		final URL url = new URL(EntrezRestClient.FETCH_URL + urlString);
		logger.debug("Table Import Query URL = " + url.toString());
		return url;
	}
}
