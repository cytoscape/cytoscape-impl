package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import javax.swing.*;
import java.awt.*;
import org.cytoscape.application.swing.CyNetworkViewDesktopMgr;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
  */
public class CyDesktopManager implements CyNetworkViewDesktopMgr {
	
	public static int MINIMUM_WIN_WIDTH = 200;
	public static int MINIMUM_WIN_HEIGHT = 200;
	public final CytoscapeDesktop desk;
	public final NetworkViewManager viewManager;
		
	public CyDesktopManager(final CytoscapeDesktop desk, final NetworkViewManager viewManager) { 
		this.desk = desk;
		this.viewManager = viewManager;
	}

	@Override
	public Dimension getDesktopViewAreaSize() {
		JDesktopPane desktop = desk.getNetworkViewManager().getDesktopPane();
		
		return desktop.getSize();
	}

	@Override
	public Rectangle getBounds (CyNetworkView view) {
		try {
			JInternalFrame frame = viewManager.getInternalFrame(view);
			if (frame != null)
				return frame.getBounds();
		} catch (Exception e) {
			// Fall through
		}
		return null;
	}

	@Override
	public void setBounds (CyNetworkView view, Rectangle bounds) {
		try {
			JInternalFrame frame = viewManager.getInternalFrame(view);
			if (frame == null)
				return;
            // It is CRITICAL that this setMaximum be performed before the setBounds().
            // If frame's state is such that isMaximum() is true, then it seems to
            // ignore any setBounds() statements until the iFrame is in a non-maximum
            // state. The result of this is that frame will use its previous
            // setting for the frame bounds when the frame is restored
            // (set to non Maximum state):
            frame.setMaximum(false);
			frame.setBounds(bounds);
		} catch (Exception e) {
			return;
		}	
	}
	
