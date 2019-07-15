package org.cytoscape.ding.impl;

import java.awt.Image;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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



public interface DingCanvas extends Callable<Image>, Supplier<Image> {
	
	
	default void setViewport(int width, int height) {}
	
	default void setCenter(double x, double y) {}
	
	default void setScaleFactor(double scaleFactor) {}
	
	
	default Image call() {
		return paintImage();
	}
	
	default Image get() {
		return paintImage();
	}
	
	Image paintImage();
	
	default void dispose() {}
	
//	// Don't know about this yet
//	public abstract void print(Graphics g);
//	
//	default void printNoImposter(Graphics g) {
//		print(g);
//	}
	
}
