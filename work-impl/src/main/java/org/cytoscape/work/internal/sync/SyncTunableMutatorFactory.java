package org.cytoscape.work.internal.sync;

import java.util.Properties;

import org.cytoscape.work.TunableHandlerFactory;

public class SyncTunableMutatorFactory {
	private TunableHandlerFactory<SyncTunableHandler> handlerFactory;

	public SyncTunableMutatorFactory(TunableHandlerFactory<SyncTunableHandler> handlerFactory) {
		this.handlerFactory = handlerFactory; 
	}
	
	public SyncTunableMutator<?> createMutator() {
		SyncTunableMutator<?> mutator = new SyncTunableMutator<Object>();
		mutator.addTunableHandlerFactory(handlerFactory, new Properties());		
		return mutator;
	}
}
