__kernel void Init(__global float2* nodeVelocity,
                   int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;
    
    nodeVelocity[id] = (float2)(0.0f, 0.0f);
}

// Calculates the gravity force between two nodes.
// Gravity constant is premultiplied in mass1.
#ifdef CYCL_GPU
float2 calcGravity(float2 pos1, float2 pos2, float mass1, float mass2, float2 force)
{
    float2 diff = (float2)(pos1.x - pos2.x, pos1.y - pos2.y);
    
    float dist = rsqrt(diff.x * diff.x + diff.y * diff.y + 1e-6f);  // rsqrt is much faster than 1 / sqrt
    dist = dist * dist * dist;                              // 3rd power to normalize diff vector
    
    float v = mass1 * mass2 * dist;
    
    // Equivalent of force + diff * v
    return (float2)(fma(diff.x, v, force.x), fma(diff.y, v, force.y));
}
#else
float16 calcGravity(float2 pos1, float8 pos2x, float8 pos2y, float8 mass2, float16 force)
{
    float8 diffx = pos1.x - pos2x;
		float8 diffy = pos1.y - pos2y;
	
    float8 dist = rsqrt(diffx * diffx + diffy * diffy + 1e-6f);
    
    float8 v = dist * dist * dist * mass2;		// 3rd power to normalize diff vector
    
    // Equivalent of force + diff * v
    return fma((float16)(diffx, diffy), (float16)(v, v), force);
}
#endif

#ifdef CYCL_GPU

#define POS(i) (float2)(s_posX[i], s_posY[i])
#define MASS(i) s_mass[i]

__kernel void CalcForcesGravity(__local float* s_posX, __local float* s_posY, __local float* s_mass,
                                __global float* nodePosX, __global float* nodePosY,
                                __global float* nodeMass,
                                __global float2* nodeForce,
                                unsigned int n, unsigned int npadded)
{
    unsigned int id1 = get_global_id(0);
    unsigned int localId = get_local_id(0);
    unsigned int groupSize = get_local_size(0);
    
    // Get data for the current node
    float2 node1;
    float mass1;
    if (id1 < n)
    {
        node1 = (float2)(nodePosX[id1], nodePosY[id1]);
        mass1 = nodeMass[id1];
    }
    float2 force = (float2)(0, 0);
    
    // Iterate over all nodes for (anti)gravity force
    unsigned int lastPreloaded = 0;
    while (lastPreloaded < n)
    {
        // Each thread in the block preloads the position and mass of a node
        // into local memory to be used later by the entire block.
        if (lastPreloaded + localId < npadded)
        {
            unsigned int globalId = lastPreloaded + localId;
            s_posX[localId] = nodePosX[globalId];
            s_posY[localId] = nodePosY[globalId];
            s_mass[localId] = nodeMass[globalId];
        }
        barrier(CLK_LOCAL_MEM_FENCE);
        
        // Using the preloaded data in local memory, calculate
        // interactions and update the force value.
        if (id1 < n)
        {
            unsigned int id2 = 0;
            // Node positions and mass are padded to a multiple of 16,
            // so this loop can be partially unrolled.
            while (id2 < groupSize && lastPreloaded + id2 < n)
            {
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                
                
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
                force = calcGravity(node1, POS(id2), mass1, MASS(id2), force);
                id2++;
            }
        }
        
        lastPreloaded += groupSize;
        barrier(CLK_LOCAL_MEM_FENCE);
        // Synchronization is performed at the end as well, to ensure no preload
        // operation overwrites a value another thread might still need.
    }
    
    if (id1 < n)
        nodeForce[id1] = force;
}
#else

#define REDUCE16TO2(v) (float2)((v).s0 + (v).s1 + (v).s2 + (v).s3 + (v).s4 + (v).s5 + (v).s6 + (v).s7, (v).s8 + (v).s9 + (v).sa + (v).sb + (v).sc + (v).sd + (v).se + (v).sf)

