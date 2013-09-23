package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writer for all JSON format.
 * Output format will be determined by ObjectMapper.
 *
 */
public class JSONNetworkWriter extends AbstractNetworkTask implements CyWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(JSONNetworkWriter.class);
	private static final String ENCODING = "UTF-8";
	
	protected final OutputStream outputStream;
	protected final ObjectMapper network2jsonMapper;
	protected final CharsetEncoder encoder;

	public JSONNetworkWriter(final OutputStream outputStream, final CyNetwork network, final ObjectMapper network2jsonMapper) {
		super(network);
		
		this.outputStream = outputStream;
		this.network2jsonMapper = network2jsonMapper;

		if(Charset.isSupported(ENCODING)) {
			// UTF-8 is supported by system
			this.encoder = Charset.forName(ENCODING).newEncoder();
		} else {
			// Use default.
			logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-Roman annotations.");
			this.encoder = Charset.defaultCharset().newEncoder();
		}
	}
	

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(taskMonitor != null) {
			taskMonitor.setTitle("Writing to JSON...");
			taskMonitor.setProgress(0);
		}
		network2jsonMapper.writeValue(outputStream, network);
		outputStream.close();
	}

}
