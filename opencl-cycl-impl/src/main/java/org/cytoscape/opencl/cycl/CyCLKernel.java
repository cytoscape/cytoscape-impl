package org.cytoscape.opencl.cycl;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;

public class CyCLKernel 
{
	private Boolean finalized = false;
	
	private CLKernel kernel;
	private CyCLContext context;
	
	public CyCLKernel(CyCLContext context, CyCLProgram program, String kernelName)
	{
		this.context = context;
		
		IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
		kernel = CL10.clCreateKernel(program.getProgram(), kernelName, errorBuffer);
		Util.checkCLError(errorBuffer.get(0));
	}
	
	public void execute(long[] dimsGlobal, long[] dimsLocal, Object... args)
	{
		synchronized (CyCL.sync)
		{
			int a = 0;
			for(Object arg : args)
			{
				if(arg.getClass().equals(CyCLLocalSize.class))
				{
					kernel.setArgSize(a++, ((CyCLLocalSize)arg).getSize());
				}
				else if(arg.getClass().equals(CyCLBuffer.class))
				{
					CL10.clSetKernelArg(kernel, a++, ((CyCLBuffer)arg).getMemObject());
				}
				else if(arg.getClass().equals(CLMem.class))
				{
					CL10.clSetKernelArg(kernel, a++, ((CLMem)arg));
				}
				else
				{
					if(arg.getClass().equals(Byte.class))
						kernel.setArg(a++, (byte)arg);
					else if(arg.getClass().equals(Short.class))
						kernel.setArg(a++, (short)arg);
					else if(arg.getClass().equals(Integer.class))
						kernel.setArg(a++, (int)arg);
					else if(arg.getClass().equals(Long.class))
						kernel.setArg(a++, (long)arg);
					else if(arg.getClass().equals(Float.class))
						kernel.setArg(a++, (float)arg);
					else if(arg.getClass().equals(Double.class))
						kernel.setArg(a++, (double)arg);
				}
			}
			
			PointerBuffer bufferGlobal = PointerBuffer.allocateDirect(dimsGlobal.length);
			for (int i = 0; i < dimsGlobal.length; i++)
				bufferGlobal.put(i, dimsGlobal[i]);
			PointerBuffer bufferLocal = null;
			if(dimsLocal != null)
			{
				bufferLocal = PointerBuffer.allocateDirect(dimsLocal.length);
				for (int i = 0; i < dimsLocal.length; i++)
					bufferLocal.put(i, dimsLocal[i]);
			}
		
			try
			{
				Util.checkCLError(CL10.clEnqueueNDRangeKernel(context.getQueue(), kernel, dimsGlobal.length, null, bufferGlobal, bufferLocal, null, null));		
			}
			catch (Exception e) {}
		}
	}
	
	@Override
	protected void finalize() throws Throwable 
	{
		if(finalized)
			return;
		
		Util.checkCLError(CL10.clReleaseKernel(kernel));
		
		finalized = true;		
		super.finalize();
	}
}