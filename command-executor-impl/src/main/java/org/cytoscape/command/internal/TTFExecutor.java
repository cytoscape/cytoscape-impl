

package org.cytoscape.command.internal;

import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;

class TTFExecutor extends TFExecutor {
	private final TableTaskFactory ttf;
	private final CyApplicationManager appMgr;

	public TTFExecutor(TableTaskFactory ttf, CommandTunableInterceptorImpl interceptor, 
	                   CyApplicationManager appMgr) {
		super(ttf,interceptor);
		this.ttf = ttf;
		this.appMgr = appMgr;
	}

	public void execute(String args) throws Exception {
		ttf.setTable( appMgr.getCurrentTable() );
		super.execute(args);
	}
}
