/*
  Copyright (c) 2001, Nerius Landys
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. The name of the author may be used to endorse or promote products
     derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.cytoscape.math.xform;


/**
 * @version $Revision: 1.1.1.1 $
 **/
public class AxisRotation3D extends AffineTransform3D {
	/**
	 * 
	 */
	public final static byte X_AXIS = (byte) 1;

	/**
	 * 
	 */
	public final static byte Y_AXIS = (byte) 2;

	/**
	 * 
	 */
	public final static byte Z_AXIS = (byte) 4;

	/**
	 * <code>theta</code> in radians.
	 **/
	public AxisRotation3D(byte axisOfRotation, double theta) {
		this(getAxisRotationMatrix(axisOfRotation, theta));
	}

	private AxisRotation3D(Matrix4x4 matrix) {
		super(matrix);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public AxisRotation3D getInverse() {
		final double[][] d = m_matrix.m_entries;

		return new AxisRotation3D(new Matrix4x4(new double[][] {
		                                            { d[0][0], -d[0][1], d[0][2], d[0][3] },
		                                            { -d[1][0], d[1][1], -d[1][2], d[1][3] },
		                                            { d[2][0], -d[2][1], d[2][2], d[2][3] },
		                                            { d[3][0], d[3][1], d[3][2], d[3][3] },
		                                        }));
	}

	private static Matrix4x4 getAxisRotationMatrix(byte axisOfRotation, double theta) {
		final double[][] entries;

		switch (axisOfRotation) {
			case X_AXIS:
				entries = new double[][] {
				              { 1.0d, 0.0d, 0.0d, 0.0d },
				              { 0.0d, Math.cos(theta), -Math.sin(theta), 0.0d },
				              { 0.0d, Math.sin(theta), Math.cos(theta), 0.0d },
				              { 0.0d, 0.0d, 0.0d, 1.0d }
				          };

				break;

			case Y_AXIS:
				entries = new double[][] {
				              { Math.cos(theta), 0.0d, Math.sin(theta), 0.0d },
				              { 0.0d, 1.0d, 0.0d, 0.0d },
				              { -Math.sin(theta), 0.0d, Math.cos(theta), 0.0d },
				              { 0.0d, 0.0d, 0.0d, 1.0d }
				          };

				break;

			case Z_AXIS:
				entries = new double[][] {
				              { Math.cos(theta), -Math.sin(theta), 0.0d, 0.0d },
				              { Math.sin(theta), Math.cos(theta), 0.0d, 0.0d },
				              { 0.0d, 0.0d, 1.0d, 0.0d },
				              { 0.0d, 0.0d, 0.0d, 1.0d }
				          };

				break;

			default:
				throw new IllegalArgumentException("invalid axisOfRotation");
		}

		return new Matrix4x4(entries);
	}
}
