__kernel void BenchmarkKernel(__global int* a,
							  __global int* b,
							  __global int* c,
							  int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;
    
	int result = 0;
	for (int i = 0; i < id; i++)
	{
		float x = (float)a[i], y = (float)b[i];
		float r = max(1e-5f, sqrt(x * x + y * y));
		x /= r;
		y /= r;
		result += (int)(min(1.0f, pow(x, 0.25f) + pow(y, 0.25f)));
	}
		
	c[id] = result;
}