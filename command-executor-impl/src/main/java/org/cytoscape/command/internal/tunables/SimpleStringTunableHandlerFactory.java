package org.cytoscape.command.internal.tunables;

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