__kernel void CalcForcesGravity(__global float8* nodePosX, __global float8* nodePosY,
                                __global float8* nodeMass,
                                __global float2* nodeForce,
                                unsigned int n)
{
		unsigned int id1 = get_global_id(0);
		if (id1 >= n)
				return;
	
		// Get data for the current node
		float2 pos1x = ((float2*)nodePosX)[id1], pos1y = ((float2*)nodePosY)[id1];
		float16 f0 = (float16)(0.0f);
		float16 f1 = (float16)(0.0f);
		
		// Iterate over all nodes for (anti)gravity force
		unsigned int id2 = 0;
		unsigned int n8 = n / 4;
		while (id2 < n8)
		{
			  float8 pos2x = nodePosX[id2], pos2y = nodePosY[id2];
			  float8 mass2 = nodeMass[id2];			
				f0 = calcGravity((float2)(pos1x.s0, pos1y.s0), pos2x, pos2y, mass2, f0);
				f1 = calcGravity((float2)(pos1x.s1, pos1y.s1), pos2x, pos2y, mass2, f1);			
				id2++;
			
			  pos2x = nodePosX[id2], pos2y = nodePosY[id2];
			  mass2 = nodeMass[id2];			
				f0 = calcGravity((float2)(pos1x.s0, pos1y.s0), pos2x, pos2y, mass2, f0);
				f1 = calcGravity((float2)(pos1x.s1, pos1y.s1), pos2x, pos2y, mass2, f1);			
				id2++;
		}
		
		float2 mass1 = ((float2*)nodeMass)[id1];
		
		nodeForce[id1 * 2 + 0] = REDUCE16TO2(f0) * mass1.s0;
		nodeForce[id1 * 2 + 1] = REDUCE16TO2(f1) * mass1.s1;
}
#endif

__kernel void PrepareEdgeRepulsion(__global float* nodePosX, __global float* nodePosY,
                                   __global unsigned int* edgeSource, __global unsigned int* edgeTarget,
                                   __global float* edgeStartX, __global float* edgeStartY,
                                   __global float* edgeTangX, __global float* edgeTangY,
                                   __global float* edgeLength,
                                   unsigned int n)
{
    for (unsigned int id = get_global_id(0); id < n; id += get_global_size(0))
    {
        unsigned int source = edgeSource[id], target = edgeTarget[id];
        float2 sourcePos = (float2)(nodePosX[source], nodePosY[source]);
        float2 targetPos = (float2)(nodePosX[target], nodePosY[target]);
        
        edgeStartX[id] = sourcePos.x;
        edgeStartY[id] = sourcePos.y;
        
        float2 tangent = (float2)(targetPos.x - sourcePos.x, targetPos.y - sourcePos.y);
        float length = hypot(tangent.x, tangent.y);
        edgeLength[id] = length;
        length = 1.0f / length;
        tangent = (float2)(tangent.x * length, tangent.y * length);
        edgeTangX[id] = tangent.x;
        edgeTangY[id] = tangent.y;
    }
}

// Calculates interaction between a node and its closest point on an edge
float2 calcEdgeRepulsion (float2 pos1, float mass1, float2 edgePos, float2 edgeTangent, float edgeLength, float2 edgeMass, float2 force)
{
    // Project the node's relative position onto the edge
    pos1 = (float2)(pos1.x - edgePos.x, pos1.y - edgePos.y);
    float parallelDist = pos1.x * edgeTangent.x + pos1.y * edgeTangent.y;   // dotp
    parallelDist = clamp(parallelDist, 0.0f, edgeLength);                   // make sure the point lies on the edge
    
    float mass2 = edgeMass.x + (edgeMass.y - edgeMass.x) * (parallelDist / edgeLength);
    
    float2 pos2 = edgeTangent * parallelDist;   // tangent * dotp = point on edge
    float2 diff = (float2)(pos1.x - pos2.x, pos1.y - pos2.y);
    
    float dist = diff.x * diff.x + diff.y * diff.y;
    if (dist < 1e-5f)           // Either too close or own edge
        dist = 0.0f;
    else
        dist = rsqrt(max(dist, 1.0f));     // rsqrt is faster than sqrt
    dist = dist * dist * dist;  // 3rd power to normalize diff vector
    
    float v = mass1 * mass2 * dist;
    
    // Equivalent of force + diff * v
    return (float2)(fma(diff.x, v, force.x), fma(diff.y, v, force.y));
}

