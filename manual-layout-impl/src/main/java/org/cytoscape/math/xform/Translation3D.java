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
 * @version $Revision: 1.1.1.1 $
 **/
public class Translation3D extends AffineTransform3D {
	/**
	 * Creates a new Translation3D object.
	 *
	 * @param x  DOCUMENT ME!
	 * @param y  DOCUMENT ME!
	 * @param z  DOCUMENT ME!
	 */
	public Translation3D(double x, double y, double z) {
		super(new Matrix4x4(new double[][] {
		                        { 1.0d, 0.0d, 0.0d, x },
		                        { 0.0d, 1.0d, 0.0d, y },
		                        { 0.0d, 0.0d, 1.0d, z },
		                        { 0.0d, 0.0d, 0.0d, 1.0d }
		                    }));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Translation3D getInverse() {
		return new Translation3D(-m_matrix.m_entries[0][3], -m_matrix.m_entries[1][3],
		                         -m_matrix.m_entries[2][3]);
	}
}