	// Arrange all windows in the desktop according to the given style
	@Override
	public void arrangeWindows(ArrangeType pStyle) {
		JDesktopPane desktop = desk.getNetworkViewManager().getDesktopPane();
		
		final Dimension desktopSize = desktop.getSize();
		
		final JInternalFrame[] allFrames = desktop.getAllFrames();
		
		int frameCount = allFrames.length; 
		if ( frameCount == 0)
			return;

		if (pStyle == ArrangeType.CASCADE) {
			int delta_x = 20;
			int delta_y = 20;
			int delta_block = 50;
						
			int[] x = new int[frameCount];
			int[] y = new int[frameCount];
			int[] w = new int[frameCount];
			int[] h = new int[frameCount];
			x[0] = 0;
			y[0] = 0;
			w[0] = allFrames[0].getWidth();
			h[0] =allFrames[0].getHeight();

			boolean multiBlock = false;
			int blockSize =0;
			for (int i=1; i<frameCount; i++) {
				blockSize++;
				x[i] = x[i-1] + delta_x;
				y[i] = y[i-1] + delta_y;

				if (desktopSize.height - y[i]<MINIMUM_WIN_HEIGHT) {
					y[i] =0;
					multiBlock = true;
				}
				if (desktopSize.width - x[i]<MINIMUM_WIN_WIDTH && !multiBlock) {
					x[i] = x[i-1];
				}
				
				// Determine the w,h for the previous block and start of another block 
				if (y[i]==0 && multiBlock) {
										
					for (int j=0; j< blockSize; j++) {
						if (i-blockSize>0) { //use the same (w, h) as previous block
							w[i-j-1] = w[i-blockSize];
							h[i-j-1] = h[i-blockSize];							
						}
						else {
							w[i-j-1] = desktopSize.width - x[i-1];
							h[i-j-1] = desktopSize.height - y[i-1];							
						}
					}									
					//start of another block
					x[i] = x[i-blockSize] + delta_block; 
					if (x[i] > (desktopSize.width - delta_x * blockSize)) {
						x[i] = x[i-blockSize];
					}
					blockSize =1;	
				}
			}

			// Handle the last block
			if (!multiBlock) { // single block
				for (int i = 0; i < frameCount; i++) {
					w[frameCount-1-i] = desktopSize.width - x[frameCount - 1];
					h[frameCount-1-i] = desktopSize.height - y[frameCount - 1];					
				}
			}
			else { //case for multiBlock
				for (int i = 0; i < blockSize; i++) {
					//use the same (w, h) as previous block
					w[frameCount-1-i] = w[frameCount - blockSize-1];
					h[frameCount-1-i] = h[frameCount - blockSize-1];
					// If w is too wider to fit to the screen, adjust it
					if (w[frameCount-1-i] > desktopSize.width - x[frameCount - 1]) {
						w[frameCount-1-i] = desktopSize.width - x[frameCount - 1];
					}
				}				
			}
			
			if (desktopSize.height - MINIMUM_WIN_HEIGHT < delta_y ) { // WinHeight is too small, This is a special case
				double delta_x1 = ((double)(desktopSize.width - MINIMUM_WIN_WIDTH))/(frameCount-1);
				for (int i = 0; i < frameCount; i++) {
					x[i] = (int) Math.ceil( i * delta_x1);
					y[i] =0;
					w[i] = MINIMUM_WIN_WIDTH;
					h[i] = MINIMUM_WIN_HEIGHT;
				}
			}
			
			//Arrange all frames on the screen
			for (int i=0; i<frameCount; i++) {
				allFrames[frameCount-1-i].setBounds(x[i], y[i], w[i], h[i]);
			}
		}
		else if (pStyle == ArrangeType.GRID) {
			// Determine the max_col and max_row for grid layout 
			int maxCol = (new Double(Math.ceil(Math.sqrt(frameCount)))).intValue();
			int maxRow = maxCol;
			while (true) {
				if (frameCount <= maxCol*(maxRow -1)) {
					maxRow--;
					continue;
				}
				break;
			}

			// Calculate frame layout on the screen, i.e. the number of frames for each column 
			int[] gridLayout = new int[maxCol];
			getGridLayout(frameCount, maxCol, maxRow, gridLayout);
			
			// Apply the layout on screen
			int w = desktopSize.width/maxCol;
			int curFrame = frameCount -1;
			for (int col=maxCol-1; col>=0; col--) {
				int h = desktopSize.height/gridLayout[col];
				
				for (int i=0; i< gridLayout[col]; i++) {
					int x = col * w;
					int y = (gridLayout[col]-i-1)* h;					
					allFrames[curFrame--].setBounds(x, y, w, h);
				}				
			}
		}
		else if (pStyle == ArrangeType.HORIZONTAL) {
			int x = 0;
			int y = 0;
			int w = desktopSize.width;
			int h = desktopSize.height/frameCount;
			if (h < MINIMUM_WIN_HEIGHT ) {
				h = MINIMUM_WIN_HEIGHT;
			}
			
			double delta_y = 0;
			if (frameCount > 1) {
				if (h < MINIMUM_WIN_HEIGHT) {
					delta_y = ((double)(desktopSize.height - MINIMUM_WIN_HEIGHT))/(frameCount-1);						
				}
				else {
					delta_y = ((double)(desktopSize.height))/(frameCount);
				}
			}
			
			for (int i=0; i< frameCount; i++) {
				y = (int)(delta_y * i);
				if (y> desktopSize.height - MINIMUM_WIN_HEIGHT) {
					y = desktopSize.height - MINIMUM_WIN_HEIGHT;
				}
				allFrames[frameCount-i-1].setBounds(x, y, w, h);
			}
		}
		else if (pStyle == ArrangeType.VERTICAL) {
			int x = 0;
			int y = 0;
			int w = desktopSize.width/frameCount;
			int h = desktopSize.height;
			
			if (w < MINIMUM_WIN_WIDTH) {
				w = MINIMUM_WIN_WIDTH;
			}

			double delta_x = 0;
			if (frameCount > 1) {
				if (w < MINIMUM_WIN_WIDTH) {
					delta_x = ((double)(desktopSize.width - MINIMUM_WIN_WIDTH))/(frameCount-1);	
				}
				else {
					delta_x = ((double)desktopSize.width)/frameCount;
				}
			}
			
			for (int i=0; i< frameCount; i++) {
				x = (int)(delta_x * i);
				if (x > desktopSize.width - MINIMUM_WIN_WIDTH) {
					x = desktopSize.width - MINIMUM_WIN_WIDTH;
				}
				allFrames[frameCount-i-1].setBounds(x, y, w, h);
			}
		}
		
		// Clean up.
		System.gc();
	}
			
	//Closes all open windows
	public  void closeAllWindows() {
		JDesktopPane desktop = desk.getNetworkViewManager().getDesktopPane();
		JInternalFrame[] allFrames = desktop.getAllFrames();
		for (int i= allFrames.length -1; i>=0; i--) {
			allFrames[i].dispose();			
		}
	}
		
	// Implementation of grid layout algorithm
	// gridLayout -- an int array-- int[i] holds the number of row for column i 
	private void getGridLayout(final int pTotal, final int pCol, final int pRow, int[] gridLayout) {
		if (pTotal > pRow) {
			int row = -1;
			if (pTotal%pCol == 0) {
				row = pTotal/pCol;
				gridLayout[pCol-1] = row;
			}
			else {
				row = pRow;				
				gridLayout[pCol-1] = pRow;
			}
			getGridLayout(pTotal-row, pCol-1,row, gridLayout);
		}
		else {
			gridLayout[0] = pTotal;
		}		
	}
	
}

