package org.cytoscape.graph.render.stateful;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.util.List;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2021 The Cytoscape Consortium
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
import junit.framework.TestCase;


public class MeasuredLineCreatorTest extends TestCase {
	Font serif;
	Font sansSerif;
	FontRenderContext frc;
	LabelInfo mlc;


	public void setUp() {
		// For whatever reason, this will force a GraphicsEnvironment to be created,
		// which ensures that all of the tests work that make use of GlyphVectors
		// work as designed.
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		serif = new Font("Serif",Font.PLAIN,10);
		sansSerif = new Font("SansSerif",Font.BOLD,12);
		frc = new FontRenderContext(null,true,true);
	}

	public void testOneLine() {
		mlc = new LabelInfo("homer",serif,frc,true,100);
		printLines("one line",mlc);
		assertTrue(mlc.getMeasuredLines().size() == 1);
	}

	public void testOneNewLine() {
		mlc = new LabelInfo("homer\nmarge",serif,frc,true,100);
		printLines("one newline",mlc);
		assertEquals(2, mlc.getMeasuredLines().size());
	}

	public void testLongLine() {
		mlc = new LabelInfo("homer bart lisa marge",serif,frc,true,10);
		printLines("long line",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 1);
	}

	public void testLongLineAndNewLines() {
		mlc = new LabelInfo("homer bart lisa marge\nmaggie\nsmithers",serif,frc,false,10);
		printLines("long line and newlines",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 3);
	}

	public void testLongLineAndSpaces() {
		mlc = new LabelInfo("homer bart lisa marge          smithers",serif,frc,true,10);
		printLines("long line and spaces",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 2);
	}

	public void testLongWord() {
		mlc = new LabelInfo("homerbartlisamargesmithers",serif,frc,false,10);
		printLines("long word",mlc);
		assertTrue(mlc.getMeasuredLines().size() == 1);
	}

	public void testWidthUpdate() {
		mlc = new LabelInfo("homer\nmarge",serif,frc,true,100);
		printLines("width update",mlc);
		double w = mlc.getMaxLineWidth();
		for ( LabelLineInfo ml : mlc.getMeasuredLines() )
			if ( ml.getWidth() == w )
				return;
		
		fail("max line width should have updated");
	}

	public void testFirstLineNotEmpty() {
		mlc = new LabelInfo("homerbart lisa margesmithers",serif,frc,false,10);
		printLines("first line not empty",mlc);
		List<LabelLineInfo> ml = mlc.getMeasuredLines(); 
		assertFalse( ml.get(0).getLine().equals("") );
	}

	public void testLastLineNotEmpty() {
		mlc = new LabelInfo("homerbart lisa margesmithers",serif,frc,false,10);
		printLines("last line not empty",mlc);
		List<LabelLineInfo> ml = mlc.getMeasuredLines(); 
		assertFalse( ml.get(ml.size()-1).getLine().equals("") );
	}

	public void testTotalHeight() {
		mlc = new LabelInfo("homerbart lisa margesmithers",serif,frc,false,10);
		printLines("total height",mlc);
		double h = mlc.getTotalHeight();
		double total = 0;
		for ( LabelLineInfo ml : mlc.getMeasuredLines() )
			total += ml.getHeight();

		assertEquals( total, h, 0.001 ); 
	}

	public void testRespectFontHeight() {
		mlc = new LabelInfo("homerbart lisa margesmithers",serif,frc,false,10);
		double h1 = mlc.getTotalHeight();
		printLines("respect font height 1",mlc);
		mlc = new LabelInfo("homerbart lisa margesmithers",sansSerif,frc,false,10);
		double h2 = mlc.getTotalHeight();
		printLines("respect font height 2",mlc);

		assertTrue( h1 < h2 );
	}

	public void testRespectOverallWidthLimit() {
		mlc = new LabelInfo("homer marge bart lisa maggie smithers",
		                              serif,frc,false,50.0);
		double mw = mlc.getMaxLineWidth();
		printLines("respect overall width",mlc);
		
		assertTrue( mw < (50.0*2.0) );
	}

	private void printLines(String title, LabelInfo mlx) {
		System.out.println("------------------------- " + title);
		System.out.println("max line width: " + mlx.getMaxLineWidth());
		System.out.println("total height  : " + mlx.getTotalHeight());
		for ( LabelLineInfo ml : mlc.getMeasuredLines() )
			System.out.println(ml.toString());
		System.out.println();
	}
}
