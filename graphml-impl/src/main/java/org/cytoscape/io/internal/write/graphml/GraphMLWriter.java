package org.cytoscape.io.internal.write.graphml;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GraphMLWriter implements CyWriter {

	private static final Logger logger = LoggerFactory
			.getLogger(GraphMLWriter.class);

	private static final String GRAPHML = "graphml";
	private static final String GRAPH = "graph";
	private static final String ID = "id";
	private static final String NODE = "node";
	private static final String EDGE = "edge";

	private static final String SOURCE = "source";
	private static final String TARGET = "target";

	private static final String directed = "edgedefault";

	private final CyNetwork network;
	private final Writer writer;
	private final OutputStream outputStream;

	public GraphMLWriter(final OutputStream outputStream,
			final CyNetwork network) {
		this.network = network;
		this.outputStream = outputStream;
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	private final void write() throws IOException,
			ParserConfigurationException, TransformerException {
		final Document graphMLDoc = createDocument();

		TransformerFactory transFactory = TransformerFactory.newInstance();
		transFactory.setAttribute("indent-number", 4);
		Transformer transformer = transFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");

		DOMSource source = new DOMSource(graphMLDoc);

		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}

	private final Document createDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.newDocument();
		Element root = document.createElement(GRAPHML);

		document.appendChild(root);
		Element graphElm = document.createElement(GRAPH);

		// For now, everything is directed.
		graphElm.setAttribute(directed, "directed");
		graphElm.setAttribute(ID,network.getRow(network).get(CyNetwork.NAME, String.class));

		// Add Column data types
		writeAttributes(network.getDefaultNodeTable(), document, root);
		writeAttributes(network.getDefaultEdgeTable(), document, root);
		writeAttributes(network.getDefaultNetworkTable(), document, root);
		
		// Add Graph element
		root.appendChild(graphElm);

		writeNodes(document, graphElm);
		writeEdges(document, graphElm);

		return document;
	}

	/**
	 * Write Column data types.
	 * 
	 * @param attr
	 * @param doc
	 * @param parent
	 */
	private void writeAttributes(CyTable attr, Document doc, Element parent) {
		for (final CyColumn column : attr.getColumns()) {
			final Class<?> type = column.getType();
			String tag = GraphMLAttributeDataTypes.getTag(type);

			if (tag == null) {
				tag = GraphMLAttributeDataTypes.STRING.getTypeTag();
			}

			final String attrName = column.getName();
			Element keyElm = doc.createElement("key");
			keyElm.setAttribute("for", NODE);
			keyElm.setAttribute("attr.name", attrName);
			keyElm.setAttribute("attr.type", tag);
			keyElm.setAttribute(ID, attrName);
			parent.appendChild(keyElm);
		}
	}

	private void writeNodes(Document doc, Element parent) {
		final List<CyNode> nodes = network.getNodeList();
		final CyTable table = network.getDefaultNodeTable();
		final Collection<CyColumn> nodeColumns = table.getColumns();

		for (final CyNode node : nodes) {
			final Element nodeElm = doc.createElement(NODE);
			nodeElm.setAttribute(ID, node.getSUID().toString());
			final CyRow row = network.getRow(node);
			appendData(row, nodeColumns, doc, nodeElm, node);
			parent.appendChild(nodeElm);
		}
	}

	private void writeEdges(Document doc, Element parent) {
		final List<CyEdge> edges = network.getEdgeList();
		final CyTable table = network.getDefaultEdgeTable();
		final Collection<CyColumn> edgeColumns = table.getColumns();

		for (final CyEdge edge : edges) {
			final Element edgeElm = doc.createElement(EDGE);
			edgeElm.setAttribute(SOURCE, edge.getSource().getSUID().toString());
			edgeElm.setAttribute(TARGET, edge.getTarget().getSUID().toString());
			final CyRow row = network.getRow(edge);
			appendData(row, edgeColumns, doc, edgeElm, edge);
			parent.appendChild(edgeElm);
		}
	}

	private void appendData(final CyRow row,
			final Collection<CyColumn> columns, Document doc, Element parent, CyIdentifiable obj) {

		for (CyColumn column : columns) {
			Object val = row.get(column.getName(), column.getType());
			if (val != null) {
				Element dataElm = doc.createElement("data");
				dataElm.setAttribute("key", column.getName());
				dataElm.setTextContent(val.toString());
				parent.appendChild(dataElm);
			}
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		write();
	}

	@Override
	public void cancel() {
		if (outputStream == null) {
			return;
		}

		try {
			outputStream.close();
		} catch (IOException e) {
			logger.error("Could not close Outputstream for GraphMLWriter.", e);
		}
	}

}