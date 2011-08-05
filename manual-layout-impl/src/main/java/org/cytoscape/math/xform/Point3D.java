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
 * Immutable class.
 * @version $Revision: 1.1.1.1 $
 **/
public final class Point3D {
	/**
	 * 
	 */
	public final double x;

	/**
	 * 
	 */
	public final double y;

	/**
	 * 
	 */
	public final double z;

	/**
	 * Creates a new Point3D object.
	 *
	 * @param x  DOCUMENT ME!
	 * @param y  DOCUMENT ME!
	 * @param z  DOCUMENT ME!
	 */
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	//    public Point3D scale(float scaleFactor)
	//    {
	//      return new Point3D(x * scaleFactor, y * scaleFactor, z * scaleFactor);
	//    }

	//    public float distance(Point3D pt3D)
	//    {
	//      return (float) Math.sqrt(square(x - pt3D.x) + square(y - pt3D.y) +
	//                               square(z - pt3D.z));
	//    }

	//    private float square(float f)
	//    {
	//      return f * f;
	//    }
}
