package org.cytoscape.io.internal.write.graphics;

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
		bw.setWidthInPixels(newWP);

		assertEquals(newWP, bw.widthInPixels);
		assertTrue(bw.zoom.getValue() /100 == (newWP/initWP));
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));


		assertTrue(bw.resolution.getSelectedValue() == newWP/bw.widthInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		
	}
	
	@Test
	public void updateHP(){
		
		int newHP = 200;
		bw.setHeightInPixels(newHP);
		
		assertEquals(newHP, bw.heightInPixels);
		assertEquals(((double)newHP)/initHP, bw.zoom.getValue() /100, 0.0);
		assertEquals( ((double)bw.widthInPixels)/initWP, bw.zoom.getValue() /100, 0.0);


		assertTrue(bw.resolution.getSelectedValue() == newHP/bw.heightInInches);
		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/bw.widthInInches);
		
	}
	
	
	@Test
	public void updateWI(){
		
		double newWI = (initWP*3)/72.0;
		bw.setWidthInInches(newWI);
		
		assertEquals(newWI, bw.widthInInches, 0.0);
		assertTrue(bw.zoom.getValue() /100  == (bw.heightInPixels/initHP));
		assertTrue(bw.zoom.getValue() /100 == (bw.widthInPixels/initWP));


		assertTrue(bw.resolution.getSelectedValue() == bw.widthInPixels/newWI);
		assertTrue(bw.resolution.getSelectedValue() == bw.heightInPixels/bw.heightInInches);
		
	}
	
	@Test
	public void updateHI(){
		
		double newHI = (initHP*3)/72.0;
		bw.setHeightInInches(newHI);
		
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
