package org.cytoscape.io.internal.write.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SifWriter implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(SifWriter.class);

	private static final String DEFAULT_INTERACTION = "-";
	private static final String ENCODING = "UTF-8";

	private final OutputStream outputStream;
	private final CyNetwork network;

	private final CharsetEncoder encoder;

	public SifWriter(final OutputStream outputStream, final CyNetwork network) {
		this.outputStream = outputStream;
		this.network = network;

		if(Charset.isSupported(ENCODING)) {
			// UTF-8 is supported by system
			this.encoder = Charset.forName(ENCODING).newEncoder();
		} else {
			// Use default.
			logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
			this.encoder = Charset.defaultCharset().newEncoder();
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (taskMonitor != null) {
			taskMonitor.setProgress(0.0);
			taskMonitor.setTitle("Exporting to SIF");
			taskMonitor.setStatusMessage("Exporting current network as SIF...");
		}

		System.out.println("Encoding = " + encoder.charset());
		final OutputStreamWriter writer = new OutputStreamWriter(outputStream, encoder);

		final String lineSep = System.getProperty("line.separator");
		final List<CyNode> nodeList = network.getNodeList();

		int i = 0;
		for (CyNode node : nodeList) {
			if (taskMonitor != null) {
				final double percent = ((double) i++ / nodeList.size());
				taskMonitor.setProgress(percent);
			}
			
			final String sourceName = network.getRow(node).get(CyNetwork.NAME, String.class);
			final List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
			if(sourceName == null || sourceName.length() == 0)
				throw new IllegalStateException("This network contains null or empty node name.");
			

			if (edges.size() == 0) {
				writer.write(sourceName + lineSep);
			} else {
				for (final CyEdge edge : edges) {

					if (node == edge.getSource()) { 
						// Do only for outgoing edges
						final CyNode target = edge.getTarget();
						final String targetName = network.getRow(target).get(CyNetwork.NAME, String.class);
						if(targetName == null || targetName.length() == 0)
							throw new IllegalStateException("This network contains null or empty node name.");
						
						String interactionName = network.getRow(edge).get(CyEdge.INTERACTION, String.class);
						if (interactionName == null)
							interactionName = DEFAULT_INTERACTION;

						writer.write(sourceName);
						writer.write("\t");
						writer.write(interactionName);
						writer.write("\t");
						writer.write(targetName);
						writer.write(lineSep);
					}
				}
			}
		}

		writer.close();
		outputStream.close();
	}

	@Override
	public void cancel() {
		if (outputStream == null)
			return;

		try {
			outputStream.close();
		} catch (IOException e) {
			logger.error("Could not close Outputstream for SifWriter.", e);
		}
	}
}