#define START(i) (float2)(s_startX[i], s_startY[i])
#define TANGENT(i) (float2)(s_tangX[i], s_tangY[i])
#define LENGTH(i) s_length[i]
#define EDGEMASS(i) (float2)(s_massStart[i], s_massEnd[i])

__kernel void CalcForcesEdgeRepulsion(__local float* s_startX, __local float* s_startY,
                                      __local float* s_tangX, __local float* s_tangY,
                                      __local float* s_length,
                                      __local float* s_massStart, __local float* s_massEnd,
                                      __global float* nodePosX, __global float* nodePosY,
                                      __global float* nodeMass,
                                      __global float* edgeStartX, __global float* edgeStartY,
                                      __global float* edgeTangX, __global float* edgeTangY,
                                      __global float* edgeLength,
                                      __global float* edgeMassStart, __global float* edgeMassEnd,
                                      __global float2* nodeForce,
                                      unsigned int n, unsigned int nedges)
{
    unsigned int id1 = get_global_id(0);
    unsigned int localId = get_local_id(0);
    unsigned int groupSize = get_local_size(0);
    
    // Get data for the current node
    float2 node1;
    float mass1;
    if (id1 < n)
    {
        node1 = (float2)(nodePosX[id1], nodePosY[id1]);
        mass1 = nodeMass[id1];
    }
    float2 force = (float2)(0, 0);
    
    // Iterate over all nodes for (anti)gravity force
    unsigned int lastPreloaded = 0;
    while (lastPreloaded < nedges)
    {
        // Each thread in the block preloads the position and mass of a node
        // into local memory to be used later by the entire block.
        if (lastPreloaded + localId < nedges)
        {
            unsigned int globalId = lastPreloaded + localId;
            s_startX[localId] = edgeStartX[globalId];
            s_startY[localId] = edgeStartY[globalId];
            s_tangX[localId] = edgeTangX[globalId];
            s_tangY[localId] = edgeTangY[globalId];
            s_length[localId] = edgeLength[globalId] + 1e-10f;
            s_massStart[localId] = edgeMassStart[globalId];
            s_massEnd[localId] = edgeMassEnd[globalId];
        }
        barrier(CLK_LOCAL_MEM_FENCE);
        
        // Using the preloaded data in local memory, calculate
        // interactions and update the force value.
        if (id1 < n)
        {
            unsigned int id2 = 0;
            // Edges are padded to a multiple of 16,
            // so this loop can be partially unrolled.
            while (id2 < groupSize && lastPreloaded + id2 < nedges)
            {
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                
                
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
                force = calcEdgeRepulsion(node1, mass1, START(id2), TANGENT(id2), LENGTH(id2), EDGEMASS(id2), force);
                id2++;
            }
        }
        
        lastPreloaded += groupSize;
        barrier(CLK_LOCAL_MEM_FENCE);
        // Synchronization is performed at the end as well, to ensure no preload
        // operation overwrites a value another thread might still need.
    }
    
    if (id1 < n)
        nodeForce[id1] += force;
}

// Groups of 16 threads work together to compute the overall spring
// force for one node, reduce their individual values to one and let
// the first thread store the value in global memory in the end.

