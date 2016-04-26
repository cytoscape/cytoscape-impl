package org.cytoscape.command.internal.available;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import java.util.*;

import javax.swing.JPanel;

import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.Tunable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgRecorder extends AbstractTunableInterceptor<ArgHandler> {

	private static final Logger logger = LoggerFactory.getLogger(ArgRecorder.class);

	public List<String> findArgs(Object o) {
		List<String> desc = new ArrayList<>();
		for (final ArgHandler p : getHandlers(o)) {
			if (p instanceof BasicArgHandler &&
			    ((BasicArgHandler)p).getContext() == Tunable.GUI_CONTEXT)
				continue;
			desc.add( p.getDesc() );
		}
		return desc;
	}

	public void addTunableHandlerFactory(ArgHandlerFactory f, Map p) {
		super.addTunableHandlerFactory(f,p);
	}
	public void removeTunableHandlerFactory(ArgHandlerFactory f, Map p) {
		super.removeTunableHandlerFactory(f,p);
	}

}
