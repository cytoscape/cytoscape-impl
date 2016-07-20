package org.ivis.layout.util;

import java.awt.Point;
import java.util.Random;

import org.ivis.layout.sbgn.SbgnPDNode;

/**
 * This class implements several rectangle processing algorithms such as
 * compaction and packing.
 */

public class RectProc
{

	final static double AspectRatio = (1.0 / 1.0);// ysize/xsize

	static void PlaceRandomly(int rN, double[] rX1, double[] rY1, double[] rL,
			double[] rH)
	{
		int index[] = new int[rN];

		double sumL = 0;
		double sumH = 0;

		for (int i = 0; i < rN; i++)
		{
			sumL += rL[i];
			sumH += rH[i];
			index[i] = i;
		}

		Random Rgen = new Random(1);
		for (int i = 0; i < rN; i++)
		{
			int a = Rgen.nextInt(rN);
			int tmp = index[i];
			index[i] = index[a];
			index[a] = tmp;
		}

		sumL /= rN;
		sumH /= rN;
		int numRows = (int) (Math.sqrt(rN) + 0.4999);

		for (int i = 0; i < rN; i++)
		{
			rX1[index[i]] = (i / numRows) * sumL;
			rY1[index[i]] = (i % numRows) * sumH;
		}
	}

	/**
	 * This method packs rectangles using polyomino packing algorithm.
	 * 
	 * @return
	 */
	public static void packRectanglesMino(double buffer, int rN,
			SbgnPDNode[] rectangles)
	{
		// make the intermediate data structure
		double[] rX1 = new double[rN];
		double[] rY1 = new double[rN];
		double[] rW = new double[rN];
		double[] rH = new double[rN];

		for (int i = 0; i < rN; i++)
		{
			rX1[i] = rectangles[i].getCenterX();
			rY1[i] = rectangles[i].getCenterY();
			rW[i] = rectangles[i].getWidth();
			rH[i] = rectangles[i].getHeight();
		}

		for (int i = 0; i < rN; i++)
		{
			rX1[i] -= rW[i] / 2;
			rY1[i] -= rH[i] / 2;
		}

		// do the packing
		packRectanglesMino(buffer, rN, rX1, rW, rY1, rH, rectangles);

		// transfer back the results
		for (int i = 0; i < rN; i++)
		{
			rX1[i] += rW[i] / 2;
			rY1[i] += rH[i] / 2;
		}

		for (int i = 0; i < rN; i++)
		{
			rectangles[i].setCenter(rX1[i], rY1[i]);
		}
	}

	/**
	 * This method packs rectangles using polyomino packing algorithm.
	 */

	static void packRectanglesMino(double buffer, int rN, double[] rX,
			double[] rW, double[] rY, double[] rH, SbgnPDNode[] rectangles)
	{
		if (rN == 0)
			return;

		double stepX = 5, stepY = 5;

		// dynamically calculate the grid step
		// double area = 0;
		//
		// for (int i = 0; i < rN; i++)
		// {
		// // stepX+=rL[i]+delta;
		// // stepY+=rH[i]+delta;
		// area += (rW[i] + delta) * (rH[i] + delta);
		// }
		//
		// double stepX = Math.sqrt(area / (rN * 16));
		// // (stepX+stepY)/(rN*8);
		//

		// adjust respecting the aspect ratio

		double fstep = 2 / (1 + AspectRatio);
		stepY = stepX * AspectRatio * fstep;
		stepX *= fstep;

		// make the polyomino representation
		Polyomino[] minos = new Polyomino[rN];

		for (int i = 0; i < rN; i++)
		{
			// size of the rectangle in grid units
			int W = (int) Math.ceil((rW[i] + buffer) / stepX);
			int H = (int) Math.ceil((rH[i] + buffer) / stepY);

			minos[i] = new Polyomino();
			minos[i].coord = new Point[W * H];

			// create the polyomino cells
			int cnt = 0;
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
				{
					minos[i].coord[cnt] = new Point();
					minos[i].coord[cnt].x = x;
					minos[i].coord[cnt++].y = y;

				}
			minos[i].l = cnt;
			minos[i].label = rectangles[i].label;
		}

		// do the packing
		PolyominoPacking packer = new PolyominoPacking();
		packer.pack(minos, rN);

		// get the results
		for (int i = 0; i < rN; i++)
		{
			rX[i] = minos[i].x * stepX;
			rY[i] = minos[i].y * stepY;
		}

	}
};
