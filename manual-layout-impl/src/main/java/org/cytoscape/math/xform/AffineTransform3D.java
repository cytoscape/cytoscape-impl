/* %% Ignore-License */
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
 * Instances of this class are immutable.
 * @version $Revision: 1.2 $
 **/
public abstract class AffineTransform3D implements Transformation3D {
	final Matrix4x4 m_matrix;

	AffineTransform3D(Matrix4x4 matrix) {
		m_matrix = matrix;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param pt3D DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Point3D transform(Point3D pt3D) {
		double[] arr3D = new double[] { pt3D.x, pt3D.y, pt3D.z };
		transformArr(arr3D);

		return new Point3D(arr3D[0], arr3D[1], arr3D[2]);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param arr3D DOCUMENT ME!
	 */
	public void transformArr(double[] arr3D) {
		m_matrix.transform(arr3D);
	}

	/**
	 * The returned transformation, in its <code>transform()</code> method,
	 * passes a point through the transform
	 * <code>xform</code> and then passes the resulting point through
	 * <code>this</code>.
	 **/
	public AffineTransform3D concatenatePre(AffineTransform3D xform) {
		return new AffineTransform3D(m_matrix.multiply(xform.m_matrix)) {
			};
	}

	/**
	 * The returned transformation, in its <code>transform()</code> method,
	 * passes a point through <code>this</code> and then passes the resulting
	 * point through <code>xform</code>.  Returns exactly
	 * <code>xform.concatenatePre(this)</code>.
	 **/
	public AffineTransform3D concatenatePost(AffineTransform3D xform) {
		return xform.concatenatePre(this);
	}
}
