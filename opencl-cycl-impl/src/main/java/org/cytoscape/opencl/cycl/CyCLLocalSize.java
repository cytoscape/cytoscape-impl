package org.cytoscape.opencl.cycl;


/***
 * Specifies the amount of local memory in an argument of a kernel call.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCLLocalSize 
{
	private Long size;
	
	/***
	 * Initializes the object with the specified amount of bytes.
	 * 
	 * @param sizeInBytes Size in bytes
	 */
	public CyCLLocalSize(Long sizeInBytes)
	{
		size = sizeInBytes;
	}
	
	/***
	 * Gets the amount of bytes specified by the object.
	 * 
	 * @return Size in bytes
	 */
	public Long getSize()
	{
		return size;
	}
}