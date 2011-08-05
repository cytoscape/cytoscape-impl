package org.cytoscape.ding.customgraphics;

public class IDGenerator {

	private static IDGenerator generator = new IDGenerator();
	
	public static IDGenerator getIDGenerator() {
		return generator;
	}
	
	
	private Long globalCounter;
	
	public IDGenerator() {
		globalCounter = 0l;
	}
	
	
	public synchronized Long getNextId() {
		return ++globalCounter;
	}
	
	
	public void initCounter(Long currentMax) {
		this.globalCounter = currentMax;
	}
}