#ifdef CYCL_GPU
__kernel void CalcForcesSpringDrag(__local float2* s_buffer,
                                   __global float* nodePosX, __global float* nodePosY,
                                   __global unsigned int* edges, __global unsigned int* edgeOffsets, __global unsigned int* edgeCounts,
                                   __global float* edgeCoeffs, __global float* edgeLengths,
                                   __global float2* nodeVelocity,
                                   __global float2* nodeForce,
                                   unsigned int n)
{
    unsigned int id1 = get_global_id(1);
    //if (id1 >= n)
        //return;
    
    // Figure out position in warp and shift s_nodeForce address to forget about other warps
    unsigned int warpSize = 16;
    unsigned int warpId = get_local_id(0);
	s_buffer += get_local_id(1) * warpSize;
    
	if (id1 < n)
	{
		// Get data for the current node
		float2 node1 = (float2)(nodePosX[id1], nodePosY[id1]);
		float2 force = (float2)(0.0f, 0.0f);

		// Iterate over edges for spring force
		unsigned int firstEdge = edgeOffsets[id1];
		unsigned int lastEdge = firstEdge + edgeCounts[id1];
		for (unsigned int e = firstEdge + warpId; e < lastEdge; e += warpSize)
		{
			unsigned int id2 = edges[e];

			float2 node2 = (float2)(nodePosX[id2], nodePosY[id2]);

			float2 diff = (float2)(node2.x - node1.x, node2.y - node1.y);

			// + 1e-8f to avoid division by zero in case of identical position.
			float dist = hypot(diff.x, diff.y) + 1e-8f;
			float v = edgeCoeffs[e] * (dist - edgeLengths[e]) / dist;

			// Equivalent of force += diff * v
			force.x = fma(diff.x, v, force.x);
			force.y = fma(diff.y, v, force.y);
		}

		// Store this thread's result in local memory
		s_buffer[warpId] = force;
	}
    
    // On some hardware, no synchronization should be needed (warp-synchronous programming).
    // But synchronize anyway to be on the safe side.
    barrier(CLK_LOCAL_MEM_FENCE);
    
    if (warpId < 8)
    {
        s_buffer[warpId] += s_buffer[warpId + 8];
    }
    barrier(CLK_LOCAL_MEM_FENCE);
    if (warpId < 4)
    {
        s_buffer[warpId] += s_buffer[warpId + 4];
    }
    barrier(CLK_LOCAL_MEM_FENCE);
    if (warpId < 2)
    {
        s_buffer[warpId] += s_buffer[warpId + 2];
    }
    barrier(CLK_LOCAL_MEM_FENCE);
    if (warpId == 0)
    {
        s_buffer[0] += s_buffer[1];
                        
		if (id1 < n)
			nodeForce[id1] += s_buffer[0] - 0.01f * nodeVelocity[id1];          // Apply drag force and store overall value
    }
}
#else
__kernel void CalcForcesSpringDrag(__global float* nodePosX, __global float* nodePosY,
                                   __global unsigned int* edges, __global unsigned int* edgeOffsets, __global unsigned int* edgeCounts,
                                   __global float* edgeCoeffs, __global float* edgeLengths,
                                   __global float2* nodeVelocity,
                                   __global float2* nodeForce,
                                   unsigned int n)
{
    unsigned int id1 = get_global_id(0);
		if (id1 >= n)
				return;
				
		// Get data for the current node
		float2 node1 = (float2)(nodePosX[id1], nodePosY[id1]);
		float2 force = (float2)(0.0f, 0.0f);
		
		// Iterate over edges for spring force
		unsigned int firstEdge = edgeOffsets[id1];
		unsigned int lastEdge = firstEdge + edgeCounts[id1];
		for (unsigned int e = firstEdge; e < lastEdge; e++)
		{
				unsigned int id2 = edges[e];
				
				float2 node2 = (float2)(nodePosX[id2], nodePosY[id2]);
				
				float2 diff = (float2)(node2.x - node1.x, node2.y - node1.y);
				
				// + 1e-8f to avoid division by zero in case of identical position.
				float dist = hypot(diff.x, diff.y) + 1e-8f;
				float v = edgeCoeffs[e] * (dist - edgeLengths[e]) / dist;
				
				// Equivalent of force += diff * v
				force.x = fma(diff.x, v, force.x);
				force.y = fma(diff.y, v, force.y);
		}
						
		nodeForce[id1] += force - 0.01f * nodeVelocity[id1];          // Apply drag force and store overall value
}
#endif

__kernel void IntegrateRK0(__global float* nodePosX, __global float* nodePosY,
                           __global float* nodeMass,
                           __global float2* nodeK,
                           __global float2* nodeL,
                           __global float2* nodeVelocity,
                           __global float2* nodeForce,
                           float timestep,
                           int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;

		float mass = nodeMass[id];
		
		nodeK[3 * n + id] = (float2)(nodePosX[id], nodePosY[id]);
		
		float2 update = nodeVelocity[id] * timestep;
		nodeK[id] = update;
		nodeL[id] = nodeForce[id] * timestep / mass;
		
		nodePosX[id] += 0.5f * update.x;
		nodePosY[id] += 0.5f * update.y;
}

