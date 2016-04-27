package org.cytoscape.internal.view;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.swing.CyNetworkViewDesktopMgr;
import org.cytoscape.view.model.CyNetworkView;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyDesktopManager implements CyNetworkViewDesktopMgr {
	
	public static int MINIMUM_WIN_WIDTH = 200;
	public static int MINIMUM_WIN_HEIGHT = 200;
	
	private final NetworkViewMediator netViewMediator;
		
	public CyDesktopManager(final NetworkViewMediator netViewMediator) { 
		this.netViewMediator = netViewMediator;
	}

	@Override
	public Dimension getDesktopViewAreaSize() {
		return netViewMediator.getNetworkViewMainPanel().getSize();
	}

	@Override
	public Rectangle getBounds(final CyNetworkView view) {
		Rectangle bounds = null;
		final NetworkViewFrame frame = netViewMediator.getNetworkViewFrame(view);
		
		if (frame != null) {
			bounds = frame.getBounds();
		} else {
			final NetworkViewContainer card = netViewMediator.getNetworkViewCard(view);
			
			if (card != null)
				bounds = card.getBounds();
		}
		
		return bounds;
	}

	@Override
	public void setBounds(CyNetworkView view, Rectangle bounds) {
		// Does not do anything anymore since version 3.4...
	}
	
	@Override
	public void arrangeWindows(final ArrangeType type) {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] devices = ge.getScreenDevices();
		final CyNetworkView currentView = netViewMediator.getNetworkViewMainPanel().getCurrentNetworkView();
		
		final Set<NetworkViewFrame> allViewFrames = netViewMediator.getAllNetworkViewFrames();
		
		// Group frames by monitor
		for (GraphicsDevice gd : devices) {
			final GraphicsConfiguration gc = gd.getDefaultConfiguration();
			final List<NetworkViewFrame> frames = new ArrayList<>();
			
			for (NetworkViewFrame f : allViewFrames) {
				if (f.getGraphicsConfiguration().equals(gc)) {
					if (type == ArrangeType.CASCADE) {
						// If cascade, the current view frame must be the last one in order to be totally visible
						final NetworkViewFrame lastFrame = frames.isEmpty() ? null : frames.get(frames.size() - 1);
						final CyNetworkView lastView = lastFrame == null ? null : lastFrame.getNetworkView();
						
						if (lastView != null && lastView.equals(currentView))
							frames.add(frames.size() - 1, f);
						else
							frames.add(f);
					} else {
						frames.add(f);
					}
				}
			}
			
			if (!frames.isEmpty()) {
				// Calculate the actual screen area by removing screen insets such as Menu Bars, Docks, etc.
				final Rectangle effectiveScreenArea = getEffectiveScreenArea(gc);
				arrangeWindows(frames.toArray(new NetworkViewFrame[frames.size()]), type, effectiveScreenArea);
			}
		}
	}

	private Rectangle getEffectiveScreenArea(final GraphicsConfiguration gc) {
		final Rectangle bounds = gc.getBounds();
		final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		final Rectangle rect = new Rectangle();
		rect.x = bounds.x + screenInsets.left;
		rect.y = bounds.y + screenInsets.top;
		rect.height = bounds.height - screenInsets.top - screenInsets.bottom;
		rect.width = bounds.width - screenInsets.left - screenInsets.right;
		
		return rect;
	}
	
	private void arrangeWindows(final NetworkViewFrame[] frames, final ArrangeType type, final Rectangle bounds) {
		final int frameCount = frames != null ? frames.length : 0;
		
		if (frameCount == 0)
			return;

		final int screenX = bounds.x;
		final int screenY = bounds.y;
		final int screenWidth = bounds.width;
		final int screenHeight = bounds.height;
		
		if (type == ArrangeType.CASCADE) {
			int delta_x = 20;
			int delta_y = 20;
			int delta_block = 50;
						
			int[] x = new int[frameCount];
			int[] y = new int[frameCount];
			int[] w = new int[frameCount];
			int[] h = new int[frameCount];
			x[0] = 0;
			y[0] = 0;
			w[0] = frames[0].getWidth();
			h[0] = frames[0].getHeight();

			boolean multiBlock = false;
			int blockSize = 0;
			
			for (int i = 1; i < frameCount; i++) {
				blockSize++;
				x[i] = x[i - 1] + delta_x;
				y[i] = y[i - 1] + delta_y;

				if (screenHeight - y[i] < MINIMUM_WIN_HEIGHT) {
					y[i] = 0;
					multiBlock = true;
				}

				if (screenWidth - x[i] < MINIMUM_WIN_WIDTH && !multiBlock)
					x[i] = x[i - 1];
				
				// Determine the w,h for the previous block and start of another block 
				if (y[i] == 0 && multiBlock) {
					for (int j = 0; j < blockSize; j++) {
						if (i - blockSize > 0) { // use the same (w, h) as previous block
							w[i - j - 1] = w[i - blockSize];
							h[i - j - 1] = h[i - blockSize];
						} else {
							w[i - j - 1] = screenWidth - x[i - 1];
							h[i - j - 1] = screenHeight - y[i - 1];
						}
					}
					
					// start of another block
					x[i] = x[i - blockSize] + delta_block;

					if (x[i] > (screenWidth - delta_x * blockSize))
						x[i] = x[i - blockSize];

					blockSize = 1;
				}
			}

			// Handle the last block
			if (!multiBlock) { // single block
				for (int i = 0; i < frameCount; i++) {
					w[frameCount - 1 - i] = screenWidth - x[frameCount - 1];
					h[frameCount - 1 - i] = screenHeight - y[frameCount - 1];
				}
			} else { // case for multiBlock
				for (int i = 0; i < blockSize; i++) {
					// use the same (w, h) as previous block
					w[frameCount - 1 - i] = w[frameCount - blockSize - 1];
					h[frameCount - 1 - i] = h[frameCount - blockSize - 1];
					
					// If w is too wider to fit to the screen, adjust it
					if (w[frameCount - 1 - i] > screenWidth - x[frameCount - 1])
						w[frameCount - 1 - i] = screenWidth - x[frameCount - 1];
				}
			}
			
			if (screenHeight - MINIMUM_WIN_HEIGHT < delta_y) { // WinHeight is too small, This is a special case
				double delta_x1 = ((double) (screenWidth - MINIMUM_WIN_WIDTH)) / (frameCount - 1);
				
				for (int i = 0; i < frameCount; i++) {
					x[i] = (int) Math.ceil(i * delta_x1);
					y[i] = 0;
					w[i] = MINIMUM_WIN_WIDTH;
					h[i] = MINIMUM_WIN_HEIGHT;
				}
			}
			
			// Arrange all frames on the screen
			for (int i = 0; i < frameCount; i++) {
				final NetworkViewFrame f = frames[i];
				f.setBounds(x[i] + screenX, y[i] + screenY, w[i], h[i]);
				f.toFront();
			}
		} else if (type == ArrangeType.GRID) {
			// Determine the max_col and max_row for grid layout
			int maxCol = (new Double(Math.ceil(Math.sqrt(frameCount)))).intValue();
			int maxRow = maxCol;

			while (true) {
				if (frameCount <= maxCol * (maxRow - 1)) {
					maxRow--;
					continue;
				}
				break;
			}

			// Calculate frame layout on the screen, i.e. the number of frames for each column
			int[] gridLayout = new int[maxCol];
			getGridLayout(frameCount, maxCol, maxRow, gridLayout);

			// Apply the layout on screen
			int w = screenWidth / maxCol;
			int curFrame = frameCount - 1;

			for (int col = maxCol - 1; col >= 0; col--) {
				int h = screenHeight / gridLayout[col];

				for (int i = 0; i < gridLayout[col]; i++) {
					int x = col * w;
					int y = (gridLayout[col] - i - 1) * h;
					frames[curFrame--].setBounds(x + screenX, y + screenY, w, h);
				}
			}
		} else if (type == ArrangeType.HORIZONTAL) {
			int x = 0;
			int y;
			int w = screenWidth;
			int h = screenHeight / frameCount;

			if (h < MINIMUM_WIN_HEIGHT)
				h = MINIMUM_WIN_HEIGHT;

			double delta_y = 0;

			if (frameCount > 1) {
				if (h < MINIMUM_WIN_HEIGHT)
					delta_y = ((double) (screenHeight - MINIMUM_WIN_HEIGHT)) / (frameCount - 1);
				else
					delta_y = ((double) (screenHeight)) / (frameCount);
			}

			for (int i = 0; i < frameCount; i++) {
				y = (int) (delta_y * i);

				if (y > screenHeight - MINIMUM_WIN_HEIGHT)
					y = screenHeight - MINIMUM_WIN_HEIGHT;

				frames[frameCount - i - 1].setBounds(x + screenX, y + screenY, w, h);
			}
		} else if (type == ArrangeType.VERTICAL) {
			int x;
			int y = 0;
			int w = screenWidth / frameCount;
			int h = screenHeight;

			if (w < MINIMUM_WIN_WIDTH)
				w = MINIMUM_WIN_WIDTH;

			double delta_x = 0;

			if (frameCount > 1) {
				if (w < MINIMUM_WIN_WIDTH)
					delta_x = ((double) (screenWidth - MINIMUM_WIN_WIDTH)) / (frameCount - 1);
				else
					delta_x = ((double) screenWidth) / frameCount;
			}

			for (int i = 0; i < frameCount; i++) {
				x = (int) (delta_x * i);

				if (x > screenWidth - MINIMUM_WIN_WIDTH)
					x = screenWidth - MINIMUM_WIN_WIDTH;

				frames[frameCount - i - 1].setBounds(x + screenX, y + screenY, w, h);
			}
		}
		
		// Clean up.
		System.gc();
	}
			
	// Implementation of grid layout algorithm
	// gridLayout -- an int array-- int[i] holds the number of row for column i 
	private void getGridLayout(final int pTotal, final int pCol, final int pRow, int[] gridLayout) {
		if (pTotal > pRow) {
			int row;

			if (pTotal % pCol == 0) {
				row = pTotal / pCol;
				gridLayout[pCol - 1] = row;
			} else {
				row = pRow;
				gridLayout[pCol - 1] = pRow;
			}
			
			getGridLayout(pTotal - row, pCol - 1, row, gridLayout);
		} else {
			gridLayout[0] = pTotal;
		}	
	}
}

