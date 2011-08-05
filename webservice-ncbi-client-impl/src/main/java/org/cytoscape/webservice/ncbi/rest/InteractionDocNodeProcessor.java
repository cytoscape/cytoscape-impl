package org.cytoscape.webservice.ncbi.rest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InteractionDocNodeProcessor {

	private static final Logger logger = LoggerFactory.getLogger(InteractionDocNodeProcessor.class);

	private static final String GC_TEXT = "Gene-commentary_text";
	private static final String GC_REFS = "Gene-commentary_refs";
	private static final String GC_SOURCE = "Gene-commentary_source";
	private static final String GC_COMMENT = "Gene-commentary_comment";

	private static final String PUB_TAG = "PubMedId";
	private static final String DB_TAG = "Dbtag_db";
	private static final String ID_TAG = "Object-id_id";
	private static final String ANCHOR_TAG = "Other-source_anchor";
	
	static final String targetSource = "DB Name";
	static final String targetSourceID = "Target ID";
	static final String targetOtherName = "Other Name";
	static final String interaction = "Interaction Type";
	
	
	private static final String NCBI_GENE_ID_TAG = "GeneID";
	private static final String DEF_INTERACTION_TYPE = "protein-protein";
	
	private Map<String, String> idMap;
	private Map<String, String> resultMap;

	protected String getTargetID() {
		// Use NCBI ID if available
		String geneID = idMap.get(NCBI_GENE_ID_TAG);
		if(geneID == null)
			geneID = idMap.values().iterator().next();
		return geneID;
	}
	
	protected String getInteractionType() {
		return resultMap.get(interaction);
	}
	
	protected String getTargetAltName() {
		return null;
	}
	
	public void process(final Node interactionNode) {

		idMap = new HashMap<String, String>();
		resultMap = new HashMap<String, String>();
		
		final NodeList children = interactionNode.getChildNodes();
		final int length = children.getLength();

		for (int i = 0; i < length; i++) {
			final Node node = children.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			final String nodeName = node.getNodeName();
			if (nodeName.equals(GC_TEXT)) {
				final String interactionTypeText = node.getTextContent();
				logger.debug("Interaction type = " + interactionTypeText);
				if(interactionTypeText != null && interactionTypeText.trim().length() != 0)
					resultMap.put(interaction, interactionTypeText);
				else
					resultMap.put(interaction, DEF_INTERACTION_TYPE);
			} else if (nodeName.equals(GC_REFS))
				processReference(node);
			else if (nodeName.equals(GC_SOURCE))
				processSource(node);
			else if (nodeName.equals(GC_COMMENT))
				processComment(node);
		}
	}

	
	private void processComment(final Node commentNode) {
		final NodeList comments = commentNode.getChildNodes();
		final int length = comments.getLength();

		for (int i = 0; i < length; i++) {
			final Node comment = comments.item(i);
			for(Node child = comment.getFirstChild(); child != null; child = child.getNextSibling()) {
				if(child.getNodeType() == Node.ELEMENT_NODE) {
					if(child.getNodeName().equals("Gene-commentary_source")) {
						getOtherElements(child);
					}
				}
			}
		}
	}
	
	private void getOtherElements(Node gcSource) {
		for(Node child = gcSource.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				if(child.getNodeName().equals("Other-source")) {
					final Map<String, String> idBlock = new HashMap<String, String>();
					walkNames(child, DB_TAG, idBlock);
					walkNames(child, ID_TAG, idBlock);
					walkNames(child, ANCHOR_TAG, idBlock);
					
					for(String key: idBlock.keySet()) {
						logger.debug(key + " ======= " + idBlock.get(key));
					}
					
					idMap.put(idBlock.get(DB_TAG), idBlock.get(ID_TAG));
				}
			}
		}
	}
	
	private void walkNames(Node node, final String targetTag, Map<String, String> idBlock) {
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				if(child.getNodeName().equals(targetTag)) {
					idBlock.put(targetTag, child.getTextContent());
					break;
				} else
					walkNames(child, targetTag, idBlock);
			}
		}
	}
	

	private void processSource(Node sourceNode) {
		final NodeList publications = sourceNode.getChildNodes();
		final int length = publications.getLength();

		for (int i = 0; i < length; i++) {
			final Node pubNode = publications.item(i);
			walk(pubNode, DB_TAG, targetSource);
			walk(pubNode, ID_TAG, targetSourceID);
		}
		
		logger.debug("DB = " + resultMap.get(targetSource));
		logger.debug("DB ID = " + resultMap.get(targetSourceID));
	}

	
	private void walk(Node node, final String targetTag, final String targetVal) {
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				if(child.getNodeName().equals(targetTag)) {
					//logger.debug(targetTag + " Tag = " + child.getTextContent());
					this.resultMap.put(targetVal, child.getTextContent());
					break;
				} else
					walk(child, targetTag, targetVal);
			}
		}
	}
	
	
	/**
	 * Process reference (publication) section.
	 * 
	 * @param node
	 */
	private void processReference(Node refNode) {
		final NodeList publications = refNode.getChildNodes();
		final int length = publications.getLength();

		for (int i = 0; i < length; i++) {
			final Node pubNode = publications.item(i);
			String test = null;
			walk(pubNode, PUB_TAG, test);
		}
	}

	// private void processInteraction() {
	// interactionType = itr.getGeneCommentaryText();
	//
	// if (interactionType == null) {
	// interactionType = DEF_ITR_TYPE;
	// }
	//
	// if (itr.getGeneCommentaryComment().getGeneCommentary().size() > 1) {
	// // Find node ID. If available, use Entrez Gene
	// // ID.
	// // If not, use the database-specific ID instead.
	// try {
	// nodeid =
	// itr.getGeneCommentaryComment().getGeneCommentary().get(1).getGeneCommentarySource()
	// .getOtherSource().get(0).getOtherSourceSrc().getDbtag().getDbtagTag().getObjectId()
	// .getObjectIdId().toString();
	// } catch (NullPointerException npe) {
	// // This gene is not in NCBI DB.
	// // Use original database ID
	// continue;
	// }
	//
	// // Check source Type
	//
	// nodeType =
	// itr.getGeneCommentaryComment().getGeneCommentary().get(1).getGeneCommentarySource()
	// .getOtherSource().get(0).getOtherSourceSrc().getDbtag().getDbtagDb();
	//
	// // System.out.println("DB Tag for nodeID: "
	// // + nodeid + " = " + nodeType);
	//
	// // In case ID is not GeneID, put tag
	// if (nodeType.equals(GENE_ID_TAG) == false) {
	// nodeid = nodeType + ":" + nodeid;
	// }
	//
	// n1 = Cytoscape.getCyNode(nodeid, true);
	// nodeList.add(n1);
	//
	// // Add node attributes
	// nodeTypes.put(nodeid, nodeType);
	//
	// altName =
	// itr.getGeneCommentaryComment().getGeneCommentary().get(1).getGeneCommentarySource()
	// .getOtherSource().get(0).getOtherSourceAnchor();
	// if (altName != null && altName.length() != 0)
	// nodeAltName.put(nodeid, altName);
	//
	// final String dataSource =
	// itr.getGeneCommentarySource().getOtherSource().get(0).getOtherSourceSrc()
	// .getDbtag().getDbtagDb();
	//
	// List<Edge> eList2 = new ArrayList<Edge>();
	//
	// if (dataSource.equals("BioGRID")) {
	// final String[] expTypes = interactionType.split(";");
	//
	// String etString = null;
	// Edge newEdge = null;
	//
	// for (String eType : expTypes) {
	// etString = eType.trim();
	//
	// eList2.add(newEdge = Cytoscape.getCyEdge(centerNode, n1, "interaction",
	// etString, true));
	//
	// if (dataSource.equals("BioGRID")) {
	// attrMap.put(new String[] { newEdge.getIdentifier(), "interaction type" },
	// BioGRIDUtil.getInteractionType(etString));
	// }
	// }
	// } else {
	// eList2.add(Cytoscape.getCyEdge(centerNode, n1, "interaction",
	// interactionType, true));
	// }
	//
	// // e1 = Cytoscape.getCyEdge(centerNode, n1,
	// // "interaction", interactionType, true);
	// for (Edge e1 : eList2) {
	// edgeList.add(e1);
	// edgeID = e1.getIdentifier();
	//
	// // Add edge attributes
	// attrMap.put(new String[] { edgeID, "datasource" }, dataSource);
	//
	// List<Pub> pubmed = itr.getGeneCommentaryRefs().getPub();
	//
	// if ((pubmed != null) && (pubmed.size() > 0)) {
	// String[] pmid = new String[] { edgeID, "PubMed ID" };
	// List<String> pmids = new ArrayList<String>();
	//
	// for (Pub pub : pubmed) {
	// pmids.add(pub.getPubPmid().getPubMedId().toString());
	// }
	//
	// attrMap.put(pmid, pmids);
	// } // /
	// }
	// }
	// }

}
