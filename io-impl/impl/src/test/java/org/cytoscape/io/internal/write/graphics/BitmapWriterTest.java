package org.cytoscape.io.internal.write.graphics;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.List;
import com.lowagie.text.pdf.codec.Base64.OutputStream;

public class BitmapWriterTest {
	private static final Logger logger = LoggerFactory.getLogger(PDFWriterTest.class);

	protected BitmapWriter bw;
	int initHP, initWP;
	
	@Before
	public void init(){
		Set<String> mySet = new HashSet<String>();
		for( String s : ImageIO.getWriterFormatNames()){
			mySet.add(s);
		}
		
		initHP = 400;
		initWP = 550;
		
		RenderingEngine<CyNetwork> re = ((RenderingEngine<CyNetwork>)( mock(RenderingEngine.class)));
		View<CyNetwork> view= (View<CyNetwork> )mock(View.class);
		when(view.getVisualProperty(NETWORK_WIDTH)).thenReturn(550.0);
		when(view.getVisualProperty(NETWORK_HEIGHT)).thenReturn(400.0);
		when(re.getViewModel()).thenReturn(view);
		
		bw = new BitmapWriter(re , mock(OutputStream.class), mySet);
		
	}
	
	@Test
	public void updateZoomTest(){
		BoundedDouble newZoom = new BoundedDouble(0.0,100.0, 500.0, true, false);
		newZoom.setValue(50.0);
		
		bw.setZoom(newZoom);
		assertTrue(50/100  == (bw.heightInPixels/initHP));
		assertTrue(50/100 == (bw.widthInPixels/initWP));

		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/bw.widthInInches);
		
	}
	
	@Test
	public void updateZoomTest2(){
		BoundedDouble newZoom = new BoundedDouble(0.0,100.0, 500.0, true, false);
		newZoom.setValue(200.0);
		
		bw.setZoom(newZoom);
		
		assertEquals(newZoom.getValue(), bw.zoom.getValue());
		assertTrue(newZoom.getValue()/100  == (bw.heightInPixels/initHP));
		assertTrue(newZoom.getValue()/100 == (bw.widthInPixels/initWP));

		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/bw.widthInInches);
		
	}
	
	@Test
	public void updateWP(){
		
		int newWP = 1100;
		ListSingleSelection<String> units = bw.getUnits();
		units.setSelectedValue("pixels");
		bw.setUnits(units);
		bw.setWidth(new Double(newWP));

		assertEquals(newWP, bw.widthInPixels);
		assertTrue(bw.zoom.getValue() /100 == (newWP/initWP));
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));


		assertTrue(bw.resolution.getSelectedValue() == newWP/bw.widthInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		
	}
	
	@Test
	public void updateHP(){
		
		int newHP = 200;
		ListSingleSelection<String> units = bw.getUnits();
		units.setSelectedValue("pixels");
		bw.setUnits(units);
		bw.setHeight(new Double(newHP));
		
		assertEquals(newHP, bw.heightInPixels);
		assertEquals(((double)newHP)/initHP, bw.zoom.getValue() /100, 0.0);
		assertEquals( ((double)bw.widthInPixels)/initWP, bw.zoom.getValue() /100, 0.0);


		assertTrue(bw.resolution.getSelectedValue() == newHP/bw.heightInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/bw.widthInInches);
		
	}
	
	
	@Test
	public void updateWI(){
		
		double newWI = (initWP*3)/72.0;
		ListSingleSelection<String> units = bw.getUnits();
		units.setSelectedValue("inches");
		bw.setUnits(units);
		bw.setWidth(newWI);
		
		assertEquals(newWI, bw.widthInInches, 0.0);
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));
		assertTrue(bw.zoom.getValue() /100 == (bw.widthInPixels/initWP));


		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/newWI);
		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		
	}
	
	@Test
	public void updateHI(){
		
		double newHI = (initHP*3)/72.0;
		ListSingleSelection<String> units = bw.getUnits();
		units.setSelectedValue("inches");
		bw.setUnits(units);
		bw.setHeight(newHI);
		
		assertEquals(newHI, bw.heightInInches, 0.0);
		
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));
		assertTrue(bw.zoom.getValue() /100 == (bw.widthInPixels/initWP));


		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/newHI);
		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/bw.widthInInches);
		
	}
	
	@Test
	public void updateResolution(){
		
		ListSingleSelection<Integer> newResolution = bw.getResolution();
		int newDpi = 300;
		newResolution.setSelectedValue(newDpi);
		bw.setResolution(newResolution);
		
		assertEquals(newDpi, bw.getResolution().getSelectedValue().intValue());
		
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));
		assertTrue(bw.zoom.getValue() /100 == (bw.widthInPixels/initWP));

		assertTrue(newDpi == bw.heightInPixels/bw.heightInInches);
		assertTrue(newDpi == bw.widthInPixels/bw.widthInInches);
	}
	
}
