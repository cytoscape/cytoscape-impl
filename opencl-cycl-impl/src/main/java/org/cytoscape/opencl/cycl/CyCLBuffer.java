package org.cytoscape.opencl.cycl;

import java.nio.*;

import org.lwjgl.opencl.*;
import org.lwjgl.BufferUtils;

/***
 * Provides functionality associated with an OpenCL memory object.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCLBuffer 
{
	private Boolean finalized = false;
	
	private CyCLContext context;
	private Class<?> type;
	private int elements;
	private CLMem memObject;
	private ByteBuffer buffer;
	
	/***
	 * Allocates [sizeof(type) * elements] bytes in device memory without copying any host data
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param type Element type
	 * @param elements Number of elements
	 */
	public CyCLBuffer(CyCLContext context, Class<?> type, int elements)
	{
		this.context = context;
		this.type = type;
		this.elements = elements;
		
		buffer = BufferUtils.createByteBuffer(sizeInBytes());
		
		IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
		memObject = CL10.clCreateBuffer(context.getContext(), CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR, buffer, errorBuffer);
		Util.checkCLError(errorBuffer.get(0));
	}
	
	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, byte[] data)
	{
		this(context, byte.class, data.length);
		setFromHost(data);
	}

	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, short[] data)
	{
		this(context, short.class, data.length);
		setFromHost(data);
	}

	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, int[] data)
	{
		this(context, int.class, data.length);
		setFromHost(data);
	}

	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, long[] data)
	{
		this(context, long.class, data.length);
		setFromHost(data);
	}

	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, float[] data)
	{
		this(context, float.class, data.length);
		setFromHost(data);
	}

	/***
	 * Allocates device memory to fit all elements in data, and copies its contents
	 * 
	 * @param context The context of the device where the memory should be allocated
	 * @param data Data to be copied into device memory
	 */
	public CyCLBuffer(CyCLContext context, double[] data)
	{
		this(context, double.class, data.length);
		setFromHost(data);
	}
	
	/***
	 * Returns the number of bytes associated with the CyCLBuffer's element type.
	 * 
	 * @return sizeof(type)
	 */
	public int elementSize()
	{
		if(type.equals(char.class))
			return Sizeof.cl_char;
		else if(type.equals(byte.class))
			return Sizeof.cl_char;
		else if(type.equals(short.class))
			return Sizeof.cl_short;
		else if(type.equals(float.class))
			return Sizeof.cl_float;
		else if(type.equals(int.class))
			return Sizeof.cl_int;
		else if(type.equals(double.class))
			return Sizeof.cl_double;
		else if(type.equals(long.class))
			return Sizeof.cl_long;
		else
			return 1;
	}
	
	/***
	 * Returns the overall buffer size in bytes.
	 * 
	 * @return sizeof(type) * elements
	 */
	public int sizeInBytes()
	{
		return elementSize() * elements;
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(byte[] data, int length, int offset)
	{
		buffer.put(data);
		buffer.rewind();
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(short[] data, int length, int offset)
	{
		buffer.asShortBuffer().put(data);
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(int[] data, int length, int offset)
	{
		buffer.asIntBuffer().put(data);
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(long[] data, int length, int offset)
	{
		buffer.asLongBuffer().put(data);
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(float[] data, int length, int offset)
	{
		buffer.asFloatBuffer().put(data);
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 * @param length Number of elements to be copied
	 * @param offset Offset in bytes from the device memory's start
	 */
	public void setFromHost(double[] data, int length, int offset)
	{
		buffer.asDoubleBuffer().put(data);
		CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(byte[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(short[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(int[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(long[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(float[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from host to device memory.
	 * 
	 * @param data Data to be copied
	 */
	public void setFromHost(double[] data)
	{
		setFromHost(data, sizeInBytes(), 0);
	}
	
	/***
	 * Copies data from another device buffer to this one.
	 * 
	 * @param src Device buffer with the source data
	 * @param bytes Amount of bytes to be copied
	 * @param offsetSrc Offset in bytes from the start of the source buffer
	 * @param offsetDst Offset in bytes from the start of the destination buffer
	 */
	public void setFromDevice(CyCLBuffer src, long bytes, long offsetSrc, long offsetDst)
	{
		CL10.clEnqueueCopyBuffer(context.getQueue(), src.getMemObject(), memObject, offsetSrc, offsetDst, bytes, null, null);
	}

	/***
	 * Copies data from another device buffer to this one.
	 * 
	 * @param src Device buffer with the source data
	 */
	public void setFromDevice(CyCLBuffer src)
	{
		setFromDevice(src, sizeInBytes(), 0, 0);
	}
	
	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(byte[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(short[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.asShortBuffer().get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(int[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.asIntBuffer().get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(long[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.asLongBuffer().get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(float[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.asFloatBuffer().get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 * @param length Number of elements
	 * @param offset Offset in bytes from the start of the device buffer
	 */
	public void getFromDevice(double[] data, int length, int offset)
	{
		CL10.clEnqueueReadBuffer(context.getQueue(), memObject, CL10.CL_TRUE, offset, buffer, null, null);
		CL10.clFinish(context.getQueue());
		buffer.asDoubleBuffer().get(data);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(byte[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(short[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(int[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(long[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(float[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}

	/***
	 * Copies data from device to host memory.
	 * 
	 * @param data Array that the data will be copied to
	 */
	public void getFromDevice(double[] data)
	{
		getFromDevice(data, sizeInBytes(), 0);
	}
	
	/***
	 * Gets the underlying memory object.
	 * 
	 * @return LWJGL memory object
	 */
	public CLMem getMemObject()
	{
		return memObject;
	}
	
	/***
	 * Frees all device memory associated with the buffer.
	 * CyCLBuffer cannot be used anymore once this method has been executed.
	 */
	public void free()
	{
		try
		{
			this.finalize();
		}
		catch (Throwable exc) {}
	}
	
	/***
	 * Frees all device memory associated with the buffer.
	 * CyCLBuffer cannot be used anymore once this method has been executed.
	 */
	@Override
	protected void finalize() throws Throwable 
	{
		if(finalized)
			return;
		
		Util.checkCLError(CL10.clReleaseMemObject(memObject));
		
		finalized = true;		
		super.finalize();
	}
}