__kernel void IntegrateRK1(__global float* nodePosX, __global float* nodePosY,
                           __global float* nodeMass,
                           __global float2* nodeK,
                           __global float2* nodeL,
                           __global float2* nodeVelocity,
                           __global float2* nodeForce,
                           float maxVelocity,
                           float timestep,
                           int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;		

		float mass = nodeMass[id];
		
		float2 v = nodeVelocity[id] + 0.5f * nodeL[id];
		float vmagn = length(v);
		if (vmagn > maxVelocity)
				v *= maxVelocity / vmagn;
		
		float2 update = v * timestep;
		nodeK[n + id] = update;
		nodeL[n + id] = nodeForce[id] * timestep / mass;
		
		update = nodeK[3 * n + id] + 0.5f * update;
		nodePosX[id] = update.x;
		nodePosY[id] = update.y;
}

__kernel void IntegrateRK2(__global float* nodePosX, __global float* nodePosY,
                           __global float* nodeMass,
                           __global float2* nodeK,
                           __global float2* nodeL,
                           __global float2* nodeVelocity,
                           __global float2* nodeForce,
                           float maxVelocity,
                           float timestep,
                           int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;
		
		float mass = nodeMass[id];
		
		float2 v = nodeVelocity[id] + 0.5f * nodeL[n + id];
		float vmagn = length(v);
		if (vmagn > maxVelocity)
				v *= maxVelocity / vmagn;
		
		float2 update = v * timestep;
		nodeK[2 * n + id] = update;
		nodeL[2 * n + id] = nodeForce[id] * timestep / mass;
		
		update = nodeK[3 * n + id] + 0.5f * update;
		nodePosX[id] = update.x;
		nodePosY[id] = update.y;
}

__kernel void IntegrateRK3(__global float* nodePosX, __global float* nodePosY,
                           __global float* nodeMass,
                           __global float2* nodeK,
                           __global float2* nodeL,
                           __global float2* nodeVelocity,
                           __global float2* nodeForce,
                           float maxVelocity,
                           float timestep,
                           int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;
		
		float mass = nodeMass[id];
		
		float2 v = nodeVelocity[id] + 0.5f * nodeL[2 * n + id];
		float vmagn = length(v);
		if (vmagn > maxVelocity)
				v *= maxVelocity / vmagn;
		
		float2 k3 = v * timestep;
		float2 l3 = nodeForce[id] * timestep / mass;
		
		k3 = nodeK[3 * n + id] + (nodeK[id] + k3) / 6.0f + (nodeK[n + id] + nodeK[2 * n + id]) / 3.0f;
		nodePosX[id] = k3.x;
		nodePosY[id] = k3.y;
		
		v = (nodeL[id] + l3) / 6.0f + (nodeL[n + id] + nodeL[2 * n + id]) / 3.0f;
		vmagn = length(v);
		if (vmagn > maxVelocity)
				v *= maxVelocity / vmagn;
		
		nodeVelocity[id] += v;
}

__kernel void IntegrateEuler(__global float* nodePosX, __global float* nodePosY,
                             __global float* nodeMass,
                             __global float2* nodeVelocity,
                             __global float2* nodeForce,
                             float maxVelocity,
                             float timestep,
                             int n)
{
    int id = get_global_id(0);
    if (id >= n)
        return;
		
		float2 pos = (float2)(nodePosX[id], nodePosY[id]);
		float m = nodeMass[id];
		float2 v = nodeVelocity[id];
		float2 f = nodeForce[id];
		
		pos += v * timestep;
		
		v += f / m * timestep;
		float vmagn = length(v);
		if (vmagn > maxVelocity)
				v *= maxVelocity / vmagn;
		
		nodePosX[id] = pos.x;
		nodePosY[id] = pos.y;
		nodeVelocity[id] = v;
}


