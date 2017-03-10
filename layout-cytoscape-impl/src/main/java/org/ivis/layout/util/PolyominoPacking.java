package org.ivis.layout.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

/**
 * This class implements a polyomino packing algorithm. A polyomino is a finite
 * set of cells in the infinite planar square grid. The algorithms finds a
 * placement of polyominoes such that the bounding square is minimized.
 * 
 */
public class PolyominoPacking
{
	/**
	 * Polyomino array
	 */
	Polyomino[] polyominoes;

	/**
	 * Bounding rectangles of the polyominoes
	 */
	Rectangle[] rect;

	/**
	 * The grid
	 */
	byte[][] grid;

	/**
	 * Center point of the grid
	 */
	int gcx, gcy;

	/**
	 * Grid size
	 */
	int sizeX;

	/**
	 * Grid size
	 */
	int sizeY;

	/**
	 * The number of already placed polyominoes
	 */
	int curmino;

	/**
	 * Stores the ordering of the polyominoes
	 */
	int[] ind;

	/**
	 * Random generator
	 */
	Random Rgen;

	/**
	 * This method performs polyomino packing.
	 */
	public void pack(Polyomino[] pm, int pcount)
	{
		polyominoes = pm;
		rect = new Rectangle[pcount];

		// make the initial grid
		makeGrid(100, 100, 0);

		// make the random permutation of polyomino cells and
		// calculate the bounding rectangles.
		Rgen = new Random(1);
		for (int k = 0; k < pcount; k++)
			RandomizeMino(k);

		// order the polyominoes in increasing size
		double[] key = new double[pcount];
		ind = new int[pcount];

		for (int i = 0; i < pcount; i++)
			key[i] = -(rect[i].getMaxX() - rect[i].getMinX())
					- (rect[i].getMaxY() - rect[i].getMinY());

		PolyominoQuickSort qsort = new PolyominoQuickSort();
		qsort.sort(pcount, key, ind);

		// place one by one starting from the largest
		for (curmino = 0; curmino < pcount; curmino++)
			putMino(ind[curmino]);
	}

	/**
	 * This creates the grid of given dimensions and fills it with the already
	 * placed polyominoes.
	 */
	void makeGrid(int dimx, int dimy, int mN)
	{
		int i;

		// allocate the grid
		grid = new byte[dimy][];
		for (i = 0; i < dimy; i++)
			grid[i] = new byte[dimx];

		int dx = dimx / 2 - gcx;
		int dy = dimy / 2 - gcy;
		gcx = dimx / 2;
		gcy = dimy / 2;
		sizeX = dimx;
		sizeY = dimy;

		// mark the positions occupied with the already placed
		// polyominoes.
		for (i = 0; i < mN; i++)
		{
			Polyomino p = polyominoes[ind[i]];
			p.x += dx;
			p.y += dy;

			for (int k = 0; k < p.l; k++)
			{
				int xx = (int) (p.coord[k].getX() + p.x);
				int yy = (int) (p.coord[k].getY() + p.y);
				grid[yy][xx] = 1;
			}
		}
	}

	/**
	 * This method checks whether p can be placed in (x,y). For each polyomino,
	 * check if the (x,y) is occupied/fits in the grid.
	 */
	boolean IsFreePlace(int x, int y, Polyomino p)
	{
		for (int k = 0; k < p.l; k++)
		{
			int xx = (int) (p.coord[k].getX() + x);
			int yy = (int) (p.coord[k].getY() + y);
			// return false if the polyomino goes outside the grid
			if (xx < 0 || yy < 0 || xx >= sizeX || yy >= sizeY)
				return false;
			// or the position is occupied
			if (grid[yy][xx] != 0)
				return false;
		}

		// remember the position
		p.x = x;
		p.y = y;
		return true;
	}

	/**
	 * This tries to find a free place in the grid. The function returns true if
	 * the placement is successful.
	 */
	boolean tryPlacing(int pi)
	{
		Polyomino p = polyominoes[pi];

		int cx = gcx - (int) (rect[pi].getMaxX() + rect[pi].getMinX()) / 2;
		int cy = gcy - (int) (rect[pi].getMaxY() + rect[pi].getMinY()) / 2;

		// see if the center point is not occupied
		if (IsFreePlace(cx, cy, p))
			return true;

		// try placing in the increasing distance from the center
		for (int d = 1; d < sizeX / 2; d++)
		{
			for (int i = -d; i < d; i++)
			{
				int i1 = (i + d + 1) / 2 * (((i & 1) == 1) ? 1 : -1);
				if (IsFreePlace(-d + cx, -i1 + cy, p))
					return true;
				if (IsFreePlace(d + cx, i1 + cy, p))
					return true;
				if (IsFreePlace(cx - i1, d + cy, p))
					return true;
				if (IsFreePlace(i1 + cx, -d + cy, p))
					return true;
			}
		}
		return false;
	}

	/**
	 * This method places the given polyomino. The grid is enlarged if
	 * necessary.
	 */
	void putMino(int pi)
	{
		Polyomino p = polyominoes[pi];

		// if the polyomino cannot be placed in the current grid,
		// enlarge it.
		while (!tryPlacing(pi))
		{
			sizeX += 10;
			sizeY += 10;
			makeGrid(sizeX, sizeY, curmino);
		}

		// mark the positions occupied
		for (int k = 0; k < p.l; k++)
		{
			int xx = (int) (p.coord[k].getX() + p.x);
			int yy = (int) (p.coord[k].getY() + p.y);
			grid[yy][xx] = 1;
		}

	}

	/**
	 * This method makes a random permutation of polyomino cells and calculates
	 * the bounding rectangles of the polyominoes.
	 */
	void RandomizeMino(int pi)
	{
		Polyomino p = polyominoes[pi];
		int i;

		// make the random permutation. Theoretically it speeds up the
		// algorithm a little.
		for (i = 0; i < p.l; i++)
		{
			int i1 = Rgen.nextInt(p.l - i) + i;
			Point tmp = p.coord[i];
			p.coord[i] = p.coord[i1];
			p.coord[i1] = tmp;
		}

		// calculate the bounding rectangle of the polyomino
		rect[pi] = new Rectangle();

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		p.x = p.y = 0;

		for (i = 0; i < p.l; i++)
		{
			if (p.coord[i].getX() < minX)
				minX = (int) p.coord[i].getX();
			if (p.coord[i].getY() < minY)
				minY = (int) p.coord[i].getY();
			if (p.coord[i].getX() > maxX)
				maxX = (int) p.coord[i].getX();
			if (p.coord[i].getY() > maxY)
				maxY = (int) p.coord[i].getY();
		}

		rect[pi].x = minX;
		rect[pi].y = minY;
		rect[pi].width = maxX - minX;
		rect[pi].height = maxY - minY;
	}
}
