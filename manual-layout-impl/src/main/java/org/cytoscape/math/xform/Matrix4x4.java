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


/* @version $Revision: 1.1.1.1 $
 */
class Matrix4x4 {
	final double[][] m_entries; // this class is _immutable_; do not modify!

	/*
	 * entries[row][column].
	 */
	Matrix4x4(double[][] entries) {
		m_entries = entries;
	}

	void transform(double[] arr3D) {
		final double x = (arr3D[0] * m_entries[0][0]) + (arr3D[1] * m_entries[0][1])
		                 + (arr3D[2] * m_entries[0][2]) + (1.0d * m_entries[0][3]);
		final double y = (arr3D[0] * m_entries[1][0]) + (arr3D[1] * m_entries[1][1])
		                 + (arr3D[2] * m_entries[1][2]) + (1.0d * m_entries[1][3]);
		final double z = (arr3D[0] * m_entries[2][0]) + (arr3D[1] * m_entries[2][1])
		                 + (arr3D[2] * m_entries[2][2]) + (1.0d * m_entries[2][3]);

		arr3D[0] = x;
		arr3D[1] = y;
		arr3D[2] = z;
	}

	/*
	 * this * matrix
	 */
	Matrix4x4 multiply(Matrix4x4 matrix) {
		final double[][] newEntries = new double[4][];

		for (int i = 0; i < 4; i++) {
			newEntries[i] = new double[4];

			for (int j = 0; j < 4; j++) {
				newEntries[i][j] = (m_entries[i][0] * matrix.m_entries[0][j])
				                   + (m_entries[i][1] * matrix.m_entries[1][j])
				                   + (m_entries[i][2] * matrix.m_entries[2][j])
				                   + (m_entries[i][3] * matrix.m_entries[3][j]);
			}
		}

		return new Matrix4x4(newEntries);
	}
}
