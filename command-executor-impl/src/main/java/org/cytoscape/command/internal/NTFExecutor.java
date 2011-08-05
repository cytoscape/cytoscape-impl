

package org.cytoscape.command.internal;

import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TunableInterceptor;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;

class NTFExecutor extends TFExecutor {
	private final NetworkTaskFactory ntf;
	private final CyApplicationManager appMgr;

	public NTFExecutor(NetworkTaskFactory ntf, CommandTunableInterceptorImpl interceptor, 
	                   CyApplicationManager appMgr) {
		super(ntf,interceptor);
		this.ntf = ntf;
		this.appMgr = appMgr;
	}

	public void execute(String args) {
		ntf.setNetwork( appMgr.getCurrentNetwork() );
		super.execute(args);
	}
}
