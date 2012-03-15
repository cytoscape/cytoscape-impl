

package org.cytoscape.command.internal;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;

class NVTFExecutor extends TFExecutor {
	private final NetworkViewTaskFactory nvtf;
	private final CyApplicationManager appMgr;

	public NVTFExecutor(NetworkViewTaskFactory nvtf, CommandTunableInterceptorImpl interceptor, 
	                   CyApplicationManager appMgr) {
		super(nvtf,interceptor);
		this.nvtf = nvtf;
		this.appMgr = appMgr;
	}

	public void execute(String args) throws Exception {
		nvtf.setNetworkView( appMgr.getCurrentNetworkView() );
		super.execute(args);
	}
}
