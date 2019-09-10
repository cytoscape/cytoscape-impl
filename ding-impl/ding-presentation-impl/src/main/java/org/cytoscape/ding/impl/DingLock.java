package org.cytoscape.ding.impl;


/**
 * We could just use Object for the lock but using a class with a name makes 
 * it possible to profile lock contention in a profiler such as YourKIT.
 */
public class DingLock extends Object {

}
