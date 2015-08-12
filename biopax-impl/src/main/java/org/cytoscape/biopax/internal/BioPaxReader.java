package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.SwingUtilities;

import org.cytoscape.biopax.internal.util.BioPaxReaderError;
import org.cytoscape.biopax.internal.util.VisualStyleUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioPaxReader extends AbstractInputStreamTaskFactory implements NetworkViewAddedListener {

	private final CyServices cyServices;
	private final VisualStyleUtil visualStyleUtil;
	
	private static final Logger LOG = LoggerFactory.getLogger(BioPaxReader.class);

	public BioPaxReader(CyFileFilter filter, CyServices cyServices, VisualStyleUtil visualStyleUtil)
	{
		super(filter);
		this.cyServices = cyServices;
		this.visualStyleUtil = visualStyleUtil;
	}
	

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {		
		LOG.info("createTaskIterator: input stream name: " + inputName);
		try {
			return new TaskIterator(
				new BioPaxReaderTask(copy(is), inputName, cyServices, visualStyleUtil)
			);
		} catch (IOException e) {
			throw new BioPaxReaderError(e.toString());
		}
	}


	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		// always apply the style and layout to new BioPAX views;
		// i.e., not only for the first time when one's created.
		final CyNetworkView view = e.getNetworkView();
		final CyNetwork cyNetwork = view.getModel();	
		if(isBioPaxNetwork(cyNetwork)) {	
			VisualStyle style = null;		
			String kind = cyNetwork.getRow(cyNetwork).get(BioPaxMapper.BIOPAX_NETWORK, String.class);
			if ("DEFAULT".equals(kind))
				style = visualStyleUtil.getBioPaxVisualStyle();
			else if ("SIF".equals(kind))
				style = visualStyleUtil.getBinarySifVisualStyle();

			//apply style and layout			
			if(style != null) {
				final VisualStyle vs = style;			
				//apply style and layout			
				SwingUtilities.invokeLater(new Runnable() {
						public void run() {			
							layout(view);
							cyServices.mappingManager.setVisualStyle(vs, view);
							vs.apply(view);		
							view.updateView();
						}
				});
			}
		}
	}
	
	private void layout(CyNetworkView view) {
		// do layout
		CyLayoutAlgorithm layout = cyServices.layoutManager.getLayout("force-directed");
		if (layout == null) {
			layout = cyServices.layoutManager.getDefaultLayout();
			LOG.warn("'force-directed' layout not found; will use the default one.");
		}
		cyServices.taskManager.execute(layout.createTaskIterator(view, 
				layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
	}	
	
	private boolean isBioPaxNetwork(CyNetwork cyNetwork) {
		//true if the attribute column exists
		CyTable cyTable = cyNetwork.getDefaultNetworkTable();
		return cyTable.getColumn(BioPaxMapper.BIOPAX_NETWORK) != null;
	}
	
	
	private InputStream copy(InputStream is) throws IOException {
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
		int chunk = 0;
		byte[] data = new byte[1024*1024];
		while((-1 != (chunk = is.read(data)))) {
			copy.write(data, 0, chunk);
		}
		is.close();
		return new ByteArrayInputStream( copy.toByteArray() );
	}	

}
