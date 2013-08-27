package org.cytoscape.command.internal.tunables;

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

import org.cytoscape.command.StringTunableHandler;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.work.BasicTunableHandlerFactory;


public class SimpleStringTunableHandlerFactory<T extends StringTunableHandler> 
	extends BasicTunableHandlerFactory<T> implements StringTunableHandlerFactory<T> {

    /**
     * Constructs this BasicStringTunableHandlerFactory.
     * @param specificHandlerType The class of the specific handler to be constructed
     * to handle the matching classes. For instance FloatHandler.class might be specified
     * to handle values with a Float type.
     * @param classesToMatch One or more class types that will be handled by this handler.
     * For example the FloatHandler might handle both Float.class and float.class.
     */
    public SimpleStringTunableHandlerFactory(Class<T> specificHandlerType, Class<?>... classesToMatch ) {
        super(specificHandlerType, classesToMatch);
    }
}
