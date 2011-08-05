package org.cytoscape.biopax.internal.action;

import java.io.IOException;
import java.io.OutputStream;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.util.BioPaxUtil;
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
        Model bpModel = BioPaxUtil.getNetworkModel(network.getSUID());
        String bpModelStr = network.getCyRow().get(BioPaxUtil.BIOPAX_MODEL_STRING, String.class);
        try {
            if(bpModel == null || bpModelStr == null )
                throw new IllegalArgumentException("Invalid/empty BioPAX model.");

            SimpleIOHandler simpleExporter = new SimpleIOHandler(bpModel.getLevel());
            simpleExporter.convertToOWL(bpModel, stream);

            stream.close();

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