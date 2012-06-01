package org.cytoscape.command.internal.available;

import java.util.*;

import javax.swing.JPanel;

import org.cytoscape.work.AbstractTunableInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgRecorder extends AbstractTunableInterceptor<ArgHandler> {

	private static final Logger logger = LoggerFactory.getLogger(ArgRecorder.class);

	public List<String> findArgs(Object o) {
		List<String> desc = new ArrayList<String>();
		for (final ArgHandler p : getHandlers(o))
			desc.add( p.getDesc() );
		return desc;
	}

	public void addTunableHandlerFactory(ArgHandlerFactory f, Map p) {
        super.addTunableHandlerFactory(f,p);
    }
    public void removeTunableHandlerFactory(ArgHandlerFactory f, Map p) {
        super.removeTunableHandlerFactory(f,p);
    }

}
