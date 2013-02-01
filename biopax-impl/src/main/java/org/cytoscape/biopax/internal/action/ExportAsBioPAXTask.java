package org.cytoscape.biopax.internal.action;

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

class ExportAsBioPAXTask implements CyWriter {
	private final OutputStream stream;
	private final CyNetwork network;
	private final String fileName;

	ExportAsBioPAXTask(String fileName, OutputStream stream, CyNetwork network) {
		this.fileName = fileName;
		this.stream = stream;
		this.network = network;
	}

	@Override
	public void cancel() {
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Exporting BioPAX...");
        //String bpModelStr = network.getCyRow(network).get(BioPaxUtil.BIOPAX_DATA, String.class);
        String bpModelStr = network.getRow(network,CyNetwork.HIDDEN_ATTRS).get(BioPaxUtil.BIOPAX_DATA, String.class);
        try {
            Writer w = new OutputStreamWriter(stream);
            w.write(bpModelStr);
            w.close();

            // TODO: Port this?
//			Object[] ret_val = new Object[3];
//			ret_val[0] = network;
//			ret_val[1] = new File(fileName).toURI();
//			ret_val[2] = Cytoscape.FILE_BIOPAX;
//			Cytoscape.firePropertyChange(Cytoscape.NETWORK_SAVED, null, ret_val);

			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Network successfully saved to:  " + fileName + ".");
		} catch (IllegalArgumentException e) {
			throw new Exception("Network is invalid. Cannot be saved.", e);
        } catch (IOException e) {
        	throw new Exception("Unable to save network.", e);
        }
    }
}
