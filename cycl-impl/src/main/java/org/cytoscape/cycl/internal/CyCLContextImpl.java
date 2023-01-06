package org.cytoscape.cycl.internal;

import org.cytoscape.cycl.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.*;
import org.lwjgl.opencl.*;
import org.lwjgl.system.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opencl.CL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class CyCLContextImpl implements CyCLContext
{
  private Boolean finalized = false;
  // private MemoryStack stack;
  
  // private CLContext context;
  // private CLCommandQueue queue;
  private long context;
  private long queue = -1;
  
  public CyCLContextImpl(CyCLPlatform platform, CyCLDevice device)
  {
    CLContextCallback contextCB = null;
    try (MemoryStack stack = stackPush()) {
      IntBuffer errcode_ret = stack.callocInt(1);
      contextCB = CLContextCallback.create((errinfo, private_info, cb, user_data) -> {
        System.err.println("[LWJGL] cl_context_callback");
        System.err.println("\tInfo: " + memUTF8(errinfo));
      });
      PointerBuffer ctxProps = ((CyCLPlatformImpl)platform).getContextProps();
      // System.out.println("contextProps limit = "+ctxProps.limit());
      // System.out.println("Terminator = "+(ctxProps.get(ctxProps.limit() - 1) == NULL));
      context = clCreateContext(ctxProps, device.getDevice(), contextCB, NULL, errcode_ret);
      CyCLUtils.checkCLError(errcode_ret);

      queue = clCreateCommandQueue(context, device.getDevice(), NULL, errcode_ret);
      CyCLUtils.checkCLError(errcode_ret);
    } finally {
      contextCB.free();
    }
  }
  
  public long getContext()
  {
    return context;
  }
  
  public long getQueue()
  {
    return queue;
  }
  
  protected void finalize() throws Throwable {
    if(finalized)
      return;
    
    CL10.clReleaseCommandQueue(queue);
    CL10.clReleaseContext(context);
    
    finalized = true;    
    super.finalize();
  }
}
